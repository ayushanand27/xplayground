# Puppet manifest to install Java 17 and Maven on a Debian/Ubuntu-like Linux agent
# Adjust package names as needed for your distro.

package { 'openjdk-17-jdk':
  ensure => installed,
}

package { 'maven':
  ensure => installed,
  require => Package['openjdk-17-jdk'],
}
