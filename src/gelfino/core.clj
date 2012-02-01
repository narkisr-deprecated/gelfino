(ns gelfino.core
  (:gen-class)
  (:use 
    lamina.core 
    [clojure.tools.logging :only (trace info debug warn)]
    clojure.data.json
    tron
    (gelfino compression constants chunked header udp)))

(def in-out (atom {:input (channel) :output (channel)}))
(def counters (agent {:total 0 :prev 0}))

(defn statistics [] (tron/do-periodically 5000 
  (let [{:keys [total prev]} @counters]
    (info (str "Total messages processed so far: " total))
    (info (str "Current processing rate is: " (- total prev)))
    (send counters #(assoc % :prev (% :total ))))))

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

(defn start-processing [] 
 (receive-all (@in-out :output)  
    (fn [m] 
      (let [json-m (read-json m)]
        (debug json-m) 
        (send counters #(assoc % :total (-> % :total (+ 1)))))))
 (receive-all (@in-out :input) #(route-handling %))
 (connect)
 (feed-messages
   (fn [packet] 
       (debug (str "recieved packet " packet))
       (enqueue (@in-out :input) (as-data packet)))))


(defn reset []
  (doseq [c (vals @in-out)] (close c))
  (disconnect) 
  (start-processing))

(defn -main []
  (statistics)
  (start-processing))
