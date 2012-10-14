(ns gelfino.storm.spout
  (:use clojure.pprint
        [gelfino.client :only (message-template)]
        [backtype.storm clojure config]  
        [gelfino streams bootstrap]))

(def tuple-keys (sort (keys message-template)))

(defn tuple-form []
  (into [] (map (comp str name) tuple-keys)))

(defn into-tuple [event] 
  (into [] (map #(% event) tuple-keys)))

(defmacro defgspout [name]
  `(do 
     (def ~'events (agent clojure.lang.PersistentQueue/EMPTY))
     (defspout ~name ~(tuple-form) {:prepare true}
        [conf# context# collector#]
        (start-processing "0.0.0.0" "12201")
        (~'spout
          (~'nextTuple []
             (if-let [event# (peek @~'events)]
               (do 
                 (send ~'events pop)
                 (emit-spout! collector# (into-tuple event# )))))
          (~'ack [id#])))) 
  )  


