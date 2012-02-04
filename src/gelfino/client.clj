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
      {:facility "GELF" :full_message m :host host :level "INFO" :short_message m :version "1.0" :_unicorn true})))

#_(defn performance [total]
  (apply pcalls (for [i (range total)] 
    (fn [] 
      (Thread/sleep 50) 
      (send (str (.getName (Thread/currentThread)) " not too long " (.getTime (Date.))))))))

(defn performance [total]
  (doseq [i (range total)] 
    (future  (Thread/sleep 100) 
      (if (> 1  (rand-int 2))
        (send (str (.getName (Thread/currentThread)) (apply str (range 2000)) (.getTime (Date.))))
        (send (str (.getName (Thread/currentThread)) " not too long " (.getTime (Date.)))) 
        ))))
