(ns clj-imgcat.core-test
  (:require [clojure.test :refer :all]
            [clojure.string :as str]
            [clojure.java.shell :refer [sh]]
            [clj-imgcat.core :refer :all]))

(def test-file "README.md")

(defn- wc [option file]
  (-> (sh "wc" option file)
      :out (str/split #"\s+") second Integer/parseInt))

(defn- base64 [file]
  (-> (sh "base64" file)
      :out str/trim-newline))

(deftest file->bytes-test
  (testing "Load a local file as byte-array"
    (is (= (-> test-file file->bytes count)
           (wc "-c" test-file)))))

(deftest bytes->base64-string-test
  (testing "Encode using Base64 representation"
    (is (= (-> test-file file->bytes bytes->base64-string)
           (base64 test-file)))))

(deftest display-test
  (testing "Inline Image Protocol"
    (let [base64-string (base64 test-file)]
      (is (= (str "\033]1337;File=;inline=1:" base64-string "\007" \newline)
             (with-out-str (display base64-string)))))))

(deftest imgcat-test
  (testing "imgcat logic"
    (let [base64-string (base64 test-file)]
      (is (= (str "\033]1337;File=;inline=1:" base64-string "\007" \newline)
             (with-out-str (imgcat test-file)))))))
