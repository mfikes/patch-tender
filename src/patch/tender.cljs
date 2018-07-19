(ns patch.tender
  (:require
   [clojure.string :as string]
   [planck.core :refer [spit slurp line-seq]]
   [planck.io :as io]
   [planck.shell :as shell :refer [with-sh-dir]]))

(defn resource-lines [filename]
  (line-seq (io/reader (io/resource filename))))

(def patches (resource-lines "patches.txt"))

(defn get-env []
  (into {}
    (map (fn [line]
           (string/split line #"=" 2))
      (-> (:out (shell/sh "env"))
        (string/split #"\n")))))

(defn cache-dir []
  (io/file (get (get-env) "HOME") ".patch-tender" "patches"))

(defn fetch-patch [tmpdir url]
  (let [cache-dir (cache-dir)]
    (let [cache-file (io/file cache-dir (str (Math/abs (hash url))))]
      (when-not (io/file-attributes cache-file)
        (when-not (io/make-parents cache-file)
          (throw (ex-info "failed to make cache dir" {:dir cache-dir})))
        (spit cache-file (slurp url)))
      (io/copy cache-file (io/file tmpdir "temp.patch")))))

(defn inst->branch-name-suffix
  [ts]
  (let [normalize (fn [n len]
                    (loop [ns (str n)]
                      (if (< (count ns) len)
                        (recur (str "0" ns))
                        ns)))]
    (str
      (.getUTCFullYear ts) "-"
      (normalize (inc (.getUTCMonth ts)) 2) "-"
      (normalize (.getUTCDate ts) 2) "T"
      (normalize (.getUTCHours ts) 2))))

(defn inst->branch-name
  [ts]
  (str "patch-tender-" (inst->branch-name-suffix ts)))

(defn -main [& args]
  (let [push? (= "push" (first args))
        test? (= "test" (first args))
        test-ndx (and test? (when-some [ndx (second args)] (js/parseInt ndx)))
        build? (or test? (= "build" (first args)))
        patch-filter (if build?
                       (let [tickets (resource-lines "tickets.txt")]
                         (fn [url]
                           (some (fn [ticket]
                                   (string/includes? url ticket))
                             tickets)))
                       (constantly true))
        tmpdir (io/file (string/trim (:out (shell/sh "mktemp" "-d"))))
        clojurescript-dir (io/file tmpdir "clojurescript")
        branch-name (inst->branch-name (js/Date.))]
    (with-sh-dir tmpdir
      (shell/sh "git" "clone" "https://github.com/clojure/clojurescript")
      (with-sh-dir clojurescript-dir
        (shell/sh "git" "checkout" "-b" branch-name))
      (doseq [url (if test-ndx
                    [(nth (filter patch-filter patches) test-ndx)]
                    (filter patch-filter patches))]
        (when test-ndx (println "Testing" url))
        (fetch-patch tmpdir url)
        (with-sh-dir clojurescript-dir
          (let [res (if push?
                      (let [res2 (shell/sh "git" "apply" "--check" "../temp.patch")]
                        (if (zero? (:exit res2))
                          (shell/sh "git" "am" "../temp.patch")
                          res2))
                      (shell/sh "git" "apply" "--check" "../temp.patch"))]
            (when-not (zero? (:exit res))
              (println url "does not apply"))))))
    (when build?
      (println "Building...")
      (with-sh-dir (io/file tmpdir "clojurescript")
        (let [res (shell/sh "script/build")]
          (println (:err res))
          (println (:out res)))))
    (when test?
      (println "Testing...")
      (with-sh-dir clojurescript-dir
        (shell/sh "script/bootstrap")
        (let [res (shell/sh "script/test")]
          (println (:err res))
          (println (:out res)))))
    (when push?
      (println "Pushing...")
      (with-sh-dir clojurescript-dir
        (shell/sh "git" "remote" "add" "personal" "git@github.com:mfikes/clojurescript")
        (shell/sh "git" "push" "personal"))
      (let [sha (string/trim (:out (shell/sh "git" "rev-parse" "HEAD")))]
        (spit "README.md"
          (str
            "# patch-tender" \newline
            "ClojureScript [JIRA](https://dev.clojure.org/jira/browse/CLJS) contains many candidate patches that have not yet been applied to master." \newline
            \newline
            "`patch-tender` is used to maintain a curated list of patches, applying them in a branch so they can be easily soak-tested in downstream projects." \newline
            \newline
            "The latest set of [applied patches](https://github.com/clojure/clojurescript/compare/master...mfikes:" branch-name ") are in this branch " \newline
            \newline
            "   https://github.com/mfikes/clojurescript/commits/" branch-name " " \newline
            \newline
            "Branch build status: " "[![Build Status](https://travis-ci.org/mfikes/clojurescript.svg?branch=" branch-name ")](https://travis-ci.org/mfikes/clojurescript)"
            " " "[![Build status](https://ci.appveyor.com/api/projects/status/oggs1yydb8c2t6pa/branch/" branch-name "?svg=true)](https://ci.appveyor.com/project/mfikes/clojurescript/branch/" branch-name ")"
            \newline \newline
            "If using `deps.edn` you can depend on this set of patches via" \newline
            "```clojure" \newline
            'org.clojure/clojurescript " " {:git/url "https://github.com/mfikes/clojurescript"
                                            :sha     sha} \newline
            "```" \newline
            \newline
            "Or you can clone and build this branch for use in a `lein`- or `boot`-based project:" \newline
            \newline
            "```" \newline
            "$ git clone https://github.com/mfikes/clojurescript -b " branch-name \newline
            "$ cd clojurescript" \newline
            "$ script/build" \newline
            "```" \newline
            "For more info see https://clojurescript.org/community/building"))))))
