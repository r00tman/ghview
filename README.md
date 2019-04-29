# ghview

View your GitHub traffic stats in terminal for all repos at once.

## Getting Started

You need to create `src/ghview/config.clj` with following content

```clojure
(ns ghview.config)

(def token "your github token")
(def auth {"Authorization" (clojure.string/join ["token " token])})
(def userUrl "https://api.github.com/users/yourName")
```

Now, it's ready to use:

```shell
$ lein run        # shows your view stats
$ lein run views  # same
$ lein run clones # shows your clone stats
```

