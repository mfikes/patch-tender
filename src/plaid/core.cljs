(ns plaid.core
  (:require
   [clojure.string :as string]
   [planck.core :refer [spit slurp line-seq]]
   [planck.io :as io]
   [planck.http :as http]
   [planck.shell :as shell :refer [with-sh-dir]]))

(def patches (line-seq (io/reader (io/resource "patches.txt"))))

(defn -main []
  (let [tmpdir (string/trim (:out (shell/sh "mktemp" "-d")))]
    (with-sh-dir tmpdir
      (shell/sh "git" "clone" "https://github.com/clojure/clojurescript")
      (doseq [url patches]
        (spit (io/file tmpdir "temp.patch") (slurp url))
        (with-sh-dir (str tmpdir "/clojurescript")
          (let [res (shell/sh "git" "apply" "--check" "../temp.patch")]
            (when-not (zero? (:exit res))
              (println url "does not apply"))))))))
