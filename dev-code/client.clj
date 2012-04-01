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

(defn gelf-send [m]
  (.sendGelfMessageToGraylog transport appender 
    (json-str
      {:facility "GELF" :full_message m :host host :level "INFO" :short_message m :version "1.0" :_unicorn "true"})))

(defn random-string [length]
  (let [ascii-codes (concat (range 48 58) (range 66 91) (range 97 123))]
    (apply str (repeatedly length #(char (rand-nth ascii-codes))))))

(def chunked-contents (doall  (random-string 12000)))

(defn performance [total]
  (doseq [i (range total)]  
    (future 
       (if (> (rand-int 2) 0)
          (gelf-send chunked-contents)
          (gelf-send "short message")))))

(defn periodicly [p]
 (while true
   (performance 10000)  
   (Thread/sleep (* p 60000))))

#_(clojure.repl/set-break-handler!)
