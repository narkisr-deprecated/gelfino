; seem to be the cause  for truncation! http://lists.jboss.org/pipermail/netty-users/2009-June/000729.html
(ns gelfino.core
  (:use aleph.udp lamina.core 
    [clojure.tools.logging :only (trace info)]
    clojure.data.json
    gloss.core
    (gelfino compression constants chunked header)))

(defn as-data [m] (->  m :message (.array)))

(def output (channel))
(def input (channel))

(defn route-handling [data]
  (let [type (gelf-type data)]
    (trace type) 
    (condp  = type
       zlib-header-id (enqueue output (decompress-zlib data))
       gzip-header-id (enqueue output (decompress-gzip data))
       chunked-header-id (handle-chunked data input)
       (throw (Exception. (str "No matching handling found for " type))))))

(receive-all output #(info (read-json %)))
(receive-all input #(route-handling %))

(def last-m (atom nil))

(defn connect []
  (def port (atom nil))
  (reset! port (udp-socket {:port 12201 :buf-size 5000})))

(defn disconnect [] (-> @port deref close))

(defn start-processing [] 
 (connect)
 (receive-all (deref @port) 
   (fn [m]
       (info "got new message")
       (info m)
       (enqueue input (as-data m)))))


