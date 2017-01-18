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

rm -rf "$PLUGINS_DST_DIR"
mkdir -p "$PLUGINS_DST_DIR"

for i in "${JARS[@]}"
do :
	SIZE=${#i}-4
	PLUGIN_FOLDER="$PLUGINS_DST_DIR/${i:0:$SIZE}"
	mkdir -p "$PLUGIN_FOLDER"
	cp -v -f  "$i" "$PLUGIN_FOLDER"
done
