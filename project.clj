(defproject clj-imgcat "0.3.0-SNAPSHOT"
  :description "imgcat written in Clojure"
  :url "https://github.com/hisaitami/clj-imgcat"
  :license {:name "MIT License"
            :url "https://github.com/hisaitami/clj-imgcat/blob/master/LICENSE"}
  :dependencies [[org.clojure/clojure "1.11.1"]]
  :repl-options {:init-ns clj-imgcat.core}
  :aot [clj-imgcat.core]
  :main clj-imgcat.core)
