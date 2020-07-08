#!/bin/bash

set -x

# install siegfried
curl https://bintray.com/user/downloadSubjectPublicKey?username=bintray | sudo apt-key add -
echo "deb http://dl.bintray.com/siegfried/debian wheezy main" | sudo tee -a /etc/apt/sources.list
sudo apt-get -qq update
sudo apt-get -qq install siegfried -y
sudo sf -update


# Install dummy clam[d]scan
sudo cp .travis/clamscan /usr/bin/clamscan
sudo cp .travis/clamscan /usr/bin/clamdscan
sudo chmod a+rx /usr/bin/clamscan /usr/bin/clamdscan
