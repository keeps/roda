#!/bin/bash

cd "$(dirname "$0")"

JARS_WITH_DEPENDENCIES=(`find roda-core/roda-plugins/ -name roda-plugin-\*-jar-with-dependencies.jar`)
ALL_JARS=(`find roda-core/roda-plugins/ -name roda-plugin-\*.jar`)

JARS=("${ALL_JARS[@]}")

for i in "${JARS_WITH_DEPENDENCIES[@]}"
do :
	SIZE=${#i}-26
	FILE="${i:0:$SIZE}.jar"

	JARS=(${JARS[@]/$FILE})

done

mkdir -p jar_plugins

for i in "${JARS[@]}"
do :
	cp -v -f  "$i" jar_plugins/
done
