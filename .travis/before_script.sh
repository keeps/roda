#!/bin/bash

set -ex

# install dependencies
curl https://bintray.com/user/downloadSubjectPublicKey?username=bintray | sudo apt-key add -
echo "deb http://dl.bintray.com/siegfried/debian wheezy main" | sudo tee -a /etc/apt/sources.list
sudo apt-get -qq update
sudo apt-get -qq install siegfried -y
sudo sf -update
sudo apt-get -qq install clamav clamav-daemon -y
sudo freshclam

# decrypt maven setting.xml
openssl aes-256-cbc -K $encrypted_a8a9ca6bf122_key -iv $encrypted_a8a9ca6bf122_iv -in .travis/settings.xml.enc -out settings.xml -d
