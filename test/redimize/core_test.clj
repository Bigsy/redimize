(ns redimize.core-test
  (:require [clojure.java.io :as io]
            [redimize.core :as red]
            [clojure.test :refer [deftest is testing use-fixtures]]
            [redis-embedded-clj.core :as sut]
            [taoensso.carmine :as car]))

(use-fixtures :once sut/with-rd-fn)

(defn around-all
  [f]
  (sut/with-rd-fn {:port 6379} f))

(use-fixtures :once around-all)

(def conn {:host "127.0.0.1", :port 6379})
(def conn-broken {:host "127.0.0.1", :port 6377})


(defn slowly [n]
  (Thread/sleep 500)
  n)

(def memoized-test (red/dual-memo conn slowly :keyprefix "test-1" :expire -1))
(def memoized-test2 (red/dual-memo conn-broken slowly :keyprefix "test-2" :expire -1))
(def memoized-test3 (red/dual-memo conn slowly :keyprefix "test-3" :expire 1))
(def memoized-test4 (red/dual-memo conn slowly :expire 1))
(def memoized-test5 (red/dual-memo conn slowly :expire 0))





(deftest can-wrap-around
  (let [start (System/currentTimeMillis)]
    (is (= -1 (memoized-test -1)))
    (let [end (System/currentTimeMillis)]
      (is (< 500 (- end start))))

    (is (= "-1" (car/wcar (red/check-opts conn) (car/get "test-1:redimize.core_test$slowly:(-1)"))))
    (is (= -1 (car/wcar (red/check-opts conn) (car/ttl "test-1:redimize.core_test$slowly:(-1)")))))

  (let [start (System/currentTimeMillis)]
    (is (= -1 (memoized-test -1)))
    (let [end (System/currentTimeMillis)]
      (is (> 2 (- end start))))))


(deftest dont-blow-up-if-redis-down-use-first-cache
  (let [start (System/currentTimeMillis)]
    (is (= -1 (memoized-test2 -1)))
    (let [end (System/currentTimeMillis)]
      (is (< 500 (- end start)))))

  (is (= nil (car/wcar (red/check-opts conn) (car/get "test-2:redimize.core_test$slowly:(-1)"))))
  (is (= -2 (car/wcar (red/check-opts conn) (car/ttl "test-2:redimize.core_test$slowly:(-1)"))))


  (let [start (System/currentTimeMillis)]
    (is (= -1 (memoized-test2 -1)))
    (let [end (System/currentTimeMillis)]
      (is (> 2 (- end start))))))

(deftest check-expiry
  (let [start (System/currentTimeMillis)]
    (is (= 1 (memoized-test3 1)))
    (let [end (System/currentTimeMillis)]
      (is (< 500 (- end start))))

    (is (= "1" (car/wcar (red/check-opts conn) (car/get "test-3:redimize.core_test$slowly:(1)"))))
    (is (= 1 (car/wcar (red/check-opts conn) (car/ttl "test-3:redimize.core_test$slowly:(1)")))))

  (Thread/sleep 1000)

  (let [start (System/currentTimeMillis)]
    (is (= 1 (memoized-test3 1)))
    (let [end (System/currentTimeMillis)]
      (is (< 500 (- end start))))))

(deftest no-prefix
  (let [start (System/currentTimeMillis)]
    (is (= -1 (memoized-test4 -1)))
    (let [end (System/currentTimeMillis)]
      (is (< 500 (- end start))))

    (is (= "-1" (car/wcar (red/check-opts conn) (car/get "redimize.core_test$slowly:(-1)"))))
    (is (= 1 (car/wcar (red/check-opts conn) (car/ttl "redimize.core_test$slowly:(-1)")))))

  (let [start (System/currentTimeMillis)]
    (is (= -1 (memoized-test4 -1)))
    (let [end (System/currentTimeMillis)]
      (is (> 2 (- end start))))))

(deftest zero-expire
  (let [start (System/currentTimeMillis)]
    (is (= -1 (memoized-test5 -1)))
    (let [end (System/currentTimeMillis)]
      (is (< 500 (- end start))))

    (is (= nil (car/wcar (red/check-opts conn) (car/get "redimize.core_test$slowly:(-1)"))))
    (is (= -2 (car/wcar (red/check-opts conn) (car/ttl "redimize.core_test$slowly:(-1)")))))

  (let [start (System/currentTimeMillis)]
    (is (= -1 (memoized-test5 -1)))
    (let [end (System/currentTimeMillis)]
      (is (<= 500 (- end start))))))
