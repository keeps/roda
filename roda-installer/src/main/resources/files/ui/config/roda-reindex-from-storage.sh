#!/bin/bash

cd $(dirname $0)

. set-roda-env.sh

RODA_WUI_LIB="${RODA_HOME}/webapps/roda-wui.war/WEB-INF/lib/"
LIBS="${RODA_HOME}/webapps/roda-wui.war/WEB-INF/classes/"

for i in $(ls $RODA_WUI_LIB);
do
	LIBS="$LIBS:${RODA_WUI_LIB}$i"
done

java -cp "$LIBS" -Droda.home="$RODA_HOME" pt.gov.dgarq.roda.common.ModelFactory reindex

