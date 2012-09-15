# Intro

This is an example project that showcases how to use gelfino with systems like [fnordmetric](https://github.com/paulasmuth/fnordmetric).

# Perquisites 

* fnordmetric is Ruby based so [rvm](https://rvm.io/rvm/install/) and [bundler](http://gembundler.com/) are used to set it up, the .rvmrc uses ruby ruby-1.9.2-p320 so make sure to install it also.

* [redis](http://redis.io/) is used as the message queue into which gelfino writes and fnordmetric pull events from.

* [lein2](git://github.com/technomancy/leiningen.git) is the easiest way to get going with Clojure.


# Walk through
The following is how to get going on Ubuntu based systems:

```bash
$ git clone git://github.com/narkisr/gelfino.git

# install current version into local repo
$ lein install 

# Approve the rvm gemset creation
$ cd example

# Setting up rubygems
$ gem install bundle
$ bundle install 

# building client example jar
$ lein uberjar

# starting gelfino server and fnordmetric
$ foreman start

```

Now point your browser to this [url](http://localhost:4242), start up the client:

```clojure
$ lein repl
user => (use 'gelfino.example.client)
nil
user => (unicorn-seen-event)
nil
user => (four-errors-event)
nil
```

Each event should be visible on the Webui.

