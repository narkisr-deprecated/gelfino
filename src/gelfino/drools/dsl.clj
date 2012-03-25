(ns gelfino.drools.dsl
  (:use (clojure (string :only [split])) gelfino.drools.bridging)
  (:import [org.drools.lang.api DescrFactory]))

(defmacro d-> [target & elements]
  "Threading macro with default end call"
  `(-> ~target ~@elements (.end)))

(defn type- [dec- v]
  (-> dec- (.type) (.name v))) 

(defn annotation [dec- k v]
  (d-> dec- (.newAnnotation k) (.value v)))

(defn declare- [p t role]
  `(d-> (.newDeclare ~p) (type- ~t) (annotation "role" ~role)))

#_(defn imports [pkg entries]
  (map #(list 'd-> pkg '(.newImport) (list '.target (str %))) entries))

(defn message-import [pkg]
  (d-> pkg (.newImport) (.target "gelfino.drools.bridging.Message")))

(defn dialect [pkg]
  (d-> pkg (.attribute "dialect") (.value "mvel")))

(defn actions-global [pkg]
  (d-> pkg (.newGlobal) (.type "clojure.lang.IDeref") (.identifier "actions")))

(defn to-infix [[pred l r]]
  "turns clojure prefix notation to infix not recursive"
   (list l pred r))

(defn rhs [n [_then_ & body]]
  `(-> (.rhs  ~(str "actions.deref().get(\"" n "\").invoke();"))))

(defn lhs [[_ ident _type_ type- c _from_ [_entry_ stream]]]
  "lhs is drl when statement"
   `(d-> (.lhs) 
      (d-> (.pattern ~(str type-)) 
           (.id ~(str ident) true) 
           (.constraint ~(reduce str (map pr-str (to-infix c))))
           (.from) (.entryPoint ~stream))))

(defn register-action [n [_then_ & body]]
  `(dosync (alter actions assoc ~(str n) #(do ~@body))))

(defn rules [dcl n l-exp r-exp]
  `(do 
     ~(register-action n r-exp)
     (d-> ~dcl (.newRule) (.name ~n) ~(lhs l-exp) ~(rhs n r-exp))))

(defmacro def-rulestream [sname  [_ t _ role] [_ n when- then]]
  (let [package (gensym "package") 
         with-dec (gensym "with-dec") with-rules (gensym "with-rules")]; see http://bit.ly/GGlTuh 
    `(def ~sname
       (let [~package (.name (DescrFactory/newPackage) "gelfino.streams") 
             ~with-dec ~(declare- package (str t) (str role))
             ~with-rules ~(rules with-dec (str n) when- then)] 
         (-> ~with-rules message-import actions-global dialect)))))


