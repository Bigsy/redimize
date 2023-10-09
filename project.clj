(defproject org.clojars.bigsy/redimize "0.1.1-SNAPSHOT"
  :description "two level memoize redis caching in clojure"
  :url "https://github.com/Bigsy/redimize"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.11.1"]
                 [com.taoensso/carmine "3.3.0-RC1"]
                 [org.clojure/core.memoize "1.0.257"]]

  :profiles {:dev {:dependencies [[org.clojars.bigsy/redis-embedded-clj "0.0.1-SNAPSHOT"]]}}

  :repl-options {:init-ns redimize.core})
