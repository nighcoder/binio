(ns binio.core
  (:require [clojure.java.io :as io])
  (:import [java.io FileInputStream FileOutputStream IOException]))

(defn slurp-bytes
  [^String filename]
  (let [file (io/file filename)
        buf (byte-array (.length file))]
    (with-open [fis (FileInputStream. file)]
      (.read fis buf))
    buf))

(defn spit-bytes
 [^String filename content]
 (if (bytes? content)
   (with-open [file (FileOutputStream. filename)]
     (.write file content))
   (throw (IOException. "Error. Must supply a byte-array to write"))))


