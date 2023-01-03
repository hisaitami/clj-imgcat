(ns clj-imgcat.core
  (:require [clojure.java.io :as io]))

(defn ->bytes
  "read a file into a byte array"
  [file]
  (with-open [in (io/input-stream file)
              out (java.io.ByteArrayOutputStream.)]
    (io/copy in out)
    (.toByteArray out)))

(defn ->base64-string
  "Encodes the byte array into a base64 string"
  [^bytes bytes]
  (.encodeToString (java.util.Base64/getEncoder) bytes))

(defn parse-options [{:keys [:width :height :preserveAspectRatio] :as options}]
  (->> options
       (map (fn [[k v]] (str (name k) "=" v ";")))
       (apply str)))

(defn display
  "Displays an image using Inline Image Protocol for iTerm2"
  [base64-string & {:as options}]
  (println (str "\033]1337;"
                "File=;" (parse-options options) "inline=1:"
                base64-string "\007")))

(defn imgcat
  "Displays an image within a terminal."
  [file & {:as options}]
  (display (-> file ->bytes ->base64-string) options))

(defn -main [& args]
  (if-let [file (first args)]
    (imgcat file)
    (println "Usage: lein run <image file>")))
