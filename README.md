# clj-imgcat

imgcat written in Clojure

### Installation

Add the following dependency to your `project.clj` file

[![Clojars Project](https://clojars.org/clj-imgcat/latest-version.svg)](https://clojars.org/clj-imgcat)

### Usage

After launching the REPL in an iTerm window,

```
user=> (require '[clj-imgcat.core :refer [imgcat]])
nil
user=> (imgcat "images/logo.png")
```
To display remote image,

```
user=> (imgcat "https://clojure.org/images/clojure-logo-120b.png")
```

Execute via `lein run`

```
% lein run
Usage: lein run <image file>

% lein run images/logo.png
```

### References

Inline Images Protocol for iTerm2. [doc](https://www.iterm2.com/documentation-images.html)

Original sample [code](https://iterm2.com/utilities/imgcat)

### License

Copyright (c) 2020 hisaitami
Distributed under the terms of the [MIT License](LICENSE)

