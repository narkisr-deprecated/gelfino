(ns gelfino.storm.spout
  (:use clojure.pprint
        [backtype.storm clojure config]  
        [gelfino streams bootstrap]))


(defmacro defgspout [name]
  `(do 
     (def ~'events (agent clojure.lang.PersistentQueue/EMPTY))
     (defspout ~name ["event"] {:prepare true}
        [conf# context# collector#]
        (start-processing "0.0.0.0" "12201")
        (~'spout
          (~'nextTuple []
             (if-let [event# (peek @~'events)]
               (do 
                 (send ~'events pop)
                 (emit-spout! collector# [event#]))))
          (~'ack [id#])))) 
  )  
;(pprint (macroexpand-1 '(defgpout foo))) 

