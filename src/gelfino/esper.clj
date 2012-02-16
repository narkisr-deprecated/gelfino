(ns gelfino.esper
 (:use [clojure.walk :only (stringify-keys) ])
 (:import 
    java.util.Date
    (com.espertech.esper.client Configuration EPServiceProviderManager UpdateListener))) 

(def esper-env (atom {:provider nil :runtime nil :config nil :admin nil}))

(defn initialize-esper []
  "A nice macro could be to do all this self referencing 
   {:provider (EPServiceProviderManager/getProvider :config)}"
  (swap! esper-env assoc :config (Configuration.)) 
  (swap! esper-env assoc :provider (EPServiceProviderManager/getProvider "gelfino" (@esper-env :config)))
  (swap! esper-env assoc :runtime (.getEPRuntime (@esper-env :provider)))
  (swap! esper-env assoc :admin (.getEPAdministrator (@esper-env :provider))))

(defrecord UnicornHandler [l]
     UpdateListener
      (update [this new-events old-events] (l new-events old-events)))

(defn add-event [name e]
  (.addEventType (@esper-env :config) name (stringify-keys e)))

(defn add-query [query listener ]
  (.addListener (.createEPL (@esper-env :admin) query) (UnicornHandler. listener)))
 

(initialize-esper)

(add-event "UnicornSeen" {:time Date :name "string"})

(.sendEvent (@esper-env :runtime) (stringify-keys {:time (Date.) :name "Im a cute uni!"}) "UnicornSeen");
