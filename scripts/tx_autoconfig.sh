#!/bin/bash
# Depends on having Transifex CLI installed (>= 0.13.0)
# https://docs.transifex.com/client/installing-the-client

set -e

SCRIPT_DIR=$(dirname $(readlink -f $0))
RODA_PROJECT_DIR=$(readlink -f "${SCRIPT_DIR}/..")

echo "Changing to $RODA_PROJECT_DIR"

cd "$RODA_PROJECT_DIR"

PROJECT="roda2"
SOURCE_LANGUAGE="en"
CONFIG='.tx/config'

EXECUTE="$1"

function map {
  DIR=$1
  EXTENSION=$2
  TYPE=$3

  mkdir -p "locale/$DIR"
  find "$(readlink -m $DIR)" -type f -name \*${EXTENSION}  -regextype egrep  -not  -regex ".*/.+_[a-z]{2}_[A-Z]{2}\\${EXTENSION}" \
      -exec ln -s {} "locale/$DIR" \;


  tx config mapping-bulk -p $PROJECT --source-language $SOURCE_LANGUAGE \
    --type ${TYPE} -f ${EXTENSION} --source-file-dir "locale/$DIR" --expression "$DIR/{filename}_<lang>{extension}" \
    $EXECUTE

  rm -rf "locale"

}

# Backup config
cp $CONFIG ${CONFIG}_$(date -Iseconds).bak

# Set default messages

tx config mapping \
   -r $PROJECT.ServerMessages \
   -t UNICODEPROPERTIES \
   -s $SOURCE_LANGUAGE \
   -f roda-ui/roda-wui/src/main/resources/config/i18n/ServerMessages.properties \
   $EXECUTE \
   'roda-ui/roda-wui/src/main/resources/config/i18n/ServerMessages_<lang>.properties'


tx config mapping \
    -r $PROJECT.ClientMessages \
    -t UNICODEPROPERTIES \
    -s $SOURCE_LANGUAGE \
    -f roda-ui/roda-wui/src/main/resources/config/i18n/client/ClientMessages.properties \
    --minimum-perc 100 \
    $EXECUTE \
    'roda-ui/roda-wui/src/main/resources/config/i18n/client/ClientMessages_<lang>.properties'


map "documentation" '.md' 'GITHUBMARKDOWN'
map "roda-ui/roda-wui/src/main/resources/config/theme/" '.html' 'HTML'

if [[ ! -z  $EXECUTE ]]; then
  # fix config
  sed -r "s/\[${PROJECT}.locale_([[:alnum:]_-]+)\]/[${PROJECT}.\1]/" -i $CONFIG
  sed -r 's/source_file = locale\//source_file = /' -i $CONFIG

  # rename slugs
  sed -r 's/roda-ui_roda-wui_src_main_resources_config_theme/theme/' -i $CONFIG
fi
