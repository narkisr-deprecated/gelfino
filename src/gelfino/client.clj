(ns gelfino.client
  (:use [cheshire.core :only [generate-string]] )
  (:import 
    (java.net InetSocketAddress DatagramSocket DatagramPacket)
    java.io.ByteArrayOutputStream 
    java.security.MessageDigest
    java.net.InetAddress
    java.lang.System 
    java.util.Arrays
    java.util.zip.GZIPOutputStream 
    java.util.Date)) 

(defn max-chunk-size 
  ([] (max-chunk-size :lan)) 
  ([k] ({:wan 1420 :lan 8154} k)))

(def ^Long port 12201)

(def client-socket (atom nil))

(def ids (atom 0)) 

(defn connect [] 
  (reset! ids 0)
  (when @client-socket (.close @client-socket))
  (reset! client-socket (DatagramSocket.))
  )

(def message-template
  {:version  "1.0" :host  "" :short_message  "" :full_message  "" 
   :timestamp  0 :level  1 :facility  "" :file  "" :line  0 })

(defn raw-send [^"[B" data to]
  (.send ^DatagramSocket @client-socket 
         (DatagramPacket. data (alength data) (InetAddress/getByName to) port)))

(defn gzip [^String message]
  (with-open [bos (ByteArrayOutputStream.) stream (GZIPOutputStream. bos) ]
    (.write stream (.getBytes message)) 
    (.finish stream)
    (.toByteArray bos)))

(defn ++ [^"[B" f ^"[B" s]
  (let [f-l (alength f) s-l (alength s)
        res (byte-array (+ f-l s-l))]
    (System/arraycopy f 0 res 0 f-l) 
    (System/arraycopy s 0 res f-l s-l) 
    res
    ))

(defn md5 [token]
  (let [hash-bytes (doto (MessageDigest/getInstance "MD5") (.reset) (.update (.getBytes token)))]
    (.toString (new java.math.BigInteger 1 (.digest hash-bytes)) 16)))

(defn id [] 
  (swap! ids inc)
  (.getBytes (String. (.substring (md5 (str @ids (.getTime (Date.)))) 0 8))))

(defn chunk-range [c-size len]
  (let [exc (into [] (interleave (range 0 len c-size) (range c-size len c-size)))]
    (partition-all 2 (conj exc (last exc) len) )))

(defn header [i d t]
  (++ (byte-array [(byte 0x1e) (byte 0x0f)]) (++ d (byte-array [(byte i) (byte t)]))))

(defn chunks [^"[B" comp-m to]
  (let [csr (chunk-range (max-chunk-size) (alength comp-m)) d (id)]
    (map (fn [[^Long s ^Long e] i] 
           (++ (header i d (count csr)) (Arrays/copyOfRange comp-m s e))) csr (range))))

(defn send-> [to m] 
  (let [^"[B" comp-m (gzip (generate-string (merge message-template m {:timestamp (.getTime (Date.))})))]
    (if (> (alength comp-m) (max-chunk-size))
      (doseq [c (chunks comp-m to)] (raw-send c to))
      (raw-send comp-m to)))) 

;(connect)
;(send-> "localhost" {:short_message "i am a unicorn" :message (apply str (take 400000 (repeat "I am a unicorn")))})
;(send-> "0.0.0.0" {:short_message "i am a unicorn" :message "i am a unicorn" :level 4})


(defn random-string [length]
  (let [ascii-codes (concat (range 48 58) (range 66 91) (range 97 123))]
    (apply str (repeatedly length #(char (rand-nth ascii-codes))))))

