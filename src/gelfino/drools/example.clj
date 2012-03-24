(ns gelfino.drools.example
  (:use gelfino.drools.bridging)
  (:import 
     [org.drools KnowledgeBase KnowledgeBaseFactory]
     [gelfino.drools.bridging Message]
     [org.drools.builder KnowledgeBuilder KnowledgeBuilderError KnowledgeBuilderErrors KnowledgeBuilderFactory]
      org.drools.conf.EventProcessingOption
      org.drools.builder.ResourceType org.drools.io.ResourceFactory 
      org.drools.runtime.StatefulKnowledgeSession))


(def builder (KnowledgeBuilderFactory/newKnowledgeBuilder))


(def knowledge-base
  (let [config (KnowledgeBaseFactory/newKnowledgeBaseConfiguration)]
    (.setOption config (EventProcessingOption/STREAM))
    (KnowledgeBaseFactory/newKnowledgeBase config)))

(defn set-non-strict []
  "Setting mvel to be non-strict (thus avoiding all those type casting in RHS) see http://tinyurl.com/7hsoe4c"
  (-> builder (.getPackageBuilder) (.getPackageBuilderConfiguration) 
              (.getDialectConfiguration  "mvel") (.setStrict false)))

(defn build-session [] 
    (set-non-strict)
    (.add builder (ResourceFactory/newFileResource "src/main/resources/example.drl") ResourceType/DRL)
    (when (.hasErrors builder) 
       (println (.toString (.getErrors builder)))
       (throw (RuntimeException. "Unable to compile drl\".")))
    (.addKnowledgePackages knowledge-base (. builder getKnowledgePackages))
    (.newStatefulKnowledgeSession knowledge-base ))


(let [session (build-session) entry (.getWorkingMemoryEntryPoint session "entryone")]
  (dosync
    (alter actions assoc "rule1" #(println "rule 1 fired me")))
  (.setGlobal session "actions" actions)
  (.insert entry (Message. "INFO" ""))
  (.insert entry (Message. "bla" ""))
  (.fireAllRules session))

