(ns binio.core-test
  (:require clojure.test
            [clojure.test.check :as tc]
            [clojure.test.check.generators :as gen]
            [clojure.test.check.properties :as prop]
            [clojure.test.check.clojure-test :refer [defspec]]
            [binio.core :refer :all]))


(defn ubyte?
  [n]
  (and (integer? n)
       (<= 0 n 255)))

(defn abs
  [x]
  (if (neg? x)
    (- x)
    x))

(defspec bits-roundtrip-test
  (do (println "Bits roundtrip test: Any number converted to bits and back remains unchanged")
    (prop/for-all [n (gen/choose 0 255)]
      (= (decode-bits (encode-bits n)) n))))

(defspec ubyte-test
  (do (println "Ubyte test: The decoded byte is an integer between 0 and 255 (inclusive)")
    (prop/for-all [b gen/byte]
      (ubyte? (decode-ubyte b)))))

(defspec ushort-sign-test 500
  (do (println "Ushort test: The decoded number is an integer between 0 and 65.535 (inclusive)")
    (prop/for-all [b (gen/vector gen/byte 2)]
      (and (integer? (decode-ushort b))
           (<= 0 (decode-ushort b) 65535)))))

(defspec ushort-sshort-conversion-test 500
  (do (println "Ushort Sshort conversion test: The decoded sshort converted to a ushort is the same as the decoded ushort")
    (prop/for-all [[b0 b1 :as b] (gen/vector gen/byte 2)]
      (if (neg? b0)
        (= (+ 65536 (decode-sshort b)) (decode-ushort b))
        (= (decode-ushort b) (decode-sshort b))))))

(defspec ulong-test 500
  (do (println "Ulong test: The decoded number is an integer between 0 and 4.294.967.295")
    (prop/for-all [b (gen/vector gen/byte 4)]
      (and (integer? (decode-ulong b))
           (<= 0 (decode-ulong b) 4294967295)))))

(defspec ulong-sslong-conversion-test 500
  (do (println "Ulong Slong conversion test: The decoded slong converted to a ulong is the same as the decoded ulong")
    (prop/for-all [[b0 & _ :as b] (gen/vector gen/byte 4)]
      (if (neg? b0)
        (= (+ 4294967296 (decode-slong b)) (decode-ulong b))
        (= (decode-ulong b) (decode-slong b))))))

(defspec ulong-rational-pos-test 500
  (do (println "Ulong rational positive test: The decoded number is a positive rational")
    (prop/for-all [b0 (gen/vector gen/byte 4)
                   b1 (gen/such-that #(not= 0 (reduce + %)) (gen/vector gen/byte 4))]
      (let [res (decode-ulongrational (concat b0 b1))]
        (and (pos? res) (rational? res))))))

(defspec ulong-rational-unit-test 500
  (do (println "Ulong rational unit test: The decoded number is > 1 when numerator > denominator")
    (prop/for-all [b0 (gen/vector gen/byte 4)
                   b1 (gen/such-that #(not= 0 (reduce + %)) (gen/vector gen/byte 4))]
      (let [res (decode-ulongrational (concat b0 b1))]
        (if (> (decode-ulong b0) (decode-ulong b1))
          (> res 1)
          (<= res 1))))))

(defspec slong-rational-test 500
  (do (println "Slong rational test: The decoded number is rational")
    (prop/for-all [b0 (gen/vector gen/byte 4)
                   b1 (gen/such-that #(not= 0 (reduce + %)) (gen/vector gen/byte 4))]
      (let [res (decode-ulongrational (concat b0 b1))]
        (rational? res)))))

(defspec slong-rational-sign-test 500
  (do (println "Slong rational sign test: The decoded number is positive if both the numerator and denominator are either positive or negative")
    (prop/for-all [b0 (gen/vector gen/byte 4)
                   b1 (gen/such-that #(not= 0 (reduce + %)) (gen/vector gen/byte 4))]
      (let [res (decode-slongrational (concat b0 b1))]
        (if (or (and (pos? (decode-slong b0)) (pos? (decode-slong b1)))
                (and (neg? (decode-slong b0)) (neg? (decode-slong b1))))
          (pos? res)
          (neg? res))))))

(defspec slong-rational-unit-test 500
  (do (println "Slong rational unit test: The decoded number in absolute value is > 1 when abs(numerator) > abs(denominator)")
    (prop/for-all [b0 (gen/vector gen/byte 4)
                   b1 (gen/such-that #(not= 0 (reduce + %)) (gen/vector gen/byte 4))]
      (let [res (decode-slongrational (concat b0 b1))]
        (if (> (abs (decode-slong b0)) (abs (decode-slong b1)))
          (> (abs res) 1)
          (<= (abs res) 1))))))
