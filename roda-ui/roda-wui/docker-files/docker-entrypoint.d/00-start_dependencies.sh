#!/bin/bash

echo "Starting supervisor"
supervisord -c /etc/supervisor/supervisord.conf
