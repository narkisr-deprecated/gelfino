(ns gelfino.streams
  (:use 
    (gelfino.drools dsl straping)
   [clojure.tools.logging :only (trace info debug error)]
   (gelfino compression constants chunked header)
    lamina.core)
  (:require 
    [cheshire.core :as cheshire]
    [cheshire.parse :as cparse]
    [clojure.walk :as walk]
    [gelfino.statistics :as stats]))


(def base-channels (atom {}))
(def stream-channels (atom {}))

(defn route-handling [data]
  (let [type (gelf-type data) {:keys [input output]} @base-channels]
      (trace type) 
      (condp  = type
        zlib-header-id (future (enqueue output (decompress-zlib data))) 
        gzip-header-id (future (enqueue output (decompress-gzip data))) 
        chunked-header-id (future (handle-chunked data input))
        (error (str "No matching handling found for " type)))))

(defn- into-json [s]
  (binding [cparse/*use-bigdecimals?* true]
    (let [json-m (cheshire/parse-string s true)]
      (debug json-m) 
      (stats/inc-processed)
      json-m)))

(defn initialize-channels []
  (reset! base-channels {:input (channel) :output (channel)})
  (swap! base-channels assoc :jsons (map* into-json (@base-channels :output)))
  (let [{:keys [input output jsons]} @base-channels] 
    (doseq [f (vals @stream-channels)] (f jsons))
    (receive-all input #(route-handling %))))

(defn close-channels []
  (doseq [c (vals @base-channels)] (close c))
  (reset! stream-channels {}))

(defn feed-fn [packet]
  (trace (str "recieved packet " packet))
  (stats/inc-received)
  (enqueue (@base-channels :input) packet))

(defn into-pred [selector]
  (cond 
    (instance? clojure.lang.IFn (eval selector)) (eval selector) 
    (instance? java.util.regex.Pattern selector) (fn [v] (-> (re-matches selector v) nil? not))
    (instance? java.lang.String selector) (fn [v] (.contains v selector))
    :else (throw (Exception.(str "Bad predicate type " selector)))))

(defn filter-fn [pred-pairs]
  (let [pairs  (map #(into [] % ) (partition 2 pred-pairs))
        preds (map (fn [[k v]] [k (into-pred v)]) pairs)]
    (fn [m] (every? (fn [[k v]] (v (m k))) preds))))

(defn apply-sym [orig form]
  (let [new (gensym (name orig))]
    (concat (list 'fn [new]) (list (walk/postwalk #(if (= % orig) new %) form)))))

(defn drools-stream [name rule]
  `(let [stream-input# (channel)]
     (swap! stream-channels assoc ~(keyword name) 
       (fn [jsons#] (receive-all jsons# (drools-pusher ~rule))))))

(defn selectors-stream [name rest]
  `(let [stream-input# (channel)]
     (swap! stream-channels assoc ~(keyword name) 
       (fn [jsons#] 
         (receive-all (filter* (filter-fn '~rest) jsons#) ~(apply-sym 'message (last rest)))))))

(defmacro defstream
  "A stream of messages filtered out of the entire messages recieved 
  the defenition takes pairs of key values where key is the message part we filter upon and the value is either:
  * A predicate function that accepts the part and returns true if it macthes.
  * A regex on which the parts will be matched.
  * A substring of the matched message part." 
  [name & rest]
  (if (= (first rest) :rule)
    (drools-stream name (second rest))
    (selectors-stream name rest)))

;examples
#_(macroexpand '(defstream not-too-long :short_message #" not too long " (println message))) 

