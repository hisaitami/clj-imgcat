(ns clj-imgcat.core-test
  (:require [clojure.test :refer [deftest is testing]]
            [clojure.string :as str]
            [clojure.java.io :as io]
            [clojure.java.shell :refer [sh]]
            [clj-imgcat.core :refer [->bytes ->base64 valid-size? parse-options
                                     inline-image-protocol print_image imgcat]]))

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

(deftest inline-image-protocol-test
  (testing "Inline Image Protocol"
    (let [img (.getBytes "123456789")
          name "hello"]
      (testing "No optional outputs"
        (is (= (inline-image-protocol img name)
               (inline-image-protocol img name {})
               (inline-image-protocol img name nil)
               (format "\033]1337;File=inline=1;size=%d;name=%s:%s\007"
                       (count img) (->base64 (.getBytes name)) (->base64 img)))))
      (testing "with options"
        (is (= (inline-image-protocol img name {:width 40})
               (format "\033]1337;File=inline=1;size=%d;name=%s;width=40:%s\007"
                       (count img) (->base64 (.getBytes name)) (->base64 img))))))))

(deftest print_image-test
  (testing "Prints a string of Inline Image Protocol"
    (let [img (->bytes test-file)
          name (str test-file)]
      (testing "with minimum args"
        (is (= (with-out-str (print_image img name {}))
               (with-out-str (print_image img name nil))
               (format "\033]1337;File=inline=1;size=%d;name=%s:%s\007"
                       (count img)
                       (base64-string (str test-file))
                       (base64-file test-file)))))
      (testing "with options"
        (is (= (with-out-str (print_image img name {:width 80}))
               (format "\033]1337;File=inline=1;size=%d;name=%s;width=80:%s\007"
                       (count img)
                       (base64-string (str test-file))
                       (base64-file test-file)))))
      (testing "with options and more"
        (is (= (with-out-str (print_image img name {:width 80} \newline name \newline))
               (format "\033]1337;File=inline=1;size=%d;name=%s;width=80:%s\007%s"
                       (count img)
                       (base64-string (str test-file))
                       (base64-file test-file)
                       (apply str [\newline name \newline]))))))))

(deftest imgcat-test
  (testing "with bytes"
    (is (= (with-out-str (imgcat (->bytes test-file)))
           (format "\033]1337;File=inline=1;size=%d;name=%s:%s\007%s"
                   (count (->bytes test-file))
                   ""
                   (base64-file test-file)
                   \newline))))
  (testing "with file path"
    (is (= (with-out-str (imgcat (str test-file)))
           (format "\033]1337;File=inline=1;size=%d;name=%s:%s\007%s"
                   (count (->bytes test-file))
                   (base64-string (str test-file))
                   (base64-file test-file)
                   \newline))))
  (testing "with file"
    (is (= (with-out-str (imgcat test-file))
           (format "\033]1337;File=inline=1;size=%d;name=%s:%s\007%s"
                   (count (->bytes test-file))
                   (base64-string (str test-file))
                   (base64-file test-file)
                   \newline))))
  (testing "options"
    (is (= (with-out-str (imgcat test-file :width "10px" :height "20px" :preserveAspectRatio 0))
           (format "\033]1337;File=inline=1;size=%d;name=%s%s:%s\007%s"
                   (count (->bytes test-file))
                   (base64-string (str test-file))
                   ";width=10px;height=20px;preserveAspectRatio=0"
                   (base64-file test-file)
                   \newline)))))

