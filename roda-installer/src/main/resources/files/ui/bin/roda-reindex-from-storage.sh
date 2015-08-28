#!/bin/bash

cd $(dirname $0)

. set-roda-env.sh

RODA_WUI_WEBINF=$(find ${RODA_HOME}/tomcat -name WEB-INF | egrep ROOT)
RODA_WUI_LIB="${RODA_WUI_WEBINF}/lib/"
LIBS="${RODA_WUI_WEBINF}/classes/"

for i in $(ls $RODA_WUI_LIB);
do
	case $i in
		*roda*|*w3c*|*solr*|*log4j*|*slf4j*|commons*|*zookeeper*|*lucene*|*guava*|*http*|*servlet*)
			LIBS="$LIBS:${RODA_WUI_LIB}$i"
		;;
	esac
done

java -cp "$LIBS" -Droda.home="$RODA_HOME" pt.gov.dgarq.roda.common.RodaCoreFactory reindex
