#!/bin/bash

RODA_HOME=$1
JOB_REPORT_FOLDERS=$RODA_HOME/data/storage/job-report

find $JOB_REPORT_FOLDERS -type f -name "*.json" -exec \
  sed -i.bak 's/"sourceObjectOriginalId":""/"sourceObjectOriginalIds":[]/g;s/"sourceObjectOriginalId":"\([^"]*\)"/"sourceObjectOriginalIds":["\1"]/g' {} \;
