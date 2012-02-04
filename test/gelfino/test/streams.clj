(ns gelfino.test.streams
  (:use clojure.test gelfino.streams))

(deftest pred-conversion
   (let [empty-fn #() regex #"foo" string "bar"]
      (is (= (into-pred empty-fn) empty-fn))
      (is (= ((into-pred regex) "foo") true))
      (is (= ((into-pred string) "barbar") true))
      
      ))

(deftest simple-stream
  (defstream not-too-long :short-message #" not too long " (println message))     
   
  )

