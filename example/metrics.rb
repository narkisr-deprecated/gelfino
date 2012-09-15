require "fnordmetric"

FnordMetric.namespace :gelfino do

  event(:unicorn_seen) do
    incr :unicorns_seen_per_second,  1
  end

  event(:four_errors) do
    incr :four_errors_seen_per_hour , :via_gelfino , 1
  end

 gauge :unicorns_seen_per_second, :tick => 1.minute

  widget 'unicorns', {
    :title => "Events per Minute",
    :type => :timeline,
    :width => 100,
    :gauges => :unicorns_seen_per_second,
    :include_current => true,
    :autoupdate => 30
  }
end

FnordMetric.options = {
  :event_queue_ttl  => 10, 
  :event_data_ttl   => 10,
  :session_data_ttl => 1, 
  :redis_prefix => "fnordmetric" 
}


FnordMetric::Web.new(:port => 4242)
FnordMetric::Acceptor.new(:protocol => :tcp, :port => 2323)
FnordMetric::Worker.new
FnordMetric.run
