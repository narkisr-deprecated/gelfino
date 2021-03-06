; The following links explain why not to use Netty for udp, it sums up to the line that you gain little by using it in this use case
; Cause  for truncation! http://tinyurl.com/6m5bfqn ;Try using this one http://tinyurl.com/887am2j  only solves it but still Netty in this case just insn't worth it.
(ns gelfino.udp
  (:import 
   (java.net InetSocketAddress DatagramSocket DatagramPacket))
  (:use 
    (gelfino constants header) 
    [clojure.tools.logging :only (trace error debug info)]
   ))

(defn- bind [host port]
  (InetSocketAddress. host port))

(defn connect [host port]
  (def server-socket (atom (DatagramSocket. (bind host port)))))

(def run-flag (atom true))

(defn disconnect [] 
  (reset! run-flag false)
  (.close @server-socket))

(defn- read-slice [^DatagramPacket packet]
  (let [length (.getLength packet) slice (byte-array length)]
    (System/arraycopy (.getData packet) 0 slice 0 length) 
     slice))

(defn- as-data [^DatagramPacket packet]
  (let [data (.getData packet)]
    (if (= chunked-header-id (gelf-type data))
      (read-slice packet) 
       data)))

(defn listen-loop [consumer]
  (debug "starting listener")
  (while @run-flag
     (let [received-data (byte-array max-packet-size) 
           packet (DatagramPacket. received-data (alength received-data))
           socket ^DatagramSocket @server-socket]
         (trace "waiting for packets")
         (.receive socket packet) 
         (try (consumer (as-data packet))
           (catch Error e (error e))))))

(defn feed-messages [consumer] 
  (reset! run-flag true)
  (.start  (Thread. #(listen-loop consumer) "UDP loop")))
