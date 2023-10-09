(ns redimize.core
  (:require [clojure.tools.logging :as log]
            [taoensso.carmine :as car]
            [clojure.core.memoize :as cm]))

(defn check-opts [{:keys [pool host port]}]
  {:pool (or pool (car/connection-pool {}))
   :spec {:host (or host (:host (car/make-conn-spec)))
          :port (or port (:port (car/make-conn-spec)))}})

(defn to-redis
  [my-wcar-opts f & {:keys [key expire]}]
  {:pre [(string? key)]}
  (fn [& args]
    (let [expire (if (not expire) 60 expire)
          ret (apply f args)
          my-wcar-opts (check-opts my-wcar-opts)]
      (let [memo-key (str key ":" (pr-str args))]
        (try
          (if-let [val (car/wcar my-wcar-opts (car/get memo-key))]
            val
            (do
              (car/wcar my-wcar-opts (car/set memo-key ret))
              (when (not= -1 expire) (car/wcar my-wcar-opts (car/expire memo-key expire)))
              ret))
          (catch Exception e
            (log/error e)
            ret))))))

(defn dual-memo
  [my-wcar-opts f & {:keys [key expire]}]
  {:pre [(string? key)]}
  (let [expire (if (not expire) 60 expire)]
    (if (= -1 expire)
      (cm/memo (to-redis my-wcar-opts f :key key :expire expire))
      (cm/ttl
        (to-redis my-wcar-opts f :key key :expire expire)
        :ttl/threshold (* expire 1000)))))

(comment
  (defn slowly [n]
    (Thread/sleep 5000)
    n)

  (def conn {:host "127.0.0.1", :port 6379})

  (def memoized-test (dual-memo nil slowly :key "test-1" :expire -1))
  (def memoized-test1 (dual-memo conn slowly :key "test09" :expire 9))
  (def memoized-test2 (dual-memo conn slowly :key "test60" :expire 60))

  (time (prn (memoized-test -1)))
  (time (prn (memoized-test1 9)))
  (time (prn (memoized-test2 60))))
