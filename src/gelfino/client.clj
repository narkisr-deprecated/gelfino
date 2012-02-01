(ns gelfino.client
   (:use clojure.data.json)
   (:import 
    java.util.Date 
    com.pstehlik.groovy.gelf4j.appender.Gelf4JAppender
    com.pstehlik.groovy.gelf4j.net.GelfTransport))

(def host "Uranus")

(def appender 
     (doto  (Gelf4JAppender.)
       (.setHost "Uranus")    
       (.setGraylogServerHost "Uranus")))

(def transport (GelfTransport.)) 


(defn send [m]
  (.sendGelfMessageToGraylog transport appender 
    (json-str
      {:facility "GELF" :full_message m :host host :level "INFO" :short_message m :version "1.0"})))

#_(send (apply str (range 3000)))

 
(defn performance []
  (apply pcalls (for [i (range 700)] 
    (fn [] 
      (Thread/sleep 50) 
      (send  "not too long")))))
