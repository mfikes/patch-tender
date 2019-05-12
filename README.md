# Patch Tender

ClojureScript [JIRA](https://dev.clojure.org/jira/browse/CLJS) contains many candidate patches that have not yet been applied to master.
The Patch Tender project maintains and applies a curated set of these patches in a public stable GitHub branch so they can be easily soak-tested in downstream projects.

The latest set of [applied patches](https://github.com/clojure/clojurescript/compare/master...mfikes:patch-tender-2019-05-12T01) as of 2019-05-12 are in [this branch](https://github.com/mfikes/clojurescript/commits/patch-tender-2019-05-12T01).

Branch build status: [![Build Status](https://travis-ci.org/mfikes/clojurescript.svg?branch=patch-tender-2019-05-12T01)](https://travis-ci.org/mfikes/clojurescript) [![Build status](https://ci.appveyor.com/api/projects/status/oggs1yydb8c2t6pa/branch/patch-tender-2019-05-12T01?svg=true)](https://ci.appveyor.com/project/mfikes/clojurescript/branch/patch-tender-2019-05-12T01)

If using `deps.edn` you can depend on this set of patches via
```clojure
org.clojure/clojurescript {:git/url "https://github.com/mfikes/clojurescript" :sha "cddde28e7045f88cd4af5a70ba398fcc596bbc8a"}
```

or you can clone and build this branch for use in a `lein`- or `boot`-based project:

```
$ git clone https://github.com/mfikes/clojurescript -b patch-tender-2019-05-12T01
$ cd clojurescript
$ script/build
```
For more info see [Building the compiler](https://clojurescript.org/community/building).