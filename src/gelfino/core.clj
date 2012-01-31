(ns gelfino.core
  (:use 
    lamina.core 
    [clojure.tools.logging :only (trace info)]
    clojure.data.json
    (gelfino compression constants chunked header udp)))

(def in-out (atom {:input (channel) :output (channel)}))

(defn route-handling [data]
  (let [type (gelf-type data)]
    (info type) 
    (condp  = type
       zlib-header-id (enqueue (@in-out :output) (decompress-zlib data))
       gzip-header-id (enqueue (@in-out :output) (decompress-gzip data))
       chunked-header-id (handle-chunked data (@in-out :input))
       (info (str "No matching handling found for " type)))))

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
 (receive-all (@in-out :output) #(info (read-json %)))
 (receive-all (@in-out :input) #(route-handling %))
 (connect)
 (feed-messages
   (fn [packet] 
       (enqueue (@in-out :input) (as-data packet)))))


(defn reset []
  (doseq [c (vals @in-out)] (close c))
  (disconnect) 
  (start-processing))


