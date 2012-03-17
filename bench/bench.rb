require 'rubygems'
require 'jruby_threach'
require 'gelf'

logger = GELF::Logger.new('Uranus')

(0..1500).to_a.threach(3) {
  # each message results with 3 chunks
  logger.info( (0...6000).map{ ('a'..'z').to_a[rand(26)] }.join)
}
