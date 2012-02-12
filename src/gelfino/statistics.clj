(ns gelfino.statistics
  (:use 
   [clojure.tools.logging :only (trace info debug warn)]
   tron))

(def counters (agent {:processed 0 :prev 0 :recieved 0 :chunks 0}))

(defn collect [] 
  (tron/do-periodically 5000 
    (let [{:keys [processed prev recieved chunks]} @counters]
      (info (str "messages recieved: " recieved))
      (info (str "messages processed: " processed))
      (info (str "chunks processed: " chunks))
      (info (str "current rate is: " (- processed prev)))
      (send counters #(assoc % :prev (% :processed ))))))

(defn stop []
  (tron/shutdown))

(defn inc-processed [] 
  (send counters #(assoc % :processed (-> % :processed (+ 1)))))

(defn inc-received [] 
  (send counters #(assoc % :recieved (-> % :recieved (+ 1)))))

(defn inc-chunks [] 
  (send counters #(assoc % :chunks (-> % :chunks (+ 1)))))
