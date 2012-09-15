(ns gelfino.example.core
  (:gen-class)
  (:require 
    [cljs-uuid.core :as uuid]  
    [cheshire.core :as cheshire]
    [taoensso.carmine :as redis])
  (:use (gelfino bootstrap streams)
        gelfino.drools.dsl
        [clojure.tools.logging :only (info)]))


(def pool (redis/make-conn-pool :max-active 8))

(def spec-server1 
  (redis/make-conn-spec :host "127.0.0.1" :port 6379 :timeout  4000))

(defmacro carmine
  "Acts like (partial with-conn pool spec-server1)."
  [& body] `(redis/with-conn pool spec-server1 ~@body))

(defn fnordic-even [type]
  (let [uuid  (uuid/make-v4) prefix "fnordmetric"]
    (carmine
      (redis/hincrby (str prefix "-testdata") "events_received" 1)
      (redis/hincrby (str prefix "-stats") "events_received" 1)
      (redis/set (str prefix "-event-" uuid) (cheshire/generate-string {:_type type}))
      (redis/lpush (str prefix "-queue") (str uuid))
      )))

(defstream unicorns :short_message #".*unicorn.*" (fnordic-even "unicorn_seen"))

(defrule four-errors
  (when Number (> intValue 3) :from 
    (accumulate $message :> Message (== level 4) :over (window :time 1 m)
                :from (entry-point event-stream) (count $message)))
  (then (fnordic-even "four_errors")))

(defstream errors :rule four-errors)

(defn -main [host port]
  (start-processing host port))


