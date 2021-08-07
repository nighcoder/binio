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


(defn decode-ubyte
  [b]
  (let [b (short b)]
    (if (neg? b)
      (+ b 256)
      b)))

(defn decode-ushort
  [b0 b1]
  (int (bit-or (bit-shift-left (decode-ubyte b0) 8) (decode-ubyte b1))))

(defn decode-ulong
  [b0 b1 b2 b3]
  (long (bit-or (bit-shift-left (decode-ubyte b0) 24)
                (bit-shift-left (decode-ubyte b1) 16)
                (bit-shift-left (decode-ubyte b2) 8)
                (decode-ubyte b3))))

(defn decode-slong
  [b0 b1 b2 b3]
  (int (bit-or (bit-shift-left b0 24)
               (bit-shift-left b1 16)
               (bit-shift-left b2 8)
               b3)))

(defn decode-ulongrational
  [b0 b1 b2 b3 b4 b5 b6 b7]
  (/ (decode-ulong b0 b1 b2 b3)
     (decode-ulong b4 b5 b6 b7)))

(defn decode-slongrational
  [b0 b1 b2 b3 b4 b5 b6 b7]
  (/ (decode-slong b0 b1 b2 b3)
     (decode-slong b4 b5 b6 b7)))

(defn hex [n]
  (format "0x%02X" n))

(defn dec->bin
  [n]
  (->> (repeat n)
       (map bit-and [128 64 32 16 8 4 2 1])
       (map #(if (pos? %) 1 0))))

(defn bin->dec
  [bin-seq]
  (reduce + (map * bin-seq (reverse (take 8 (iterate (partial * 2) 1))))))

(defn decode-float32
  [b0 b1 b2 b3]
  (let [m_exp (take 24 (iterate #(float (/ % 2)) 1))
        b0 (dec->bin (decode-ubyte b0))
        b1 (dec->bin (decode-ubyte b1))
        b2 (dec->bin (decode-ubyte b2))
        b3 (dec->bin (decode-ubyte b3))
        bits (concat b0 b1 b2 b3)
        sign (first bits)
        exponent (- (bin->dec (take 8 (rest bits))) 127)
        mantissa (cons 1 (drop 9 bits))]
     (* (reduce * (repeat sign -1))
        (reduce + (map * mantissa m_exp))
        (if (neg? exponent)
          (reduce / 1 (repeat (- exponent) 2N))
          (reduce * (repeat exponent 2N))))))
