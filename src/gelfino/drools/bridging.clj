(ns gelfino.drools.bridging)

(defrecord Message [level datetime])

(def actions (ref {}))
