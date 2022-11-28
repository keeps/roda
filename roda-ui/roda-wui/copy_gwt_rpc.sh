#!/bin/bash
SCRIPT_DIR=$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )
cd "$SCRIPT_DIR"

for MODULE_PATH in $(find $PWD/target/roda-wui-?.?.?*/ -mindepth 1 -maxdepth 1 -type d -name 'org.roda.wui.*'); do
    MODULE_NAME=$(basename "$MODULE_PATH")
    mkdir -p "src/main/webapp/$MODULE_NAME/"
    find "$MODULE_PATH" -name '*.gwt.rpc' -exec cp -v {} "src/main/webapp/$MODULE_NAME/" \;
done
