(ns gelfino.test.drools.lhs
  (:use clojure.test gelfino.drools.dsl))

(deftest simple-lhs 
   (is (= (lhs '(when $message :> Message (== level "INFO" ) :from (entry-point "event-stream")))
         "$message:Message(level==\"INFO\") from entry-point \"event-stream\" ")))

(deftest complex-lhs 
  (is (= (lhs '(when Number (> intValue 3) :from 
                (accumulate $message :> Message (== level "INFO") :over (window :time 1 m)
                  :from (entry-point event-stream)  (count $message))))
    "Number(intValue>3) from accumulate($message:Message(level==\"INFO\") over window:time(1m) from entry-point \"event-stream\" ,count($message)) ")))


