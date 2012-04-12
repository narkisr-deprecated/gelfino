class redis {
	group{"puppet":
	  ensure  => present
	}
  exec { 'apt-get update':
    command => '/usr/bin/apt-get update'
  }

  package { "redis-server":
    ensure => present,
  }

}

include redis
