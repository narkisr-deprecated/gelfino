(ns gelfino.storm
 (:gen-class)
  (:use 
    clojure.pprint
    [gelfino.storm.spout :only (defgspout)]
    [gelfino.topology :only (run-local! run-cluster-wide!)]
    [clojure.tools.logging :only (info)]
    [gelfino streams bootstrap] 
    [backtype.storm clojure config]))

(defgspout gelf-events)

(defstream unicorns :short_message #".*unicorn.*" (send events conj message))

(defbolt level-split ["level"] [tuple collector]
  (emit-bolt! collector [(str (tuple "level"))] :anchor tuple)
  (ack! collector tuple)
  )

(defbolt level-summary ["level" "total"] {:prepare true}
  [conf context collector]
  (let [counts (atom {})]
    (bolt
      (execute [tuple]
         (let [level (first tuple)]
           (swap! counts (partial merge-with +) {level 1}) 
           (info @counts)
           (emit-bolt! collector [level (@counts level)]) 
           (ack! collector tuple))))))

(defn mk-topology []
  (topology {"1" (spout-spec gelf-events :p 1)} 
            {"2" (bolt-spec {"1" :shuffle} level-split :p 2) 
             "3" (bolt-spec {"2" ["level"] } level-summary :p 2)}))

;(run-local! (mk-topology))

(defn -main 
  ([] (run-local! (mk-topology)))
  ([name] (run-cluster-wide! name (mk-topology))))
