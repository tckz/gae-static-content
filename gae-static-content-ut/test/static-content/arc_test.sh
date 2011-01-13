#!/bin/sh

# run on CentOS 5.5

mypath=`cd \`dirname $0\` && /bin/pwd`
path_script="$mypath/../../script"
path_documents=$mypath/public_html
path_out="$mypath/zips"
path_tmp="$mypath/temp"
prefix_zip=static-content-test

"$path_script"/arc_statics.sh -z "$prefix_zip" "$path_documents" "$path_out" "$path_tmp" 

