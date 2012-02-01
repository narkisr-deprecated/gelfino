(defproject gelfino "1.0.0-SNAPSHOT"
  :description "An extendable Gelf server"
  :dependencies [[org.clojure/data.json "0.1.1"]  
                 [org.clojure/tools.logging "0.2.3"]  
                 [lamina "0.4.1-alpha1"] 
                 [org.clojure/clojure "1.3.0"]
                 [org.clojure/core.match "0.2.0-alpha9"]
                 [tron   "0.5.2"]]
  
:dev-dependencies [[org.codehaus.groovy/groovy "1.7.8"]
                   [org.graylog2/gelf4j "0.86"]
                   [log4j/log4j "1.2.16"] 
                   ]
  )
