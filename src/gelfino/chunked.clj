(ns gelfino.chunked
  (:import java.io.ByteArrayOutputStream)
  (:use 
    [clojure.tools.logging :only (trace info)]
    lamina.core (gelfino constants header)))

(def channels (ref {}))

(defn- merge-bytes [output curr]
    (.write output curr chunked-header-length (- (alength curr) chunked-header-length))
      output)


(defn merge-chunks [chunks out-channel]
  (let [result (reduce* merge-bytes (ByteArrayOutputStream.) chunks)]
    (on-success result #(enqueue out-channel (.toByteArray %)))
    (on-error result #(println %))))

(defn handle-chunked [m output]
  "Handling chunked messages, output is the channel onto completed messages will be written"
  {:pre [(> (alength m) chunked-header-length)] }  
    (info (take 12 m))
    (let [{:keys [id sequence total]} (chunked-header m)]
      (dosync
        (when-not (contains? @channels id)
           (alter channels assoc id (channel))))
      (enqueue (@channels id) m)
      (when (= sequence (- total 1))
        (merge-chunks (@channels id) output)
        (close (@channels id)) 
        (dosync
          (alter channels dissoc id))
        )))
