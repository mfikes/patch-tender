(ns plaid.core
  (:require
   [clojure.string :as string]
   [planck.core :refer [spit slurp read-string]]
   [planck.io :as io]
   [planck.http :as http]
   [planck.shell :as shell :refer [with-sh-dir]]))

(def patches-edn (read-string (slurp (io/resource "patches.edn"))))

(def patches (partition 2 patches-edn))

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
