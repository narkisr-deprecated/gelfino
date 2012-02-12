(ns gelfino.chunked
  (:import java.io.ByteArrayOutputStream)
  (:require [gelfino.statistics :as stats])
  (:use 
    [clojure.tools.logging :only (trace info error debug)]
    lamina.core 
    (gelfino constants header)))

(def channels (ref {}))

(defn- merge-bytes [^ByteArrayOutputStream output ^bytes curr]
    (.write output curr chunked-header-length (- (alength curr) chunked-header-length))
      output)

(defn merge-chunks [chunks out-channel]
  (let [result (reduce* merge-bytes (ByteArrayOutputStream.) chunks)]
    (on-success result (fn [^ByteArrayOutputStream output] (enqueue out-channel (.toByteArray output)))) 
    (on-error result #(error %))))

(defn add-channel [out-channel id]
 (let [ch (channel)]
   (alter channels assoc id 
     {:channel ch 
      :result (run-pipeline ch
                #(reduce* merge-bytes (ByteArrayOutputStream.) %)
                #(enqueue out-channel (.toByteArray %)))})))

(defn handle-chunked [^bytes m output]
  "Handling chunked messages, output is the channel onto completed messages will be written"
  {:pre [(> (alength m) chunked-header-length)] }  
    (let [{:keys [id sequence total]} (chunked-header m)]
      (debug id)
      (dosync
        (when-not (contains? @channels id)
          (add-channel output id)))
      (enqueue (get-in @channels [id :channel]) m)
      (stats/inc-chunks)
      (when (= sequence (- total 1))
        (future
          (close (get-in @channels [id :channel]))
          (deref (get-in @channels [id :result])) 
          ; if dosync is out of future scope we have a race condition!
          (dosync 
            (alter channels dissoc id))))))
