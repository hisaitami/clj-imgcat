(ns clj-imgcat.core
  (:require [clojure.java.io :as io]))

(defn file->bytes
  "read a file into a byte array"
  [file]
  (with-open [in (io/input-stream file)
              out (java.io.ByteArrayOutputStream.)]
    (io/copy in out)
    (.toByteArray out)))

(defn bytes->base64-string
  "Encodes the byte array into a base64 string"
  [^bytes bytes]
  (.encodeToString (java.util.Base64/getEncoder) bytes))

(defn display
  "Displays an image using Inline Image Protocol for iTerm2"
  [base64-string]
  (println (str "\033]1337;File=;inline=1:" base64-string "\007")))

(defn imgcat
  "Displays an image within a terminal."
  [file]
  (display (bytes->base64-string (file->bytes file))))

(defn -main [& args]
  (if-let [file (first args)]
    (imgcat file)
    (println "Usage: lein run <image file>")))
