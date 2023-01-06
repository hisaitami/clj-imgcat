(ns clj-imgcat.core
  (:require [clojure.java.io :as io])
  (:gen-class))

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

(defn valid-size?
  "width and height are given as a number followed by a unit, or the word \"auto\".
  N: N character cells.
  Npx: N pixels.
  N%: N percent of the session's width or height.
  auto: The image's inherent size will be used to determine an appropriate dimension.
  "
  [x]
  (if (re-matches #"(\d+(px|%)*|auto)" (str x)) true false))

(defn parse-options
  "width - output width of the image in character cells, pixels or percent
  height - output height of the image in character cells, pixels or percent
  preserveaspectratio - 0 or 1, if 1, fill the specified width and height without stretching"
  [{:keys [width
           height
           preserveAspectRatio]}]
  (apply str
         (concat
          (when (valid-size? width)
            [";width=" width])
          (when (valid-size? height)
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
  "Displays an image within a terminal.

  Examples:
      (imgcat \"logo.png\")
      (imgcat \"avatar.png\" :width \"100px\" :height \"100px\")
  "
  [file & {:as options}]
  (display file options))

(defn -main [& args]
  (if-let [file (first args)]
    (imgcat file)
    (println "Usage: lein run <image file>")))
