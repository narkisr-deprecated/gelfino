(ns gelfino.test.chunked
  (:use gelfino.chunked clojure.test
        gelfino.header lamina.core))

(defn random-string [length]
  (let [ascii-codes (concat (range 48 58) (range 66 91) (range 97 123))]
    (apply str (repeatedly length #(char (rand-nth ascii-codes))))))

(def ids (repeatedly #(rand-int 8)))

(defn fake-m [id seq-num total contents]
  {:pre [(< (alength (.getBytes id "ISO-8859-1")) 9)] :post [(chunked-header %)] }
  "Note that this isn't up to gelf spec since we dont gzip the content"
  (into-array Byte/TYPE  
              (-> [(byte 30) (byte 15)]
                (into (.getBytes id "ISO-8859-1"))    
                (into [(byte seq-num) (byte total)])
                (into (.getBytes contents "ISO-8859-1")))))

(deftest basic-chunking 
  (let [output (channel) id (random-string 8) total 3 
        messages (map #(fake-m id % total (str "part " % " ")) (range total))]
    (doseq [m messages] (handle-chunked m output)) 
    (is (= "part 0 part 1 part 2 " (String.  @(read-channel output))))))

#_(deftest lost-packets
  (let [output (channel) id (random-string 8) total 3 
        messages (map #(fake-m id % total (str "part " % " ")) (range total))]
    (doseq [m (take 2 messages)] (handle-chunked m output)) 
    (is (= "part 0 part 1 part 2 " (String.  @(read-channel output)))))
  )

(deftest out-of-order
    (let [output (channel) id (random-string 8) total 3 
          messages (map #(fake-m id % total (str "part " % " ")) (range total))]
      (doseq [m (reverse messages)] (handle-chunked m output)) 
      (is (= "part 0 part 1 part 2 " (String.  @(read-channel output)))))
    )
