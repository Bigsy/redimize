(ns redimize.core
  (:require [clojure.tools.logging :as log]
            [taoensso.carmine :as car]
            [clojure.core.memoize :as cm]))

(defn check-opts [{:keys [pool host port spec]}]
  {:pool (or pool (car/connection-pool {}))
   :spec (or spec {:host (or host (:host (car/make-conn-spec)))
                   :port (or port (:port (car/make-conn-spec)))})})

(def ret (atom nil))

(defn to-redis
  [my-wcar-opts f & {:keys [keyprefix expire]}]
  (fn [& args]
    (let [expire (if (not expire) 60 expire)
          _ (reset! ret nil)
          my-wcar-opts (check-opts my-wcar-opts)]
      (let [memo-key (str (pr-str (type f)) ":" (pr-str args))
            memo-key (if keyprefix (str keyprefix ":" memo-key) memo-key)]
        (try
          (if-let [val (car/wcar my-wcar-opts (car/get memo-key))]
            val
            (let [retv (apply f args)
                  _ (reset! ret retv)]
              (car/wcar my-wcar-opts (car/set memo-key retv))
              (when (not= -1 expire) (car/wcar my-wcar-opts (car/expire memo-key expire)))
              retv))
          (catch Exception e
            (log/error e)
            (if @ret @ret (apply f args))))))))

(defn dual-memo
  [my-wcar-opts f & {:keys [keyprefix expire]}]
  (let [expire (if (not expire) 60 expire)]
    (if (= -1 expire)
      (cm/memo (to-redis my-wcar-opts f :keyprefix keyprefix :expire expire))
      (cm/ttl
        (to-redis my-wcar-opts f :keyprefix keyprefix :expire expire)
        :ttl/threshold (* expire 1000)))))
