(ns gelfino.drools.straping
  (:use gelfino.drools.bridging)
  (:import 
    [org.drools KnowledgeBase KnowledgeBaseFactory]
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

(defn- build-session []
  (.addKnowledgePackages knowledge-base (. builder getKnowledgePackages))
  (.newStatefulKnowledgeSession knowledge-base))

(defn- validate [error]
  (when (.hasErrors builder) 
    (println (.toString (.getErrors builder)))
    (throw (RuntimeException. error))) )

(defn build-session-from-drl [path] 
  (set-non-strict)
  (.add builder (ResourceFactory/newFileResource path) ResourceType/DRL)
  (validate "Unable to compile drl.")
  (build-session))

(defn- add-actions [session]
  (.setGlobal session "actions" actions) session)

(defn build-gelfino-session [pkg] 
  (set-non-strict)
  (.add builder (ResourceFactory/newDescrResource pkg) ResourceType/DESCR)
  (validate "Unable to compile pkg.")
  (-> (build-session) (add-actions)))




