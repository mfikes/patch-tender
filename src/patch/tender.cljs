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

(defn -main [& args]
  (let [test? (= "test" (first args))
        build? (or test? (= "build" (first args)))
        patch-filter (if build?
                       (let [tickets (resource-lines "tickets.txt")]
                         (fn [url]
                           (some (fn [ticket]
                                   (string/includes? url ticket))
                             tickets)))
                       (constantly true))
        tmpdir (io/file (string/trim (:out (shell/sh "mktemp" "-d"))))]
    (with-sh-dir tmpdir
      (shell/sh "git" "clone" "https://github.com/clojure/clojurescript")
      (doseq [url (filter patch-filter patches)]
        (fetch-patch tmpdir url)
        (with-sh-dir (io/file tmpdir "clojurescript")
          (let [res (if build?
                      (shell/sh "git" "apply" "../temp.patch")
                      (shell/sh "git" "apply" "--check" "../temp.patch"))]
            (when-not (zero? (:exit res))
              (println url "does not apply")))))
      (when build?
        (println "Building...")
        (with-sh-dir (io/file tmpdir "clojurescript")
          (let [res (shell/sh "script/build")]
            (println (:err res))
            (println (:out res)))))
     (when test?
        (println "Testing...")
        (with-sh-dir (io/file tmpdir "clojurescript")
          (shell/sh "script/bootstrap")
          (let [res (shell/sh "script/test")]
            (println (:err res))
            (println (:out res))))))))
