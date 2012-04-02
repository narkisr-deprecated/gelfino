(ns gelfino.example
  (:gen-class)
  (:require 
   [cljs-uuid.core :as uuid]  
   [cheshire.core :as cheshire]
   [redis.core :as redis])
  (:use 
    clojure.pprint
    [clojure.tools.logging :only (info)]
    gelfino.drools.dsl 
    (gelfino bootstrap streams)))


(defn fnordic-even [type]
  (let [uuid  (uuid/make-v4)]
    (redis/with-server {:host "127.0.0.1" :port 6379 :db 15 }
      (redis/set (str "fnordmetric-event-" uuid) (cheshire/generate-string {:_type type}))
      (redis/expire (str "fnordmetric-event-" uuid)  60) 
      (redis/lpush "fnordmetric-queue" uuid))))

(defstream unicorns :short_message #".*unicorn.*" (fnordic-even "seen-unicorn"))

(defstream level :level (fn [v] (= "INFO" v)) (fnordic-even "info"))

(defrule inf-rule
   (when $message :> Message (== level "INFO" ) 
      :from (entry-point "event-stream"))
   (then 
     (info "info detected by drools")))

;(pprint (macroexpand '(defstream inf :rule infos)))
(defstream informantion :rule inf-rule)


(defn -main [host port]
  (start-processing host port))

(-main "0.0.0.0" "12201") 
