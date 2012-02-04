(ns gelfino.streams
  (:require [gelfino.core :as gelfino-c]))

(defn into-pred [selector]
  (cond 
    (instance? clojure.lang.IFn selector) selector
    (instance? java.util.regex.Pattern selector) (fn [v] (-> (re-matches selector v) nil? not))
    (instance? java.lang.String selector) (fn [v] (.contains v selector))
    :else (throw (Exception.(str "Bad predicate type " selector)))))

(defn filter-fn [pred-pairs]
  (let [pairs  (map #(into [] % ) (partition 2 pred-pairs))
        preds (map (fn [[k v]] [k (into-pred v)]) pairs)]
     (fn [m]
      (every? (fn [[k v]] (v (m k))) preds))))

(defmacro defstream
  "A stream of messages filtered out of the entire messages recieved 
   the defenition takes pairs of key values where key is the message part we filter upon and the value is either:
    * A predicate function that accepts the part and returns true if it macthes.
    * A regex on which the parts will be matched.
    * A substring of the matched message part." 
  [name & rest]
      
  )

;examples
(defstream not-too-long :short-message #" not too long " 
     (println message))
