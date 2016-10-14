#!/bin/bash 

JARLIST=(`find . -name roda-plugin-\*.jar | grep 'with-dependencies'`)
COMMAND=" "

for i in "${JARLIST[@]}"
do :
	size=${#i}-26
	file="${i:0:$size}.jar"
    COMMAND="$COMMAND ! -name '${file##*/}'"
done

LIST=(`find . -name roda-plugin-\*.jar $COMMAND | grep -v roda-plugin-common`)

`mkdir plugins`

for i in "${LIST[@]}"
do :
	`cp $i plugins/`
done