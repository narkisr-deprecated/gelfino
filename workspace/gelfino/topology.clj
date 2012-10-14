(ns gelfino.topology
 (:import [backtype.storm StormSubmitter LocalCluster])
 (:use [backtype.storm clojure config])) 

(defn run-cluster-wide! [name t]
  (StormSubmitter/submitTopology name {TOPOLOGY-DEBUG true TOPOLOGY-WORKERS 3} t))

(defn run-local! [t]
  (let [cluster (LocalCluster.)]
    (.submitTopology cluster "gelfino" {TOPOLOGY-DEBUG true} t)
    ))
