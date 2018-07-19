# patch-tender
ClojureScript [JIRA](https://dev.clojure.org/jira/browse/CLJS) contains many candidate patches that have not yet been applied to master.
The `patch-tender` project maintains and applies a curated set of these patches in a public stable GitHub branch so they can be easily soak-tested in downstream projects.

The latest set of [applied patches](https://github.com/clojure/clojurescript/compare/master...mfikes:patch-tender-2018-07-19T17) as of 2018-07-19 are in [this branch](https://github.com/mfikes/clojurescript/commits/patch-tender-2018-07-19T17).

Branch build status: [![Build Status](https://travis-ci.org/mfikes/clojurescript.svg?branch=patch-tender-2018-07-19T17)](https://travis-ci.org/mfikes/clojurescript) [![Build status](https://ci.appveyor.com/api/projects/status/oggs1yydb8c2t6pa/branch/patch-tender-2018-07-19T17?svg=true)](https://ci.appveyor.com/project/mfikes/clojurescript/branch/patch-tender-2018-07-19T17)

If using `deps.edn` you can depend on this set of patches via
```clojure
org.clojure/clojurescript {:git/url "https://github.com/mfikes/clojurescript"
                           :sha "e6768a40b1c9929d7e86b259c0aa46ce39d69235"} 
```

Or you can clone and build this branch for use in a `lein`- or `boot`-based project:

```
$ git clone https://github.com/mfikes/clojurescript -b patch-tender-2018-07-19T17
$ cd clojurescript
$ script/build
```
For more info see https://clojurescript.org/community/building