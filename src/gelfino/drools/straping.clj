(ns gelfino.drools.straping
  (:use gelfino.drools.bridging)
  (:import 
    [org.drools.runtime.conf ClockTypeOption ]
    [org.drools KnowledgeBase KnowledgeBaseFactory]
    [org.drools.builder KnowledgeBuilder KnowledgeBuilderError KnowledgeBuilderErrors KnowledgeBuilderFactory]
    org.drools.conf.EventProcessingOption
    org.drools.builder.ResourceType org.drools.io.ResourceFactory 
    org.drools.runtime.StatefulKnowledgeSession
    gelfino.drools.bridging.Message))

(def builder (KnowledgeBuilderFactory/newKnowledgeBuilder))

(defn knowledge-base []
  (let [config (KnowledgeBaseFactory/newKnowledgeBaseConfiguration)]
    (.setOption config (EventProcessingOption/STREAM))
    (KnowledgeBaseFactory/newKnowledgeBase config)))

(defn set-non-strict []
  "Setting mvel to be non-strict (thus avoiding all those type casting in RHS) 
   see http://tinyurl.com/7hsoe4c"
  (-> builder (.getPackageBuilder) (.getPackageBuilderConfiguration) 
    (.getDialectConfiguration  "mvel") (.setStrict false)))

(defn- build-session [clock] 
  (let [base (knowledge-base) session-config (KnowledgeBaseFactory/newKnowledgeSessionConfiguration)]
    (.addKnowledgePackages base (. builder getKnowledgePackages))
    (.setOption session-config (ClockTypeOption/get clock))
    (.newStatefulKnowledgeSession base session-config nil)))

(defn- validate [error]
  (when (.hasErrors builder) 
    (println (.toString (.getErrors builder)))
    (throw (RuntimeException. error))) )

(defn- add-actions [session]
  (.setGlobal session "actions" actions) session)

(defn drools-session [&{:keys [pkgs path clock] :or {clock "realtime"}}] 
  (set-non-strict)
  (if (nil? pkgs)
    (.add builder (ResourceFactory/newFileResource path) ResourceType/DRL)
    (doseq [p pkgs]
      (.add builder (ResourceFactory/newDescrResource p) ResourceType/DESCR)))
  (validate "Unable to compile.")
  (if (nil? pkgs)
    (build-session clock) 
    (add-actions (build-session clock))))

(defn drools-pusher [rule]
  "Creates an fn with session in scope for pushing messages into drools with given rule"
  (let [session (drools-session :pkgs [rule]) 
         entry (.getWorkingMemoryEntryPoint session "event-stream")]
    (fn [m]
      (.insert entry (Message. (m :level) (.longValue (* 1000 (m :timestamp)))))
      (.fireAllRules session))))
