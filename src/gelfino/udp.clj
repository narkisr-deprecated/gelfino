(ns gelfino.udp
  (:import (java.net InetSocketAddress DatagramSocket DatagramPacket))
  (:use gelfino.constants) 
  )

(defn- bind [host port]
  (InetSocketAddress. host port))

(defn connect []
  (def server-socket (atom nil)) 
  (reset! server-socket (DatagramSocket. (bind "Uranus" 12201 ))))

(def run-flag (atom true))

(defn disconnect [] 
  (reset! run-flag false)
  (.close @server-socket))

(defn listen-loop [consumer]
   (while @run-flag
      (let [received-data (byte-array max-packet-size) 
           packet (DatagramPacket. received-data (alength received-data))]
        (.receive @server-socket packet) 
        (consumer packet))))

(defn feed-messages [consumer] 
  (.start  (Thread. #(listen-loop consumer))))
