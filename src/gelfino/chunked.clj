(ns gelfino.chunked
  (:use lamina.core gelfino.constants))

(def channels (ref {}))

(defn header [m]
  "The header according to this spec:
     Chunked GELF ID: 0x1e 0x0f 
     Message ID: 8 bytes 
     Sequence Number: 1 byte (The sequence number of this chunk)
     Total Number: 1 byte (How many chunks does this message consist of in total)"
  (let [parts [0 2 10 11 12] raw (into [] (take 12 m))]
    (zipmap [:gelf-id :id :sequence :total]
      (map (fn [[s e]] (subvec raw s e)) (partition 2 1 parts)))))

(defn- chunked-channel [output]
   (let [chunked (channel)]
     (on-success (reduce* conj [] chunked) #(enqueue output %)) 
      chunked))

(defn handle-chunked [m output]
  "Handling chunked messages, output is the channel onto completed messages will be written"
  {:pre [(> (alength m) chunked-header-length)] }  
    (let [{:keys [id sequence total]} (header m)]
      (dosync
        (if-not (contains? @channels id)
          (alter channels assoc id (chunked-channel output))))
      (enqueue (@channels id) m)
      (if (= (first sequence) (- (first total) 1))
        (close (@channels id)))))
