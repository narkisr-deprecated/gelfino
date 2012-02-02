(ns gelfino.statistics
  (:use 
   [clojure.tools.logging :only (trace info debug warn)]
   tron))

(def counters (agent {:total 0 :prev 0}))

(defn statistics [] (tron/do-periodically 5000 
  (let [{:keys [total prev]} @counters]
    (info (str "messages processed so far: " total))
    (info (str "current rate is: " (- total prev)))
    (send counters #(assoc % :prev (% :total ))))))

(defn inc-total [] 
  (send counters #(assoc % :total (-> % :total (+ 1)))))
