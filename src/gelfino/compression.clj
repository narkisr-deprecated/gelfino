(ns gelfino.compression
 (:import (java.io ByteArrayOutputStream ByteArrayInputStream) (java.util.zip InflaterInputStream)))


(defn decompress-zlib [data]
  (let [buffer (byte-array (alength data)) 
        out (ByteArrayOutputStream.)
        in (InflaterInputStream. (ByteArrayInputStream. data)) ]
       (loop [bytes-read (.read in buffer) r 0]
        (if-not (= bytes-read -1)
          (do
            (.write out buffer 0 bytes-read)
            (recur (.read in buffer) (+ r bytes-read)))))
       (String. (.toByteArray out) "UTF-8")))



