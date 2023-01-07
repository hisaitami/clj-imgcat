(ns clj-imgcat.core-test
  (:require [clojure.test :refer :all]
            [clojure.string :as str]
            [clojure.java.io :as io]
            [clojure.java.shell :refer [sh]]
            [clj-imgcat.core :refer :all]))

(def test-file (io/file "README.md"))

(defn- wc [option file]
  (-> (sh "wc" option (str file))
      :out (str/split #"\s+") second Integer/parseInt))

(defn- base64-file [^java.io.File file]
  (-> (sh "base64" (str file))
      :out str/trim-newline))

(defn- base64-string [s]
  (-> (sh "bash" "-c" (format "echo -n %s | base64" s))
      :out str/trim-newline))

(deftest ->bytes-test
  (testing "Load a local file as byte-array"
    (is (= (-> test-file ->bytes count)
           (wc "-c" test-file)))))

(deftest ->base64-test
  (testing "Base64 Encoding"
    (testing "bytes"
      (is (= (-> test-file ->bytes ->base64)
             (base64-file test-file))))
    (testing "ascii string"
      (is (= (->base64 (.getBytes "hello"))
             (base64-string "hello"))))
    (testing "multi-byte string"
      (is (= (->base64 (.getBytes "こんにちは"))
             (base64-string "こんにちは"))))))

(deftest valid-size-test
  (testing "Returns true"
    (is (true? (valid-size? 10)))
    (is (true? (valid-size? "10")))
    (is (true? (valid-size? "01")))
    (is (true? (valid-size? "10px")))
    (is (true? (valid-size? "10%")))
    (is (true? (valid-size? "auto")))
    )
  (testing "Returns false"
    (is (false? (valid-size? -10)))
    (is (false? (valid-size? "-10")))
    (is (false? (valid-size? "1.0")))
    (is (false? (valid-size? " 1px")))
    (is (false? (valid-size? "1 %")))))

(deftest parse-options-test
  (testing "Takes nil or empty map, returns empty string"
    (is (= (parse-options nil) (parse-options {}) "")))
  (testing "Takes options and returns them as string"
    (is (= (parse-options {:width 34 :height 41 :preserveAspectRatio 1})
           ";width=34;height=41;preserveAspectRatio=1"))
  (testing "Ignore undefined options, returns empty string"
    (is (= (parse-options {:foo 1}) "")))
    (is (= (parse-options {:foo 1 :width 21 :height 23}) ";width=21;height=23"))))

(defn- inline-image-protocol
  ([file]
   (inline-image-protocol file nil))
  ([file opts]
   (str "\033]1337;File=inline=1"
        ";size=" (wc "-c" test-file)
        ";name=" (base64-string test-file)
        opts
        ":" (base64-file test-file) "\007"
        \newline)))

(deftest display-test
  (testing "Inline Image Protocol"
    (is (= (inline-image-protocol test-file)
           (with-out-str (display test-file))))))

(deftest imgcat-test
  (testing "imgcat logic"
    (is (= (inline-image-protocol test-file";width=11;height=13;preserveAspectRatio=0")
           (with-out-str (display test-file :width 11 :height 13 :preserveAspectRatio 0))))))

