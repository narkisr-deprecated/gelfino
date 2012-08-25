(defproject com.narkisr/gelfino "0.3.1"
  :description "An embeddable Gelf server library"
  :dependencies [[org.clojure/data.json "0.1.1"]  
                 [org.clojure/tools.logging "0.2.3"]  
                 [lamina "0.4.1-alpha1"] 
                 [org.clojure/clojure "1.3.0"]
                 [org.clojure/core.incubator "0.1.1"]
                 [org.clojure/core.match "0.2.0-alpha9"]
                 [log4j/log4j "1.2.16"]
                 [tron "0.5.2"]
                 [cheshire "3.1.0"]
                 [org.drools/drools-core "5.3.0.Final"]
                 [org.drools/drools-compiler "5.3.0.Final"]
                 [org.drools/knowledge-api "5.3.0.Final"]
                 [org.drools/drools-decisiontables "5.3.0.Final"]
                 [aot-match/aot-match "0.2.0-alpha10-aot"]
                 [com.sun.xml.bind/jaxb-xjc "2.2-EA"]]

  :profiles {:dev 
             {:dependencies 
              [[lein-clojars "0.6.0"]  
               [org.codehaus.groovy/groovy "1.7.8"]
               [cljs-uuid "0.0.2"]
               [com.narkisr/java.data "0.0.1-SNAPSHOT"]
               [org.clojars.tavisrudd/redis-clojure "1.3.1-SNAPSHOT"]]}}

  :plugins [[lein-tarsier "0.9.3"]] 

  :aot [gelfino.drools.bridging]
  :main gelfino.example
  :resource-paths ["src/main/resources"]
  :repositories {"sonatype-oss-public" "https://oss.sonatype.org/content/groups/public/" 
                 "jboss" "http://repository.jboss.org/nexus/content/groups/public/"}
  )
