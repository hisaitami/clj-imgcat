# clj-imgcat

imgcat written in Clojure

### Usage

[![Clojars Project](https://img.shields.io/clojars/v/clj-imgcat.svg)](https://clojars.org/clj-imgcat)

```
user=> (require '[clj-imgcat.core :refer [imgcat]])
nil
user=> (imgcat "https://clojure.org/images/clojure-logo-120b.png")
```

Execute from command line

```
% lein run
Usage: lein run <image file>

% lein run https://clojure.org/images/clojure-logo-120b.png
```

### Links

Inline Images Protocol for iTerm2.
https://www.iterm2.com/documentation-images.html

Original sample code
https://iterm2.com/utilities/imgcat
