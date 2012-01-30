(ns gelfino.client
   (:import 
    java.util.Date 
    (org.graylog2 GelfSender GelfMessage)))


(def sender (GelfSender. "Uranus"))

(defn send-m [short full level]
  (let [msg (GelfMessage. short full (Date.) level)]
    (.setHost msg "Uranus")
    (.sendMessage sender msg )))

(send-m "yeap" "not too long" "INFO") 

