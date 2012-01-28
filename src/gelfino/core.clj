(ns gelfino.core
  (:use aleph.udp lamina.core 
    (gelfino compression constants chunked header)))

(def so (udp-socket {:port 12201 }))

(defn read-last []
  (def last-m (atom nil))
  (swap! last-m (fn [_] (->  (read-channel @so) deref :message (.array)))))

(defn as-data [m] (->  m :message (.array)))

(def output (channel))

(receive-all output #(println %))

(defn process-incoming [] 
 (receive-all @so 
   (fn [m] 
     (let [data (as-data m)] 
      (condp  = (gelf-type data)
       zlib-header-id (enqueue output (decompress-zlib data))
       chunked-header-id (handle-chunked data output)
       )))))

#_(close @so)


