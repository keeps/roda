#!/bin/bash

set -ex

# Enable Clamav database cache 
chmod -R a+r "$CLAMAV_DATABASE"

# code coverage
# bash <(curl -s https://codecov.io/bash)
