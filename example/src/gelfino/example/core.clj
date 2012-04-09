(ns gelfino.example.core
  (:require 
    [cljs-uuid.core :as uuid]  
    [cheshire.core :as cheshire]
    [redis.core :as redis])
  (:use (gelfino bootstrap streams)
         gelfino.drools.dsl
        [clojure.tools.logging :only (info)])
  (:gen-class) 
  )


(defn fnordic-even [type]
  (let [uuid  (uuid/make-v4)]
    (redis/with-server {:host "127.0.0.1" :port 6379 :db 15 }
      (redis/set (str "fnordmetric-event-" uuid) (cheshire/generate-string {:_type type}))
      (redis/expire (str "fnordmetric-event-" uuid)  60) 
      (redis/lpush "fnordmetric-queue" uuid))))

(defstream not-too-long :short_message #".*unicorn.*" (fnordic-even "unicorn_seen"))

(defstream level :level (fn [v] (= "INFO" v)) (fnordic-even "info"))

(defrule four-errors
   (when Number (> intValue 3) :from 
      (accumulate $message :> Message (== level 4) :over (window :time 1 m)
      :from (entry-point event-stream) (count $message)))
   (then (info "4 erros happend in 1 min")))

(defstream errors :rule four-errors)

(defn -main [host port]
  (start-processing host port))


