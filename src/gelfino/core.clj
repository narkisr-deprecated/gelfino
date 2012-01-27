(ns gelfino.core
   (:use aleph.udp 
        lamina.core 
        gloss.core 
        gelfino.compression 
        clojure.core.match))

(def so (udp-socket {:port 12201 }))

(defn read-last []
  (def last-m (atom nil))
  (swap! last-m (fn [_] (->  (read-channel @so) deref :message (.array)))))

(defn- norm [b] 
  (.substring (Integer/toString  (+ (bit-and 0xff b) 0x100) 16) 1)) 

(defn gelf-type [data] (apply str (map norm (take 2 data)))) 

(defn as-data [m] (->  m :message (.array)))

(defn process-incoming [] 
 (receive-all @so 
   (fn [m] 
    (let [data (as-data m)] 
     (condp  = (gelf-type data)
      "789c" (println  (decompress-zlib data)))))))

#_(close @so)


