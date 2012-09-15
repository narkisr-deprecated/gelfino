(ns gelfino.example.client
  (:use [gelfino.client :only (connect send->)]))

(connect)

(defn four-errors-event []
  (doseq [i (range 4)]  
    (send-> "localhost" {:short_message "Hmm" :message "something went unicorn" :level 4})))

(defn unicorn-seen-event []
  (send-> "localhost" {:short_message "a unicorn" :message "A white unicorn has just been seen!" :level 4}))

