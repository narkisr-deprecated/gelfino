(ns gelfino.test.drools.dsl
  (:import [gelfino.drools.bridging Message])
  (:use clojure.pprint clojure.test 
        gelfino.drools.dsl gelfino.drools.bridging 
        gelfino.drools.straping clojure.java.data))

(def-rulestream infos
  (declare Message :role event) 
  (rule info-messages
        (when message :of-type Message 
          (== level "INFO" ) :from (entry-point "event-stream"))
        (then (println "fired by rule 1"))))

(deftest import-single
  (let [imps (-> infos (.getDescr) (.getImports)) m-imp (bean (first imps ))]
    (is (= (m-imp :target) "gelfino.drools.bridging.Message"))))


(deftest declare-single
  (let [types (-> infos (.getDescr) (.getTypeDeclarations)) {:keys [annotations typeName]} (bean (first types))
        {:keys [name value]} (bean (get annotations "role"))]
    (is (= 1 (count types)))
    (is (= "Message" typeName))
    (is (= "role" name))
    (is (= "event" value))))

;(pprint (from-java (-> infos (.getDescr))))

(def rules-map (from-java (first (-> infos (.getDescr) (.getRules)))))

(deftest simple-lhs
  (let [{{[{constraint :constraint {entry :entryId} :source}] :descrs} :lhs} rules-map
        {[{exp :expression}]:descrs} constraint]
    (is (= "level==\"INFO\"" exp))
    (is (= "event-stream" entry))
    ))

(deftest static-rhs 
  (let [{consequence :consequence } rules-map {action "info-messages"} @actions]
    (is (= "actions.deref().get(\"info-messages\").invoke();" consequence))
    (is (not (nil? action )))))

(deftest session-run 
  (let [session (build-gelfino-session (.getDescr infos)) entry (.getWorkingMemoryEntryPoint session "event-stream")]
    (.insert entry (Message. "INFO" ""))
    (.insert entry (Message. "bla" ""))
    (.fireAllRules session)))

#_(pprint (macroexpand-1
            '(def-rulestream infos
               (import- gelfino.drools.bridging.Message)
               (declare Message :role event) 
               (rule info-messages
                     (when message :of-type Message 
                       (== level "INFO" ) :from (entry-point "event-stream"))
                     (then (println "Rule 1"))))))


