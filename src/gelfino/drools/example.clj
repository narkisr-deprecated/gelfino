(ns gelfino.drools.example
  (:use gelfino.drools.bridging gelfino.drools.straping)
  (:import [gelfino.drools.bridging Message]))

(let [session (build-session-from-drl "src/main/resources/example.drl") 
      entry (.getWorkingMemoryEntryPoint session "entryone")]
  (dosync
    (alter actions assoc "rule1" #(println "rule 1 fired me")))
  (.setGlobal session "actions" actions)
  (.insert entry (Message. "INFO" ""))
  (.insert entry (Message. "bla" ""))
  (.fireAllRules session))

