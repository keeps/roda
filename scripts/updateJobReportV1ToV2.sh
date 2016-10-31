#!/bin/sh

RODA_HOME=$1
JOB_REPORT_FOLDERS=$RODA_HOME/data/storage/job-report

find $JOB_REPORT_FOLDERS -type f -name "*.json" -print0 | xargs -0 sed -i.bak 's/"sourceObjectOriginalId":""/"sourceObjectOriginalIds":[]/g;s/"sourceObjectOriginalId":"\([^"]*\)"/"sourceObjectOriginalIds":["\1"]/g'
