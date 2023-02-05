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

(defn ->base64
  "Encodes the byte array into a base64 string"
  [^bytes bytes]
  (.encodeToString (java.util.Base64/getEncoder) bytes))

(defn valid-size?
  "width and height are given as a number followed by a unit, or the word \"auto\".
  N: N character cells.
  Npx: N pixels.
  N%: N percent of the session's width or height.
  auto: The image's inherent size will be used to determine an appropriate dimension."
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

(defn inline-image-protocol
  "Returns the string of Inline Image Protocol for iTerm2"
  [^bytes img ^java.lang.String name & options]
  (str "\033]1337;File=inline=1"
       ";size=" (count img)
       ";name=" (->base64 (.getBytes name))
       (parse-options options)
       ":" (->base64 img) "\007"))

(defn print_image
  "Display an image from bytes using Inline Image Protocol"
  [bytes fname options & more]
  (print (apply str (inline-image-protocol bytes fname options) more)))

(defn imgcat
  "Displays an image within a terminal.
  Examples:
    (imgcat \"logo.png\")
    (imgcat \"logo.png\" :width 80)
    (imgcat \"logo.png\" :width \"25%\" :height \"25%\")
    (imgcat \"logo.png\" :width \"50px\" :height \"100px\" :preserveaspectratio 0)"
  [img & {:as options}]
  (let [[bytes fname] (if (bytes? img) [img ""] [(->bytes img) (str img)])]
    (print_image bytes fname options \newline)))

(defn -main [& args]
  (if-let [file (first args)]
    (imgcat file)
    (println "Usage: lein run <image file>")))
