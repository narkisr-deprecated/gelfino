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

(def keywords #{'Number})

(defn operator?[o] (#{'< '> '<= '>= '==} o))

(defn accumulate-fn [f] (#{'average 'min 'max 'count 'sum 'collectList 'collectSet}))

(defn window [exp]
  "windows s-exp to l-exp http://tinyurl.com/d8ya6dn" 
  (match [exp]
    [(['window :time t unit] :seq)] (<< "window:time(~(t)~(unit))")
    [(['window :length l] :seq)] (<< "window:length(~(l))")))

(defn is-type? [t] (not (nil? (re-find #"[A-Z][a-z]*" (str t)))))

(defn lhs [body]
  "converting an s-exp to drl lhs-exp see http://tinyurl.com/d7hpovl"
  (match [body]
     [(['when & r] :seq)] (lhs r)
     [([bind ':> (t :when is-type?) & r] :seq)] (<< "~{bind}:~{t}~(lhs r)"); pattern with bind
     [([exp ':from dest & r] :seq)] (<< "~(lhs exp) from ~(lhs dest) ~(lhs r)")
     [([:over w & r] :seq)] (<< "~(window w) ~(lhs r)")
     [([(o :when operator?) f s] :seq) :as c] (<< "(~(reduce str (map pr-str (to-infix c))))")
     [(['entry-point point & r] :seq)] (<< "entry-point \"~{point}\"~(lhs r)")
     :else ""
    ))


(defmacro def-rulestream [sname [_ n _when then]]
  `(do 
     ~(register-action sname then)
     (def ~sname 
       (parse-rule 
         (rule-body ~(str sname) ~(lhs _when))))))
