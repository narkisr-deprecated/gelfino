(ns gelfino.test.drools.dsl
  (:use clojure.test gelfino.drools.dsl))

(def-rulestream infos
  (import- gelfino.drools.dsl.Message)
  (declare Message :role event) 
  (rule info-messages
        (when message :of-type Message 
          (= level "INFO" ) :from (entry-point "event-stream"))
        (then System.out.println "Rule 1")))

(deftest import-single
  (let [imps (-> infos (.getDescr) (.getImports)) m-imp (bean (first imps ))]
    (is (= (m-imp :target) "gelfino.drools.dsl.Message"))))

(bean (get-in  (bean (first (-> infos (.getDescr) (.getTypeDeclarations)) )) [:annotations "role"]))

(deftest declare-single
  (let [types (-> infos (.getDescr) (.getTypeDeclarations)) {:keys [annotations typeName]} (bean (first types))
        {:keys [name value]} (bean (get annotations "role"))]
    (is (= 1 (count types)))
    (is (= "Message" typeName))
    (is (= "role" name))
    (is (= "event" value))
    ))

#_(pprint (macroexpand-1
            '(def-rulestream infos
               (import- gelfino.drools.Message)
               (declare Message :role event) 
               (rule info-messages
                     (when message :of-type Message 
                       (= level "INFO" ) :from (entry-point "event-stream"))
                     (then System.out.println "Rule 1")))))


