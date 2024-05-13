(ns clj-imgcat.core
  (:require [clojure.java.io :as io]
            [clojure.string :as s])
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
  [^bytes b]
  (.encodeToString (java.util.Base64/getEncoder) b))

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
          (when (some #(= % preserveAspectRatio) [0 1 "0" "1"])
            [";preserveAspectRatio=" preserveAspectRatio]))))

(defn inline-image-protocol
  "Returns the string of Inline Image Protocol for iTerm2"
  [^bytes img ^java.lang.String fname & opts]
  (str "\033]1337;File=inline=1"
       ";size=" (count img)
       ";name=" (->base64 (.getBytes fname))
       (parse-options opts)
       ":" (->base64 img) "\007"))

(defn print_image
  "Display an image from bytes using Inline Image Protocol"
  [img fname opts & more]
  (print (apply str (inline-image-protocol img fname opts) more)))

(defn imgcat
  "Displays an image within a terminal.
  Examples:
    (imgcat \"logo.png\")
    (imgcat \"logo.png\" :width 80)
    (imgcat \"logo.png\" :width \"25%\" :height \"25%\")
    (imgcat \"logo.png\" :width \"50px\" :height \"100px\" :preserveaspectratio 0)"
  [x & {:as opts}]
  (let [[img fname] (if (bytes? x) [x ""] [(->bytes x) (str x)])]
    (print_image img fname opts \newline)))

(defn -main [file & opts]
  (imgcat file (->> (partition 2 opts)
                    (map (fn [[k v]] [(keyword (s/replace k #"^:" "")) v]))
                    (into {}))))
