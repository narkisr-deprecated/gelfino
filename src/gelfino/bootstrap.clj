(ns gelfino.bootstrap
  (:import 
    org.apache.log4j.Level
    org.apache.log4j.LogManager)
  (:use (gelfino udp streams))
  (:require  
    [gelfino.statistics :as stats]))

(defn start-processing [host port] 
  (stats/collect) 
  (initialize-channels)
  (connect host (Integer. port)) 
  (feed-messages feed-fn))

(defn- shutdown []
  (stats/stop)
  (close-channels) 
  (disconnect))

(defn reset [host port]
  (shutdown) 
  (start-processing host port))

#_(defn- enable-tracing []
  (.setLevel 
    (LogManager/getLogger "log4j.logger.gelfino.bootstrap") (Level/toLevel "TRACE"))  
  (debug "level set to trace"))
