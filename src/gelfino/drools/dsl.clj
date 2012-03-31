(ns gelfino.drools.dsl
  (:use 
    [clojure.string :only [split]]
    [clojure.core.match :only [match]]
    clojure.core.strint
    gelfino.drools.bridging)
  (:import
    java.io.StringReader
    [org.drools.lang.api DescrFactory]
    [org.drools.compiler DrlParser]))

(defn to-infix [[pred l r]]
  "turns clojure prefix notation to infix not recursive"
   (list l pred r))

(defn register-action [n [_then_ & body]]
  `(dosync (alter actions assoc ~(str n) #(do ~@body))))

(defn rule-body [sname l-exp]
  (<< "
import gelfino.drools.bridging.Message

global clojure.lang.IDeref actions

dialect \"mvel\"

declare Message
 @role(event)
 @timestamp(datetime)
 @typesafe(false)
end

rule \"~{sname}\" 
when
 ~{l-exp}
then
 actions.deref().get(\"~{sname}\").invoke();
end"
       ))

(defn- validate [parser error]
  (when (.hasErrors parser) 
    (println (.toString (.getErrors parser)))
    (throw (RuntimeException. error))) )

(defn parse-rule [text]
  (let [parser (DrlParser.) reader (StringReader. text) result (.parse parser reader)]
    (validate parser (<< "failed to parse ~{text}"))
    result
    ))

(defn lhs [body]
  "converting an s-exp to drl lhs-exp"
  (match [body]
     [(['when & r] :seq)] (lhs r)
     [([v ':of-type t & r] :seq)] (<< "$~{v}:~{t}~(lhs r)")
     [([exp ':from point & r] :seq)] (<< "~(lhs exp) from ~(lhs point) ~(lhs r)")
     [(['== f s] :seq) :as c] (<< "(~(reduce str (map pr-str (to-infix c))))")
     [(['entry-point point & r] :seq)] (<< "entry-point \"~{point}\"~(lhs r)")
     :else ""
    ))


(defmacro def-rulestream [sname  [_ t _ role] [_ n _when then]]
  `(do 
     ~(register-action sname then)
     (def ~sname (parse-rule (rule-body ~(str sname) ~(lhs _when))))))
