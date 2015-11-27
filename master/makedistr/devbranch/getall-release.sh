#!/bin/bash 

cd `dirname $0`
. ./_context.sh
../getall-release.sh "$@"
