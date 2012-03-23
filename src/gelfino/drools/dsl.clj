(ns gelfino.drools.dsl
  (:use clojure.pprint 
        (clojure (string :only [split])))
  (:import [org.drools.lang.api DescrFactory]))


(defmacro d-> [target & elements]
  "Threading macro with default end call"
  `(-> ~target ~@elements (.end)))

(defn type- [dec- v]
  (-> dec- (.type) (.name v))) 

(defn annotation [dec- k v]
  (d-> dec- (.newAnnotation k) (.value v)))

(defn declare- [p t role]
  `(d-> (.newDeclare ~p) (type- ~t) (annotation "role" ~role )))

(defn imports [pkg entries]
  (map #(list 'd-> pkg '(.newImport) (list '.target (str %))) entries))

(defn to-infix [[pred l r]]
   (str l pred  r))

#_(when message :of-type Message 
  (= level "INFO" ) :from (entry-point "event-stream"))

(defn lhs [[_ ident _ type- c _ stream]]
   `(d->
      (d-> (.lhs) (.pattern ~(str type-)) (.id ~(str ident) true)
       (d-> (.constraint ~(-> c to-infix str))))))

(defn rules [dcl n l-exp r-exp ]
  `(d-> ~dcl (.newRule) (.name ~n) ~(lhs l-exp)))

(defmacro def-rulestream [[_ & imps] [_ t _ role] [_ n when- then]]
  (let [package (gensym "package") with-imports (gensym "with-imports") 
        with-dec (gensym "with-dec") with-rules (gensym "with-rules")]; see http://bit.ly/GGlTuh 
    `(let [~package (.name (DescrFactory/newPackage) "gelfino.streams") 
           ~with-imports ~@(imports package imps)
           ~with-dec ~(declare- with-imports (str t) (str role))
           ~with-rules ~(rules with-dec (str n) when- then)
           ] 
       (identity ~with-rules))))

#_(pprint (macroexpand-1
            '(def-rulestream 
               (import- gelfino.drools.Message)
               (declare Message :role event) 
               (rule info-messages
                     (when message :of-type Message 
                       (= level "INFO" ) :from (entry-point "event-stream"))
                     (then System.out.println "Rule 1")))))


(println 
  ( -> (def-rulestream 
         (import- gelfino.drools.Message)
         (declare Message :role event) 
         (rule info-messages
           (when message :of-type Message 
               (= level "INFO" ) :from (entry-point "event-stream"))
           (then System.out.println "Rule 1")))
  (.getDescr) (.getImports)))