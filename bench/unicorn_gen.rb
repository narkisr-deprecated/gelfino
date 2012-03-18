require 'rubygems'
require 'jruby_threach'
require 'gelf'

logger = GELF::Logger.new('Uranus')

(0..1500).to_a.threach(3) {
  logger.info("seen a unicorn ")
  sleep 1
}
