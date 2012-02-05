## Intro 

Gelfino is a tiny embeddable [Gelf](https://github.com/Graylog2/graylog2-docs/wiki/GELF) server written in pure Clojure, Gelfino enables real time processing of log events through streams.

Streams are filtered out of the main messages river and custom actions can be defined on any of them.

One main use case of Gelfino is as forwarding destination from [graylog2-server](https://github.com/Graylog2/graylog2-server), as an example we can stream forwarded events into [fnordmetric](https://github.com/paulasmuth/fnordmetric).

## Usage
Gelfino is meant  to be used as library within a Clojure project, we use a DSL to define our streams and actions.

### DSL:
```clojure
  (defstream <stream-name> <<message-key> <selector>> ... <action>) 
```

* stream-name: a logical name for the stream.
* message-key and selector pairs (can repeate multiple times):
  * message-key: the key within the message we filter upon.
  * selector: the predicate we filter the key upon.
* action: the action that will performed on selected messages.

### Selectors:

Selectors can be one of the following:

* A Clojure [regex form](http://clojure.org/other_functions#Other%20Useful%20Functions%20and%20Macros-Regex%20Support)   on which the field will be matched, in case of a match the messages is passed through the filter.
* A string, if the field contains the given string then it gets passed through.
* A predicate function that accepts a single value and returns true or false.

## Example:
This example showcases the forwarding of two types of events to fnordmetric, any message that matches the regex creates a matching event in the fnordmetric redis set, a second event is created for any message with the INFO level set.

### Code
```clojure
(ns gelfino.example
  (:require
   [cljs-uuid.core :as uuid]
   [cheshire.core :as cheshire]
   [redis.core :as redis])
  (:use (gelfino bootstrap streams)))


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
```

### Running

Server side:
```bash
$ git clone git@github.com:narkisr/gelfino.git 
$ cd example
$ lein deps
$ lein repl
user=> (use 'gelfino-example.core)
user=> (-main "0.0.0.0" "12201")
```

Client side (run from the project root):

```bash
$ lein deps
$ lein repl
user=> (use 'gelfino.client)
user=> (performance 20000); sending 20K messages localy
```

## Installation
Just add [com.narkisr/gelfino "0.1.0"] to [project.clj](https://github.com/technomancy/leiningen) file.

## License

License GPLv3

Copyright (C) 2012 narkisr.com


