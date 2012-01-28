(ns gelfino.chunked
  (:use lamina.core (gelfino constants header)))

(def channels (ref {}))

(defn- chunked-channel [output]
   (let [chunked (channel)]
     (on-success (reduce* conj [] chunked) #(enqueue output %)) 
      chunked))

(defn handle-chunked [m output]
  "Handling chunked messages, output is the channel onto completed messages will be written"
  {:pre [(> (alength m) chunked-header-length)] }  
    (let [{:keys [id sequence total]} (chunked-header m)]
      (dosync
        (if-not (contains? @channels id)
          (alter channels assoc id (chunked-channel output))))
      (enqueue (@channels id) m)
      (if (= (first sequence) (- (first total) 1))
        (close (@channels id)))))
