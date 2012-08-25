(ns gelfino.client
   (:use [cheshire.core :only [generate-string]] )
   (:import 
     (java.net InetSocketAddress DatagramSocket DatagramPacket)
     java.io.ByteArrayOutputStream 
     java.net.InetAddress
     java.util.zip.GZIPOutputStream 
     java.util.Date)) 

(def ^Long port 12201)

(defn connect [] (def client-socket (DatagramSocket.)))

(def message-template
  {:version  "1.0" :host  "" :short_message  "" :full_message  "" 
   :timestamp  0 :level  1 :facility  "" :file  "" :line  0 })

(defn raw-send [^"[B" data to]
   (.send ^DatagramSocket client-socket 
      (DatagramPacket. data (alength data) (InetAddress/getByName to) port)))

(defn gzip [^String message]
  (with-open [bos (ByteArrayOutputStream.) stream (GZIPOutputStream. bos) ]
    (.write stream (.getBytes message)) 
    (.finish stream)
    (.toByteArray bos)))


(defn send-> [m] 
  (let [comp-m (gzip (generate-string (merge message-template m)))]
    (raw-send comp-m "0.0.0.0"))) 

;(connect)
;(send-> {:short_message "i am a unicorn"})

(defn random-string [length]
  (let [ascii-codes (concat (range 48 58) (range 66 91) (range 97 123))]
    (apply str (repeatedly length #(char (rand-nth ascii-codes))))))

