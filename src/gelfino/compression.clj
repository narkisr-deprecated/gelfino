(ns gelfino.compression
  (:import (java.io ByteArrayOutputStream ByteArrayInputStream) (java.util.zip InflaterInputStream GZIPInputStream)))


(defn decompress-zlib [data]
  (let [buffer (byte-array (alength data))
        out (ByteArrayOutputStream.)
        in (InflaterInputStream. (ByteArrayInputStream. data)) ]
    (loop [bytes-read 0]
      (when-not (< bytes-read 0)
        (.write out buffer 0 bytes-read)
        (recur (.read in buffer))))
    (.flush out)
    (.close out)
    (String. (.toByteArray out) "UTF-8")))

(defn decompress-gzip [data]
  (let [buffer (byte-array (alength data))
        out (ByteArrayOutputStream.)
        in (GZIPInputStream. (ByteArrayInputStream. data)) ]
    (loop [bytes-read 0]
      (when-not (< bytes-read 0)
        (.write out buffer 0 bytes-read)
        (recur (.read in buffer))))
    (.flush out)
    (.close out)
    (String. (.toByteArray out) "UTF-8")))
