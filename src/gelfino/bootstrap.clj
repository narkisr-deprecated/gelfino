(ns gelfino.bootstrap
  (:require 
     [cheshire.core :as cheshire]
     [gelfino.statistics :as stats])
  (:import 
    org.apache.log4j.Level
    org.apache.log4j.LogManager)
  (:use 
    lamina.core 
    clojure.data.json
    [clojure.tools.logging :only (trace info debug error)]
    (gelfino compression constants header chunked udp)))

(def in-out (atom {}))

(defn route-handling [data]
  (let [type (gelf-type data)]
    (trace type) 
      (condp  = type
        zlib-header-id (future (enqueue (@in-out :output) (decompress-zlib data))) 
        gzip-header-id (future (enqueue (@in-out :output) (decompress-gzip data))) 
        chunked-header-id (handle-chunked data (@in-out :input)) 
        (error (str "No matching handling found for " type)))))

(defn- initialize-channels []
  (reset! in-out {:input (channel) :output (channel)})
  (let [{:keys [input output]} @in-out] 
    (receive-all output  
      (fn [m] 
        (let [json-m (cheshire/parse-string m true)]
          (debug json-m) 
          (stats/inc-processed)))) 
    (receive-all input #(route-handling %))))

(defn start-processing [host port] 
    (stats/collect) 
    (initialize-channels)
    (connect host (Integer. port)) 
    (feed-messages
      (fn [packet] 
        (trace (str "recieved packet " packet))
        (stats/inc-received)
        (enqueue (@in-out :input) packet))))

(defn- shutdown []
  (stats/stop)
  (doseq [c (vals @in-out)] (close c))
  (disconnect))

(defn reset [host port]
  (shutdown) 
  (start-processing host port))

(defn- enable-tracing []
  (.setLevel 
    (LogManager/getLogger "log4j.logger.gelfino.bootstrap") (Level/toLevel "TRACE"))  
  (debug "level set to trace"))
