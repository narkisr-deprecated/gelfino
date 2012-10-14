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

(defbolt unicorns-count ["count"] {:prepare true}
  [conf context collector]
  (let [counts (atom 0)]
    (bolt
      (execute [tuple]
               (swap! counts inc)
               (info @counts)
               (emit-bolt! collector [@counts])))))

(defn mk-topology []
  (topology {"1" (spout-spec gelf-events)} 
            {"2" (bolt-spec {"1" :shuffle} unicorns-count) }))

(defn -main 
  ([] (run-local! (mk-topology)))
  ([name] (run-cluster-wide! name (mk-topology))))
