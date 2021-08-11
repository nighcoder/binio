(ns binio.core
  (:require [clojure.java.io :as io])
  (:import [java.io ByteArrayOutputStream FileOutputStream IOException]))

(defn slurp-bytes
  [^String uri & opt]
  (let [out (new ByteArrayOutputStream)]
    (io/copy (io/input-stream uri) out)
    (.toByteArray out)))

(defn spit-bytes
 [^String filename content & opt]
 (if (bytes? content)
   (with-open [file (io/make-output-stream filename (when opt (apply hash-map opt)))]
     (.write file content))
   (throw (IOException. "Error. Must supply a byte-array to write"))))

(defn encode-bits
  [n]
  (->> (repeat n)
       (map bit-and [128 64 32 16 8 4 2 1])
       (map #(if (pos? %) 1 0))))

(defn decode-bits
  [bin-seq]
  (->> (iterate (partial * 2) 1)
       (take 8)
       reverse
       (map * bin-seq)
       (reduce +)))

(defn decode-ubyte
  [b]
  (let [b (short b)]
    (if (neg? b)
      (+ b 256)
      b)))

(defn decode-ushort
  [[b0 b1]]
  (int (bit-or (bit-shift-left (decode-ubyte b0) 8) (decode-ubyte b1))))

(defn decode-sshort
  [[b0 b1]]
  (if (neg? b0)
    (- (inc (decode-ushort [(bit-not b0) (bit-not b1)])))
    (decode-ushort [b0 b1])))

(defn decode-ulong
  [[b0 b1 b2 b3]]
  (long (bit-or (bit-shift-left (decode-ubyte b0) 24)
                (bit-shift-left (decode-ubyte b1) 16)
                (bit-shift-left (decode-ubyte b2) 8)
                (decode-ubyte b3))))

(defn decode-slong
  [[b0 b1 b2 b3]]
  (if (neg? b0)
    (- (inc (decode-ulong (map bit-not [b0 b1 b2 b3]))))
    (decode-ulong [b0 b1 b2 b3])))

(defn decode-ulongrational
  [[b0 b1 b2 b3 b4 b5 b6 b7]]
  (/ (decode-ulong [b0 b1 b2 b3])
     (decode-ulong [b4 b5 b6 b7])))

(defn decode-slongrational
  [[b0 b1 b2 b3 b4 b5 b6 b7]]
  (/ (decode-slong [b0 b1 b2 b3])
     (decode-slong [b4 b5 b6 b7])))

(defn hex [n]
  (format "0x%02X" n))

(defn decode-float32
  [[b0 b1 b2 b3]]
  (let [m_exp (take 24 (iterate #(float (/ % 2)) 1))
        b0 (encode-bits (decode-ubyte b0))
        b1 (encode-bits (decode-ubyte b1))
        b2 (encode-bits (decode-ubyte b2))
        b3 (encode-bits (decode-ubyte b3))
        bits (concat b0 b1 b2 b3)
        sign (first bits)
        exponent (- (decode-bits (take 8 (rest bits))) 127)
        mantissa (cons 1 (drop 9 bits))]
     (* (reduce * (repeat sign -1))
        (reduce + (map * mantissa m_exp))
        (if (neg? exponent)
          (reduce / 1 (repeat (- exponent) 2N))
          (reduce * (repeat exponent 2N))))))
