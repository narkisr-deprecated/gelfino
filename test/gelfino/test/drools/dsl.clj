(ns gelfino.test.drools.dsl
  (:import [gelfino.drools.bridging Message])
  (:use clojure.pprint clojure.test 
        gelfino.drools.dsl gelfino.drools.bridging 
        gelfino.drools.straping clojure.java.data))

(def result (atom false))

(def-rulestream infos
  (rule info-messages
        (when message :> Message (== level "INFO" ) :from (entry-point "event-stream"))
        (then (reset! result true))))

(deftest import-single
  (let [imps (-> infos (.getImports)) m-imp (bean (first imps ))]
    (is (= (m-imp :target) "gelfino.drools.bridging.Message"))))

(deftest declare-single
  (let [types (-> infos (.getTypeDeclarations)) {:keys [annotations typeName]} (bean (first types))
        {:keys [name value]} (bean (get annotations "role"))]
    (is (= 1 (count types)))
    (is (= "Message" typeName))
    (is (= "role" name))
    (is (= "event" value))))

(def rules-map (from-java (first (-> infos (.getRules)))))


(deftest static-rhs 
  (let [{consequence :consequence } rules-map {action "infos"} @actions]
    (is (= "actions.deref().get(\"infos\").invoke();\n" consequence))
    (is (not (nil? action )))))

(deftest session-run 
  (let [session (build-gelfino-session infos) entry (.getWorkingMemoryEntryPoint session "event-stream")]
    (.insert entry (Message. "INFO" 123))
    (.insert entry (Message. "bla" 124))
    (.fireAllRules session))
    (is (= @result true)))

(deftest level-lhs
  (let [{{[{constraint :constraint {entry :entryId} :source}] :descrs} :lhs} rules-map
        {[{exp :expression}]:descrs} constraint]
    (is (= "level==\"INFO\"" exp))
    (is (= "event-stream" entry))))

(deftest simple-lhs 
   (is (= (lhs '(when message :> Message (== level "INFO" ) :from (entry-point "event-stream")))
         "$message:Message(level==\"INFO\") from entry-point \"event-stream\" ")))

#_(deftest complex-lhs 
  (is (= (lhs (when (Number (> intValue 3)) :from 
           (accumulate message :of-type Message(== level "INFO") :over 
             (window:time 1 m) :from entry-point entryone , (count $message))))
    "Number(intValue > 3) from accumulate($message:Message(level == \"INFO\") over window:time(1m) from entry-point entryone, count($message))")))

; Number(intValue > 3) from accumulate($message:Message(level == "INFO") over window:time(1m) from entry-point entryone, count($message))
; ((Number intValue > 3) :from (accumulate $message :of-type Message (== level "INFO") :over (window:time 1m) :from entry-point entryone  (count $message)))


