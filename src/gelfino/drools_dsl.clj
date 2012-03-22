(ns gelfino.drools-dsl
  (:use clojure.pprint 
        (clojure (string :only [split])))
  (:import [org.drools.lang.api DescrFactory]))


(defmacro d-> [target & elements]
  "Threading macro with default end call"
  `(-> ~target ~@elements (.end)))

(defn type- [dec- v]
  (-> dec- (.type) (.name (-> v symbol str)))) 

(defn annotation [t k v]
  (d-> t (.keyValue k v)))

(defn declare- [p t role]
  `(d-> (.newDeclare ~p) (type- ~t) (annotation "role" (-> ~role symbol str))))

(defn imports [pkg entries]
  (map #(list 'd-> pkg '(.newImport) (list '.target (str %))) entries))

(defmacro def-rulestream [[_ & imps] [_ t _ role] rule]
  (let [package (gensym "package") with-imports (gensym "with-imports")]; see http://bit.ly/GGlTuh 
    `(let [~package (.name (DescrFactory/newPackage) "gelfino.streams") 
           ~with-imports ~@(imports package imps)
           inc-dec# ~(declare- with-imports t role)
           ] 
       (identity inc-dec#)
       )))

#_(pprint (macroexpand
            '(def-rulestream 
               (import- gelfino.drools.Message)
               (declare Message :role event) 
               (rule info-messages
                     (when message :of-type Message 
                       (= level "INFO" ) :from (entry-point "event-stream"))
                     (then System.out.println "Rule 1")))))


#_(println 
  ( -> (def-rulestream 
         (import- gelfino.drools.Message)
         (declare Message :role event) 
         (rule info-messages
               (when message :of-type Message 
                 (= level "INFO" ) :from (entry-point "event-stream"))
               (then System.out.println "Rule 1")))
    (.getDescr) (.getImports)))
