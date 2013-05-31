#!/bin/sh

scriptdir=`dirname "$0"`

handle_bin_dir=$scriptdir
handle_data_dir=$handle_bin_dir/../data

CLASSPATH=$handle_bin_dir/admintool.jar:$handle_bin_dir/handle.jar

java -cp $CLASSPATH -jar $handle_bin_dir/admintool.jar

