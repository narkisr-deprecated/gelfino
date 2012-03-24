(ns gelfino.test.drools.dsl
  (:use clojure.pprint clojure.test gelfino.drools.dsl clojure.java.data))

(def-rulestream infos
  (import- gelfino.drools.dsl.Message)
  (declare Message :role event) 
  (rule info-messages
        (when message :of-type Message 
          (== level "INFO" ) :from (entry-point "event-stream"))
        (then System.out.println "Rule 1")))

(deftest import-single
  (let [imps (-> infos (.getDescr) (.getImports)) m-imp (bean (first imps ))]
    (is (= (m-imp :target) "gelfino.drools.dsl.Message"))))


(deftest declare-single
  (let [types (-> infos (.getDescr) (.getTypeDeclarations)) {:keys [annotations typeName]} (bean (first types))
        {:keys [name value]} (bean (get annotations "role"))]
    (is (= 1 (count types)))
    (is (= "Message" typeName))
    (is (= "role" name))
    (is (= "event" value))))

;(pprint (from-java (first (-> infos (.getDescr) (.getRules)))))

(deftest simple-lhs
  (let [{{[{constraint :constraint {entry :entryId} :source}] :descrs} :lhs} (from-java (first (-> infos (.getDescr) (.getRules))))
        {[{exp :expression}]:descrs} constraint ]
    (is (= "level==INFO" exp))
    (is (= "event-stream" entry))
    ))

#_(pprint (macroexpand-1
            '(def-rulestream infos
               (import- gelfino.drools.Message)
               (declare Message :role event) 
               (rule info-messages
                     (when message :of-type Message 
                       (== level "INFO" ) :from (entry-point "event-stream"))
                     (then System.out.println "Rule 1")))))


