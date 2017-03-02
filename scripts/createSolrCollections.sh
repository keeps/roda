#!/bin/bash

cd $(dirname $0)

INDEXES_PATH=../roda-core/roda-core/src/main/resources/config/index/
SOLR_BIN=../solr-5.5.3/bin/solr
for i in $(find $INDEXES_PATH -mindepth 1 -maxdepth 1 -type d);
do
        COLLECTION="$(basename $i)"
        $SOLR_BIN create -c "$COLLECTION" -d "$INDEXES_PATH/$COLLECTION/conf/" -p 8983
done
