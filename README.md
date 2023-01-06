# clj-imgcat

imgcat written in Clojure

Displays one ore more images in iTerm2 inline. For example:

 ![](https://github.com/hisaitami/clj-imgcat/blob/master/screen.png)

## Installation

Add the following dependency to your `project.clj` file

[![Clojars Project](https://clojars.org/clj-imgcat/latest-version.svg)](https://clojars.org/clj-imgcat)

## Usage

After launching the REPL in an iTerm window,

```
user=> (require '[clj-imgcat.core :refer [imgcat]])
user=> (imgcat "logo.png")
```
To display remote image,

```
user=> (imgcat "https://clojure.org/images/clojure-logo-120b.png")
```
Specify options,


```
user=> (imgcat "logo.png" :width "100px" :height "100px" :preserveAspectRatio 0)
```
width and height are given as a number followed by a unit, `:width "50%"` or `":width "100px"`
or `:width "auto"`.


Execute via `lein run`

```
% lein run
Usage: lein run <image file>

% lein run images/logo.png
```

## References

* Inline Images Protocol for iTerm2. [doc](https://www.iterm2.com/documentation-images.html)
* Original sample [code](https://iterm2.com/utilities/imgcat)
* iTerm2's Proprietary Escape Codes [doc](https://iterm2.com/documentation-escape-codes.html)

## License

Copyright (c) 2023 hisaitami
Distributed under the terms of the [MIT License](LICENSE)

