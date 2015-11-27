#!/bin/bash 

cd `dirname $0`
. ./_context.sh
../buildall-release.sh "$@"
