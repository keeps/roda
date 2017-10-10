#!/bin/bash

echo "Starting supervisor"
/usr/bin/supervisord -c /etc/supervisor/conf.d/supervisord.conf
