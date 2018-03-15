(ns plaid.core
  (:require
   [clojure.string :as string]
   [planck.core :refer [spit slurp line-seq]]
   [planck.io :as io]
   [planck.shell :as shell :refer [with-sh-dir]]))

(def patches (line-seq (io/reader (io/resource "patches.txt"))))

(defn get-env []
  (into {}
    (map (fn [line]
           (string/split line #"=" 2))
      (-> (:out (shell/sh "env"))
        (string/split #"\n")))))

(defn plaid-cache []
  (io/file (get (get-env) "HOME") ".plaid" ".patches"))

(defn fetch-patch [tmpdir url]
  (let [plaid-cache (plaid-cache)]
    (when-not (io/file-attributes plaid-cache)
      (shell/sh "mkdir" "-p" (:path plaid-cache)))
    (let [cache-file (io/file plaid-cache (Math/abs (hash url)))]
      (when-not (io/file-attributes cache-file)
        (spit cache-file (slurp url)))
      (spit (io/file tmpdir "temp.patch") (slurp cache-file)))))

(defn -main []
  (let [tmpdir (string/trim (:out (shell/sh "mktemp" "-d")))]
    (with-sh-dir tmpdir
      (shell/sh "git" "clone" "https://github.com/clojure/clojurescript")
      (doseq [url patches]
        (fetch-patch tmpdir url)
        (with-sh-dir (str tmpdir "/clojurescript")
          (let [res (shell/sh "git" "apply" "--check" "../temp.patch")]
            (when-not (zero? (:exit res))
              (println url "does not apply"))))))))
