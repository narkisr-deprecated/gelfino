(ns gelfino.drools.example
  (:use gelfino.drools.bridging gelfino.drools.straping)
  (:import [gelfino.drools.bridging Message]))

(defn now [] (java.util.Date.))

(let [session (build-session-from-drl "src/main/resources/example.drl") 
      entry (.getWorkingMemoryEntryPoint session "entryone")]
  (dosync (alter actions assoc "rule1" #(println "rule fired me")))
  (.setGlobal session "actions" actions)
  ;(.insert entry (Message. "INFO" (now)))
  ;(.insert entry (Message. "bla" (now)))
  ;(.insert entry (Message. "bla" (now)))
  ;(.insert entry (Message. "INFO" (java.util.Date.)))
  (.fireAllRules session))

