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

CONFIG='.tx/transifex.yml'
IGNORED_FILE=".tx/ignored"

echo $RODA_PROJECT_DIR

function map {
    DIR=$1
    EXTENSION=$2
    TYPE=$3

    PARTIAL=partial.yml
    touch $PARTIAL

    find "$(readlink -m $DIR)" -type f -name \*${EXTENSION}  -regextype egrep  -not  -regex ".*/.+_[a-z]{2}_[A-Z]{2}\\${EXTENSION}" | while IFS= read -r file; do
      relativepath=${file#${RODA_PROJECT_DIR}}
      if ! grep -Fxq ${relativepath:1} $IGNORED_FILE
      then
        filename=$(basename "$file" | cut -f 1 -d '.')
        yq -i '.git.filters[0].filter_type="file"' $PARTIAL
        yq -i ".git.filters[0].file_format=\"$TYPE\"" $PARTIAL
        yq -i ".git.filters[0].source_file=\"$DIR/$filename$EXTENSION\"" $PARTIAL
        yq -i ".git.filters[0].source_language=\"$SOURCE_LANGUAGE\"" $PARTIAL
        yq -i ".git.filters[0].translation_files_expression=\"$DIR/${filename}_<lang>$EXTENSION\"" $PARTIAL

        yq -i eval-all '. as $item ireduce ({}; . *+ $item)' $CONFIG $PARTIAL

        rm $PARTIAL
        touch $PARTIAL
      fi
    done
    rm $PARTIAL
}

# Backup config
if [[ -f "$CONFIG" ]]; then
    cp $CONFIG ${CONFIG}_$(date -Iseconds).bak
    rm $CONFIG
    touch $CONFIG
else
  touch $CONFIG
fi


# Set server messages
yq -i '.git.filters[0].filter_type="file"' $CONFIG
yq -i '.git.filters[0].file_format="UNICODEPROPERTIES"' $CONFIG
yq -i ".git.filters[0].source_language=\"$SOURCE_LANGUAGE\"" $CONFIG
yq -i '.git.filters[0].source_file="roda-ui/roda-wui/src/main/resources/config/i18n/ServerMessages.properties"' $CONFIG
yq -i '.git.filters[0].translation_files_expression="roda-ui/roda-wui/src/main/resources/config/i18n/ServerMessages_<lang>.properties"' $CONFIG

# Set client messages
yq -i '.git.filters[1].filter_type="file"' $CONFIG
yq -i '.git.filters[1].file_format="UNICODEPROPERTIES"' $CONFIG
yq -i ".git.filters[1].source_language=\"$SOURCE_LANGUAGE\"" $CONFIG
yq -i '.git.filters[1].source_file="roda-ui/roda-wui/src/main/resources/config/i18n/client/ClientMessages.properties"' $CONFIG
yq -i '.git.filters[1].translation_files_expression="roda-ui/roda-wui/src/main/resources/config/i18n/client/ClientMessages_<lang>.properties"' $CONFIG

map "documentation" '.md' 'GITHUBMARKDOWN'
map "roda-ui/roda-wui/src/main/resources/config/theme" '.html' 'HTML'