(ns gelfino.core
   (:use aleph.udp 
        lamina.core gelfino.compression gelfino.constants gelfino.chunked
        ))

(def so (udp-socket {:port 12201 }))

(defn read-last []
  (def last-m (atom nil))
  (swap! last-m (fn [_] (->  (read-channel @so) deref :message (.array)))))

(defn- norm [b] 
  (.substring (Integer/toString  (+ (bit-and 0xff b) 0x100) 16) 1)) 

(defn gelf-type [data] (apply str (map norm (take 2 data)))) 

(defn as-data [m] (->  m :message (.array)))

(def output (channel))

(receive-all output #(println %))

(defn process-incoming [] 
 (receive-all @so 
   (fn [m] 
     (let [data (as-data m)] 
      (condp  = (gelf-type data)
       zlib-header (enqueue output (decompress-zlib data))
       chunked-header (handle-chunked data output)
       )))))

#_(close @so)



#_(keep-indexed (fn [i v] (if (> i 2) v))  (take 12 @last-m)) 
; Each chaunked message can be a channel that we reduce upon, once its drained the reduce result can be used
