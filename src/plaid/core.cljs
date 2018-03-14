(ns plaid.core
  (:require
   [clojure.string :as string]
   [planck.core :refer [spit slurp line-seq]]
   [planck.io :as io]
   [planck.http :as http]
   [planck.shell :as shell :refer [with-sh-dir]]))

(def patches-txt (line-seq (io/reader (io/resource "patches.txt"))))

(def patches (map #(string/split % " ") patches-txt))

(defn -main []
  (let [tmpdir (string/trim (:out (shell/sh "mktemp" "-d")))]
    (with-sh-dir tmpdir
      (shell/sh "git" "clone" "https://github.com/clojure/clojurescript")
      (doseq [[ticket url] patches]
        (spit (io/file tmpdir "temp.patch") (slurp url))
        (with-sh-dir (str tmpdir "/clojurescript")
          (let [res (shell/sh "git" "apply" "--check" "../temp.patch")]
            (when-not (zero? (:exit res))
              (println ticket "does not apply"))))))))
