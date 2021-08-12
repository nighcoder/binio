(ns org.clojars.nighcoder.binio.core
  (:require [clojure.java.io :as io])
  (:import [java.io ByteArrayOutputStream FileOutputStream IOException]))

(defn slurp-bytes
  "Attempts to open uri as input stream and copy its content to a byte array."
  [^String uri]
  (let [out (new ByteArrayOutputStream)]
    (io/copy (io/input-stream uri) out)
    (.toByteArray out)))

(defn spit-bytes
  "Saves `content` to `filename`. Passes `opt` to output stream."
  [^String filename content & opt]
  (if (bytes? content)
    (with-open [file (io/make-output-stream filename (when opt (apply hash-map opt)))]
      (.write file content))
    (throw (IOException. "Error. Must supply a byte-array to write"))))

(defn encode-bits
  "Returns a list of 8 bits (0 or 1) that represents the number `n` in base 2.
  `n` is a unsigned byte or an integer from 0 to 255 inclusive.

  Example usage:
```
  => (encode-bits 22)
  (0 0 0 1 0 1 1 0)
```"
  [n]
  (->> (repeat n)
       (map bit-and [128 64 32 16 8 4 2 1])
       (map #(if (pos? %) 1 0))))

(defn decode-bits
  "Returns the decimal value of `bin-seq`, where `bin-seq` is a list of 8 bits (0 or 1).

  Example usage:
```
  => (decode-bits [0 0 1 0 1 1 1 0])
  46
```"
  [bin-seq]
  (->> (iterate (partial * 2) 1)
       (take 8)
       reverse
       (map * bin-seq)
       (reduce +)))

(defn decode-ubyte
  "Returns the base 10 value of `b` interpreted as an unsigned byte (8 bit integer), where `b` is a **byte** or and integer from -128 to 127. "
  [b]
  (let [b (short b)]
    (if (neg? b)
      (+ b 256)
      b)))

(defn decode-ushort
  "Returns the base 10 value of a *seq of two bytes* interpreted as an unsigned short (16 bit integer).

  Example usage:
```
  => (decode-ushort [12 -30])
  3298
```"
  [[b0 b1]]
  (int (bit-or (bit-shift-left (decode-ubyte b0) 8) (decode-ubyte b1))))

(defn decode-sshort
  "Returns the base 10 value of a *seq of two bytes* interpreted as a signed short (16 bit integer).

  Example usage:
```
  => (decode-sshort [12 -30])
  3298
```"
  [[b0 b1]]
  (if (neg? b0)
    (- (inc (decode-ushort [(bit-not b0) (bit-not b1)])))
    (decode-ushort [b0 b1])))

(defn decode-ulong
  "Returns the base 10 value of a *seq of four bytes* interpreted as an unsigned long (32 bit integer).

  Example usage:
```
  => (decode-ulong [-2 -30 118 108])
  4276254316
```"
  [[b0 b1 b2 b3]]
  (long (bit-or (bit-shift-left (decode-ubyte b0) 24)
                (bit-shift-left (decode-ubyte b1) 16)
                (bit-shift-left (decode-ubyte b2) 8)
                (decode-ubyte b3))))

(defn decode-slong
  "Returns the base 10 value of a *seq of four bytes* interpreted as a signed long (32 bit integer).

  Example usage:
```
  => (decode-slong [-2 -30 118 108])
  -18712980
```"
  [[b0 b1 b2 b3]]
  (if (neg? b0)
    (- (inc (decode-ulong (map bit-not [b0 b1 b2 b3]))))
    (decode-ulong [b0 b1 b2 b3])))

(defn decode-ulongrational
  "Returns the rational value of a *seq of eight bytes* interpreted as two unsigned longs (32 bit integers).
  First four bytes form the numerator and the last four bytes form the denominator.

  Example usage:
```
  => (decode-ulongrational [34 41 -113 80 -33 -21 9 33])
  573149008/3756722465
```"
  [[b0 b1 b2 b3 b4 b5 b6 b7]]
  (/ (decode-ulong [b0 b1 b2 b3])
     (decode-ulong [b4 b5 b6 b7])))

(defn decode-slongrational
  "Returns the rational value of a *seq of eight bytes* interpreted as two signed longs (32 bit integers).
  First four bytes form the numerator and the last four bytes form the denominator.

  Example usage:
```
  => (decode-slongrational [34 41 -113 80 -33 -21 9 33])
  -573149008/538244831
```"
  [[b0 b1 b2 b3 b4 b5 b6 b7]]
  (/ (decode-slong [b0 b1 b2 b3])
     (decode-slong [b4 b5 b6 b7])))

(defn hex [n]
  (format "0x%02X" n))

(defn decode-float32
  "Returns the decimal value of a *seq of four bytes* interpred as single precision floating number."
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

(defn decode-ascii-str
  "Returns the contents of `b-seq` encoded as ascii string up to first occurence of 0 or the end of sequence."
  [b-seq]
  (->> b-seq (take-while (complement zero?)) (map char) (reduce str)))
