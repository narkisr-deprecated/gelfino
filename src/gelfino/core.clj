(ns gelfino.core
  (:gen-class)
  (:import 
    org.apache.log4j.Level
    org.apache.log4j.LogManager)
  (:use 
    lamina.core 
    [clojure.tools.logging :only (trace info debug warn)]
    clojure.data.json
    (gelfino compression constants chunked header udp statistics)))

(def in-out (atom {:input (channel) :output (channel)}))


(defn route-handling [data]
  (let [type (gelf-type data)]
    (trace type) 
    (condp  = type
       zlib-header-id (enqueue (@in-out :output) (decompress-zlib data))
       gzip-header-id (enqueue (@in-out :output) (decompress-gzip data))
       chunked-header-id (handle-chunked data (@in-out :input))
       (warn (str "No matching handling found for " type)))))

(defn- read-slice [packet]
  (let [length (.getLength packet) slice (byte-array length)]
    (System/arraycopy (.getData packet) 0 slice 0 length) 
     slice
     ))

(defn- as-data [packet]
  (let [data (.getData packet)]
    (if (= chunked-header-id (gelf-type data))
      (read-slice packet) 
       data)))

(defn start-processing [host port] 
 (statistics)
 (connect host (Integer. port))
 (receive-all (@in-out :output)  
    (fn [m] 
      (let [json-m (read-json m)]
        (debug json-m) 
        (inc-total))))
 (receive-all (@in-out :input) #(route-handling %))
 (feed-messages
   (fn [packet] 
       (trace (str "recieved packet " packet))
       (enqueue (@in-out :input) (as-data packet)))))

(defn reset [host port]
  (doseq [c (vals @in-out)] (close c))
  (disconnect) 
  (start-processing host port))

(defn -main [host port]
  (start-processing host port))

(defn- enable-tracing []
  (.setLevel 
    (LogManager/getLogger "log4j.logger.gelfino") (Level/toLevel "TRACE"))  
  (debug "level set on trace"))
