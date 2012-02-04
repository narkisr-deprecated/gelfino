(ns gelfino.test.streams
  (:require [lamina.core :as lam])
  (:use clojure.test gelfino.streams))

(deftest pred-conversion
   (let [empty-fn #() regex #"foo" string "bar"]
      (is (= (into-pred empty-fn) empty-fn))
      (is (= ((into-pred regex) "foo") true))
      (is (= ((into-pred string) "barbar") true))
      (is (thrown? java.lang.Exception (into-pred 1))) 
      ))

(deftest filtering 
  (let [messages [{:short-message "foo"} {:short-message "bar"} {:short-message "hello"}]]
    (is (= (filter (filter-fn [:short-message "fo"]) messages) '({:short-message "foo"})))         
    (is (= (filter (filter-fn [:short-message #"foo"]) messages) '({:short-message "foo"})))
    (is (= (filter (filter-fn [:short-message #(= % "bar")]) messages) '({:short-message "bar"})))
    ))

(deftest simple-stream
  (defstream not-too-long :short-message #"foo" (fn [m] (println m)))     
  (is (= (keys @stream-channels) '(:not-too-long)))
  (initialize-channels)
  (lam/enqueue (@base-channels :output) "{\"short-message\" : \"foo\"}")
  #_(lam/receive (@stream-channels :not-too-long) #(println %))
  )

