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

(deftest bytes->base64-test
  (testing "Encode bytes using Base64 representation"
    (is (= (-> test-file ->bytes bytes->base64)
           (base64-file test-file)))))

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

