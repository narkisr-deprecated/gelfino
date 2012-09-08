(ns gelfino.storm
 (:gen-class)
 (:import [backtype.storm StormSubmitter LocalCluster])
 (:use 
    [clojure.tools.logging :only (info)]
    [gelfino streams bootstrap] 
    [backtype.storm clojure config]))

(def events (agent clojure.lang.PersistentQueue/EMPTY ) )


(defstream unicorns :short_message #".*unicorn.*" (send events conj message))

(defspout events-spout ["event"]
  [conf context collector]
  (spout
    (nextTuple []
      (if-let [event (peek @events)]
         (do 
          (info event) 
          (emit-spout! collector [event])
          (send events pop))         
         (Thread/sleep 500)))
    (ack [id])))


(defbolt unicorns-count ["count"] {:prepare true}
  [conf context collector]
  (let [counts (atom 0)]
    (bolt
     (execute [tuple]
      (swap! counts inc)
      (info @counts)
      (emit-bolt! collector [@counts] :anchor tuple)))))


(defn mk-topology []
  (topology {"1" (spout-spec events-spout)} 
            {"2" (bolt-spec {"1" :shuffle} unicorns-count) }))


(defn run-local! []
  (let [cluster (LocalCluster.)]
    (.submitTopology cluster "gelfino" {TOPOLOGY-DEBUG true} (mk-topology))
    (start-processing "0.0.0.0" "12201")
    ;(Thread/sleep 10000)
    ;(.shutdown cluster)
    ))

(defn -main [] (run-local!))
;(run-local!)
