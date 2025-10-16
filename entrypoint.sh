#!/bin/bash
# Create /var/run/squid and set ownership
mkdir -p /var/run/squid
chown appuser:appuser /var/run/squid

# Start Squid and run Java app as appuser
service squid start
exec java org.springframework.boot.loader.launch.JarLauncher