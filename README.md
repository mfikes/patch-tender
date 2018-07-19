# patch-tender
ClojureScript [JIRA](https://dev.clojure.org/jira/browse/CLJS) contains many candidate patches that have not yet been applied to master.

`patch-tender` is used to maintain a curated list of patches, applying them in a branch so they can be soak-tested.

The latest set of applied patches are in this branch 

   https://github.com/mfikes/clojurescript/commits/patch-tender-2018-07-19T15 

Branch build status: [![Build Status](https://travis-ci.org/mfikes/clojurescript.svg?branch=patch-tender-2018-07-19T15)](https://travis-ci.org/mfikes/clojurescript) [![Build status](https://ci.appveyor.com/api/projects/status/oggs1yydb8c2t6pa/branch/patch-tender-2018-07-19T15?svg=true)](https://ci.appveyor.com/project/mfikes/clojurescript/branch/patch-tender-2018-07-19T15)

If using `deps.edn` you can depend on this set of patches via
```clojure
org.clojure/clojurescript {:git/url "https://github.com/mfikes/clojurescript", :sha "3cf246ac1716a10b2dc4b145297d53c66100de31"}
```

Or you can clone and build this branch for use in a `lein`- or `boot`-based project:

```
$ git clone https://github.com/mfikes/clojurescript -b patch-tender-2018-07-19T15
$ cd clojurescript
$ script/build
```
For more info see https://clojurescript.org/community/building