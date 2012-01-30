(ns gelfino.header
   (:use (gelfino constants)) 
    )

(defn- bit-decode [bit] 
  (.substring (Integer/toString  (+ (bit-and 0xff bit) 0x100) 16) 1)) 

(defn- normalize [bits] (apply str (map bit-decode bits)))

(defn gelf-type [bits] (normalize (take 2 bits))) 


(defn- take-part [raw s e]
  (if (= (- e s) 1)
     (raw s) 
     (normalize (subvec raw s e))))

(defn positive [m k] (> (m k) 0))

(defn positive-inc [m k] (or (= 0 (m k)) (positive m k)))

(defn chunked-header [m]
  {:post [(positive-inc % :sequence) (positive % :total) 
          (<= (% :sequence) (- (:total %) 1))
          #_(= (% :gelf-id chunked-header-id)) 
          ] 
   :pre  [(> (count m) 0)]}
  "The header according to this spec:
    Chunked GELF ID: 0x1e 0x0f 
    Message ID: 8 bytes 
    Sequence Number: 1 byte (The sequence number of this chunk)
    Total Number: 1 byte (How many chunks does this message consist of in total)"
  (let [parts [0 2 10 11 12] raw (into [] (take 12 m))]
    (zipmap [:gelf-id :id :sequence :total]
       (map (fn [[s e]] (take-part raw s e)) (partition 2 1 parts)))))
