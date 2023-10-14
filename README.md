# redimize

[![Clojars Project](https://img.shields.io/clojars/v/org.clojars.bigsy/redimize.svg)](https://clojars.org/org.clojars.bigsy/redimize)

A Clojure library that provides two level caching to core.memoize then redis on cache miss

## Usage
Expire is in seconds ff no value passed defaults to 1 hour, passing -1 expiry disables ttl

By default the redis key will the fully qualified function name followed by args

`"redimize.core_test$slowly:(-1)"`

You can pass an optional prefix if you need to disambiguate

``` clojure
(:require [redimize.core :as red])

(defn slowly [n]
  (Thread/sleep 5000)
  n)
  
(def conn {:host "127.0.0.1", :port 6379})

(def memoized-no-key (red/dual-memo conn slowly :expire -1))
(def memoized-test   (red/dual-memo conn slowly :keyprefix "test-1" :expire -1))
(def memoized-test1  (red/dual-memo conn slowly :keypreifx "test09" :expire 9))
(def memoized-test2  (red/dual-memo conn slowly :keyprefix "test60" :expire 60))
(def memoized-1-hour (red/dual-memo conn slowly))


(time (prn (memoized-no-prefix -1)))
(time (prn (memoized-test -1)))
(time (prn (memoized-test1 9)))
(time (prn (memoized-test2 60)))
(time (prn (memoized-1-hour 60)))
```

## License

Copyright © 2023 FIXME

This program and the accompanying materials are made available under the
terms of the Eclipse Public License 2.0 which is available at
http://www.eclipse.org/legal/epl-2.0.

This Source Code may also be made available under the following Secondary
Licenses when the conditions for such availability set forth in the Eclipse
Public License, v. 2.0 are satisfied: GNU General Public License as published by
the Free Software Foundation, either version 2 of the License, or (at your
option) any later version, with the GNU Classpath Exception which is available
at https://www.gnu.org/software/classpath/license.html.
