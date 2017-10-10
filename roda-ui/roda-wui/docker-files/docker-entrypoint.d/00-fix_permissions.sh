#!/bin/bash

echo "Setting permissions on Tomcat and RODA home"
chown -R roda:roda /usr/local/tomcat
mkdir -p /roda
chown roda:roda /roda
