(ns gelfino.chunked
  (:import java.io.ByteArrayOutputStream)
  (:use 
    [clojure.tools.logging :only (trace info error)]
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

(defn handle-chunked [^bytes m output]
  "Handling chunked messages, output is the channel onto completed messages will be written"
  {:pre [(> (alength m) chunked-header-length)] }  
    (let [{:keys [id sequence total]} (chunked-header m)]
      (dosync
        (when-not (contains? @channels id)
          (alter channels assoc id (channel))))
      (enqueue (@channels id) m)
      (when (= sequence (- total 1))
        (merge-chunks (@channels id) output)
        (future
          (close (@channels id))
          ; if dosync is out of future scope we have a race condition!
          (dosync 
            (alter channels dissoc id))))))
