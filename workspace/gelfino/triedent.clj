(ns gelfino.triedent
  (:use [clojure test])
  (:require [backtype.storm [testing :as t]])
  (:import [storm.trident.testing Split CountAsAggregator StringLength TrueFilter])
  (:use [storm.trident testing])
  (:use [backtype.storm util]))

(bootstrap-imports)

(defn run-count [topo feeder]
  (-> topo
    (.newStream "tester" feeder)
    (.each (fields "sentence") (Split.) (fields "word"))
    (.groupBy (fields "word"))
    (.persistentAggregate (memory-map-state) (Count.) (fields "count"))
    (.parallelismHint 6)
    ))

(defn query [topo drpc word-counts]
  (-> topo
    (.newDRPCStream "words" drpc)
    (.each (fields "args") (Split.) (fields "word"))
    (.groupBy (fields "word"))
    (.stateQuery word-counts (fields "word") (MapGet.) (fields "count"))
    (.aggregate (fields "count") (Sum.) (fields "sum"))
    (.project (fields "sum"))
    ))

(t/with-local-cluster [cluster]
  (with-drpc [drpc]
    (let [topo (TridentTopology.) feeder (feeder-spout ["sentence"])] 
      (run-count topo feeder)
      (query ))))
