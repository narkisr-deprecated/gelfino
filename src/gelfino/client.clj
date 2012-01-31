(ns gelfino.client
   (:import 
    java.util.Date 
    (org.graylog2 GelfSender GelfMessage)))

(def host "Uranus")

(def sender (GelfSender. "Uranus"))

(defn send-m [short full level]
  (let [msg (GelfMessage. short full (Date.) level)]
    (.setHost msg "Uranus")
    (.sendMessage sender msg )))



(defn performance []
  (apply pcalls (for [i (range 70000)] 
    (fn [] 
      (Thread/sleep 50) 
      (send-m (str "yeap " i) "not too long" "INFO"))))) 
