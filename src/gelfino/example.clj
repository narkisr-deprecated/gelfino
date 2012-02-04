(ns gelfino.example
  (:gen-class)
  (:require 
   [cljs-uuid.core :as uuid]  
   [cheshire.core :as cheshire]
   [redis.core :as redis])
  (:use (gelfino bootstrap streams)))

#_(set! *warn-on-reflection* true)

(defn fnordic-even [type]
  (let [uuid  (uuid/make-v4)]
    (redis/with-server {:host "127.0.0.1" :port 6379 :db 15 }
      (redis/set (str "fnordmetric-event-" uuid) (cheshire/generate-string {:_type type}))
      (redis/expire (str "fnordmetric-event-" uuid)  60) 
      (redis/lpush "fnordmetric-queue" uuid))))

(defn -main [host port]
  (defstream not-too-long :short_message #".*unicorn.*" (fnordic-even "seen-unicorn"))
  (defstream level :level (fn [v] (= "INFO" v)) (fnordic-even "info"))
  (start-processing host port))

 
