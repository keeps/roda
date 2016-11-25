#!/bin/bash

cd "$(dirname "$0")"

PLUGINS_SRC_DIR="../roda-core/roda-plugins/"
PLUGINS_DST_DIR="../jar_plugins/"

JARS_WITH_DEPENDENCIES=(`find $PLUGINS_SRC_DIR -name roda-plugin-\*-jar-with-dependencies.jar`)
ALL_JARS=(`find $PLUGINS_SRC_DIR -name roda-plugin-\*.jar`)

JARS=("${ALL_JARS[@]}")

for i in "${JARS_WITH_DEPENDENCIES[@]}"
do :
	SIZE=${#i}-26
	FILE="${i:0:$SIZE}.jar"

	JARS=(${JARS[@]/$FILE})

done

mkdir -p "$PLUGINS_DST_DIR"

for i in "${JARS[@]}"
do :
	cp -v -f  "$i" "$PLUGINS_DST_DIR"
done
