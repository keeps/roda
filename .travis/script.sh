#!/bin/bash

set -ex

################################################
# functions
################################################
function deploy_to_artifactory(){
  echo "Deploy to artifactory"
  cp .travis/settings.xml $HOME/.m2/
  mvn $MAVEN_CLI_OPTS clean package deploy -Dmaven.test.skip=true -Denforcer.skip=true -Pcore
  rm $HOME/.m2/settings.xml
}

function deploy_to_dockerhub(){
  echo "Deploy to docker hub"

  DOCKER_TAG=${1:-$TRAVIS_BRANCH}
  RODA_DEV_BRANCH=${2:-$TRAVIS_BRANCH}

  if [[ "$DOCKER_TAG" != "latest" ]]; then
    docker tag keeps/roda:latest keeps/roda:$TRAVIS_BRANCH
  fi

  # Push to https://hub.docker.com/r/keeps/roda/
  docker push keeps/roda:$DOCKER_TAG

  # Trigger external builds
  curl  --progress-bar -o /dev/null -L --request POST \
        --form ref=$RODA_DEV_BRANCH \
        --form token=$GITLAB_RODA_DEV_TRIGGER_TOKEN \
        --form "variables[DOCKER_TAG]=$DOCKER_TAG" \
        $GITLAB_RODA_DEV_TRIGGER
}


################################################
# Compile, test, code analysis
################################################
# temporary deactivating sonar
mvn $MAVEN_CLI_OPTS -Dtestng.groups="travis" -Denforcer.skip=true -Proda-wui-docker clean org.jacoco:jacoco-maven-plugin:prepare-agent install # sonar:sonar

################################################
# Deploy
################################################

if [[ ! -z "$DOCKER_USERNAME" ]]; then
  # init
  docker login -u "$DOCKER_USERNAME" -p "$DOCKER_PASSWORD"

  if [ "$TRAVIS_BRANCH" == "master" ]; then
    echo "Logic for master branch"
    deploy_to_dockerhub "latest" "$TRAVIS_BRANCH"

  elif [ "$TRAVIS_BRANCH" == "development" ]; then
    echo "Logic for development branch"
    deploy_to_dockerhub "$TRAVIS_BRANCH" "$TRAVIS_BRANCH"
    deploy_to_artifactory

  elif [ "$TRAVIS_BRANCH" == "staging" ]; then
    echo "Logic for staging branch"
    deploy_to_dockerhub "$TRAVIS_BRANCH" "staging"

  elif [ "`echo $TRAVIS_BRANCH | egrep "^v[2-9]+" | wc -l`" -eq "1" ]; then
    echo "Logic for tags"
    deploy_to_dockerhub "$TRAVIS_BRANCH" "master"
    deploy_to_artifactory
  fi

  # clean up
  docker logout
fi
