#!/bin/sh

scriptdir=`dirname "$0"`
handle_bin_dir=$scriptdir
handle_data_dir=$handle_bin_dir/../data

java -cp $handle_bin_dir/handle.jar net.handle.server.SimpleSetup $handle_data_dir

