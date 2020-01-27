(ns binio.core
  (:require [clojure.java.io :as io])
  (:import [java.io FileInputStream FileOutputStream IOException]))

(defn slurp-bytes
  [^String filename & opt]
  (let [file (io/file filename)
        buf (byte-array (.length file))]
    (with-open [fis (io/make-input-stream file (when opt (apply hash-map opt)))]
      (.read fis buf))
    buf))

(defn spit-bytes
 [^String filename content & opt]
 (if (bytes? content)
   (with-open [file (io/make-output-stream filename (when opt (apply hash-map opt)))]
     (.write file content))
   (throw (IOException. "Error. Must supply a byte-array to write"))))


