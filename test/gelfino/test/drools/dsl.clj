(ns gelfino.test.drools.dsl
  (:import [gelfino.drools.bridging Message] 
            java.util.concurrent.TimeUnit)
  (:use clojure.test 
        gelfino.drools.dsl gelfino.drools.bridging 
        gelfino.drools.straping clojure.java.data))

(def result (ref {:infos false :four-errors false}))

(def-rulestream infos
  (rule info-messages
     (when $message :> Message (== level "INFO" ) 
       :from (entry-point "event-stream"))
     (then 
       (dosync 
         (alter result assoc :infos true)))))

(def-rulestream four-errors
  (rule info-messages
     (when Number (> intValue 3) :from 
       (accumulate $message :> Message (== level "ERROR") :over (window :time 1 m)
       :from (entry-point event-stream) (count $message)))
     (then 
       (dosync 
         (alter result assoc :four-errors true)))))

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

(deftest infos-session-run 
  (let [session (drools-session :pkg infos) 
        entry (.getWorkingMemoryEntryPoint session "event-stream")]
    (.insert entry (Message. "INFO" 123))
    (.insert entry (Message. "bla" 124))
    (.fireAllRules session))
  (is (= (@result :infos) true)))

(deftest infos-session-run 
  (let [session (drools-session :pkg four-errors :clock "pseudo" ) 
        clock (.getSessionClock session) 
        now (.getTime (java.util.Date.))
        entry (.getWorkingMemoryEntryPoint session "event-stream")]
    ;(.insert entry (Message. "INFO" now ))
    (.insert entry (Message. "ERROR" now))
    (.insert entry (Message. "ERROR" (+ 4000 now)))
    (.insert entry (Message. "INFO" (+ 5000 now)))
    (.insert entry (Message. "ERROR" (+ 6000 now)))
    (.insert entry (Message. "ERROR" (+ 7000 now)))
    (.advanceTime clock 10 TimeUnit/SECONDS)
    (.fireAllRules session))
  (is (= (@result :four-errors) true)))

(deftest level-lhs
  (let [{{[{constraint :constraint {entry :entryId} :source}] :descrs} :lhs} rules-map
        {[{exp :expression}]:descrs} constraint]
    (is (= "level==\"INFO\"" exp))
    (is (= "event-stream" entry))))

