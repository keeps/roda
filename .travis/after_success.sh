#!/bin/bash

set -ex

################################################
# functions
################################################
function deploy_to_artifactory(){
  echo "Deploy to artifactory"
  cp settings.xml $HOME/.m2/
  mvn $MAVEN_CLI_OPTS clean package deploy -Dmaven.test.skip=true -Denforcer.skip=true -Pcore
  rm $HOME/.m2/settings.xml
}

function on_master(){
  echo "Executing on master logic"
  export DOCKER_TAG="latest"
  export RODA_DEV_BRANCH="master"
}

function on_development(){
  echo "Executing on development logic"
  deploy_to_artifactory

  execute_gren
}

function on_staging(){
  echo "Executing on staging logic"
  export DOCKER_TAG="$TRAVIS_BRANCH"
  export RODA_DEV_BRANCH="staging"
  docker tag keeps/roda:latest keeps/roda:$TRAVIS_BRANCH
}

function execute_gren(){
  echo "Generating release notes"
  # install nvm & node
  curl https://raw.githubusercontent.com/creationix/nvm/v0.33.8/install.sh | bash
  source ~/.nvm/nvm.sh
  nvm install v8.11.1
  npm install github-release-notes -g

  gren release --draft -I question -L closed,task
  gren changelog --overwrite
}

function on_tag(){
  echo "Executing on tag logic"
  export DOCKER_TAG="$TRAVIS_BRANCH"
  export RODA_DEV_BRANCH="master"
  docker tag keeps/roda:latest keeps/roda:$TRAVIS_BRANCH

  deploy_to_artifactory

  execute_gren
}

################################################
# main logic
################################################
# code coverage
bash <(curl -s https://codecov.io/bash)

# do login in docker hub
docker login -u "$DOCKER_USERNAME" -p "$DOCKER_PASSWORD"

if [ "$TRAVIS_BRANCH" == "master" ]; then
  on_master
elif [ "$TRAVIS_BRANCH" == "development" ]; then
  on_development
elif [ "$TRAVIS_BRANCH" == "staging" ]; then
  on_staging
elif [ "`echo $TRAVIS_BRANCH | egrep "^v[2-9]+" | wc -l`" -eq "1" ]; then
  on_tag
fi

# if docker tag is defined
if [ ! -z $DOCKER_TAG ]; then
  docker push keeps/roda:$DOCKER_TAG
  curl -# -o /dev/null -L --request POST --form ref=$RODA_DEV_BRANCH --form token=$GITLAB_RODA_DEV_TRIGGER_TOKEN --form "variables[DOCKER_TAG]=$DOCKER_TAG" $GITLAB_RODA_DEV_TRIGGER
fi

# clean up
docker logout
