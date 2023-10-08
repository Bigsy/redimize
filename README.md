# redimize

[![Clojars Project](https://img.shields.io/clojars/v/org.clojars.bigsy/redimize.svg)](https://clojars.org/org.clojars.bigsy/redimize)

A Clojure library that provides two level caching to core.memoize then redis on cache miss

## Usage
If no expire value passed defaults to 1 hour, passing -1 expiry disables ttl
``` 
(defn slowly [n]
  (Thread/sleep 5000)
  n)
  
(def conn {:host "127.0.0.1", :port 6379})

(def memoized-test (dual-memo nil slowly :key "test-1" :expire -1))
(def memoized-test1 (dual-memo conn slowly :key "test09" :expire 9))
(def memoized-test2 (dual-memo conn slowly :key "test60" :expire 60))

(time (prn (memoized-test -1)))
(time (prn (memoized-test1 9)))
(time (prn (memoized-test2 60)))
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
