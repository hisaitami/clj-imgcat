(ns clj-imgcat.core
  (:require [clojure.java.io :as io]))

(defn ->bytes
  "read a file into a byte array"
  [file]
  (with-open [in (io/input-stream file)
              out (java.io.ByteArrayOutputStream.)]
    (io/copy in out)
    (.toByteArray out)))

(defn bytes->base64
  "Encodes the byte array into a base64 string"
  [^bytes bytes]
  (.encodeToString (java.util.Base64/getEncoder) bytes))

(defn string->base64
  "Encodes the string into a base64 string"
  [^java.lang.String string]
  (bytes->base64 (byte-array (map byte string))))

(defn parse-options
  "width - output width of the image in pixels
  height - output height of the image in pixels
  preserveaspectratio - 0 or 1, if 1, fill the specified width and height without stretching"
  [{:keys [width
           height
           preserveAspectRatio]}]
  (apply str
         (concat
          (when (pos-int? width)
            [";width=" width])
          (when (pos-int? height)
            [";height=" height])
          (when (some #(= % preserveAspectRatio) [0 1])
            [";preserveAspectRatio=" preserveAspectRatio]))))

(defn display
  "Displays an image using Inline Image Protocol for iTerm2"
  [file & {:as options}]
  (let [bytes (-> file ->bytes)]
    (println (str "\033]1337;File=inline=1"
                  ";size=" (count bytes)
                  ";name=" (string->base64 (str file))
                  (parse-options options)
                  ":" (bytes->base64 bytes) "\007"))))

(defn imgcat
  "Displays an image within a terminal."
  [file & {:as options}]
  (display file options))

(defn -main [& args]
  (if-let [file (first args)]
    (imgcat file)
    (println "Usage: lein run <image file>")))
