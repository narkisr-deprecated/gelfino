(ns gelfino.drools
  (:import 
     [org.drools KnowledgeBase KnowledgeBaseFactory]
     [org.drools.builder KnowledgeBuilder KnowledgeBuilderError KnowledgeBuilderErrors KnowledgeBuilderFactory]
      org.drools.conf.EventProcessingOption
      org.drools.builder.ResourceType org.drools.io.ResourceFactory 
      org.drools.runtime.StatefulKnowledgeSession))

(defrecord Message [level datetime])

(def builder (KnowledgeBuilderFactory/newKnowledgeBuilder))

(def knowledge-base
  (let [config (KnowledgeBaseFactory/newKnowledgeBaseConfiguration)]
    (.setOption config (EventProcessingOption/STREAM))
    (KnowledgeBaseFactory/newKnowledgeBase config)))

(defn build-session [] 
    (.add builder (ResourceFactory/newFileResource "src/main/resources/example.drl") ResourceType/DRL)
    (when (.hasErrors builder) 
       (println (.toString (.getErrors builder)))
       (throw (RuntimeException. "Unable to compile drl\".")))
    (.addKnowledgePackages knowledge-base (. builder getKnowledgePackages))
    (.newStatefulKnowledgeSession knowledge-base ))


(let [session (build-session) entry (.getWorkingMemoryEntryPoint session "entryone")]
  (.insert entry (Message. "INFO" ""))
  (.insert entry (Message. "bla" ""))
 (.fireAllRules session)  
  )





