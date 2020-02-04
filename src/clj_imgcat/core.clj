(ns clj-imgcat.core
  (:require [clojure.java.io :as io]))

(defn slurp-bytes
  "Slurp the bytes from a slurpable thing
  https://clojuredocs.org/clojure.core/slurp"
  [x]
  (with-open [out (java.io.ByteArrayOutputStream.)]
    (io/copy (io/input-stream x) out)
    (.toByteArray out)))

(defn imgcat [x]
  "Display images with the iTerm2 using the Inline Images Protocol"
  (->> (slurp-bytes x)
       (.encodeToString (java.util.Base64/getEncoder))
       (printf (str "\033]1337;File=;inline=1:%s" (char 7)))))
