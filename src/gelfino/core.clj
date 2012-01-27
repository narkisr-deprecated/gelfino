; use nc -u 127.0.0.1 10001 to play with me :)
;
(ns gelfino.core
   (:use aleph.udp lamina.core gloss.core gelfino.compression))

(def so (udp-socket {:port 12201 }))

(def last-m (atom nil))

(close @so)

(defn read-last []
  (swap! last-m (fn [_] (->  (read-channel @so) deref :message (.array)))))

(defn- norm [b] 
  (.substring (Integer/toString  (+ (bit-and 0xff b) 0x100) 16) 1)) 

(defn gelf-type [data] (apply str (map norm (take 2 data)))) 

(decompress-zlib @last-m)

#_(println (read-channel @so) )
#_(wait-for-message @so)
#_ (receive-all @so #(println "message:" (class (:message  %))) )

