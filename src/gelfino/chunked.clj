(ns gelfino.chunked
  (:import java.io.ByteArrayOutputStream)
  (:require [gelfino.statistics :as stats])
  (:use 
    [clojure.tools.logging :only (trace info error debug)]
    lamina.core 
    (gelfino constants header)))

(def buffers (ref {}))

(defn- merge-bytes [^ByteArrayOutputStream output ^bytes curr]
    (.write output curr chunked-header-length (- (alength curr) chunked-header-length))
      output)

(defn update-buf [id buf s m]
  (alter buffers assoc id (assoc buf s m)))

(defn handle-chunked [^bytes m output]
  "Handling chunked messages, output is the channel onto completed messages will be written"
  {:pre [(> (alength m) chunked-header-length)] }  
  (let [{:keys [id sequence total]} (chunked-header m)]
    (debug id)
    (dosync
      (if-not (contains? @buffers id)
        (update-buf id (sorted-map) sequence m) 
        (update-buf id (buffers id) sequence m)))
    (stats/inc-chunks)
    (when (= (-> (buffers id) vals count) total)
      (future
        (let [merged (reduce merge-bytes (ByteArrayOutputStream.) (vals (buffers id)))]
          (enqueue output (.toByteArray merged)))
        ; if dosync is out of future scope we have a race condition!
        (dosync 
          (alter buffers dissoc id))))))
