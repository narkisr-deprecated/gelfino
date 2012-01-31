; seem to be the cause  for truncation! http://lists.jboss.org/pipermail/netty-users/2009-June/000729.html
; Try using this one http://web.archiveorange.com/archive/v/ZVMdI7IKZeZ1FJfkdqUN 
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


(defn start-processing [] 
 (receive-all (@in-out :output) #(info (read-json %)))
 (receive-all (@in-out :input) #(route-handling %))
 (connect)
 (feed-messages
   (fn [packet] 
       (println packet) 
        ;;; chunk.setRaw(clientMessage.getData(), clientMessage.getLength());
       (enqueue (@in-out :input) (.getData packet)))))


(defn reset []
  (doseq [c (vals @in-out)] (close c))
  (disconnect) 
  (start-processing))
