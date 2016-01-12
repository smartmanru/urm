#!/bin/bash 

cd `dirname $0`

# top help
if [ "$1" = "" ]; then
	./bin/urm.sh help "$@"
	exit 0
fi

# category help
CATEGORY=$1
shift 1

./bin/urm.sh $CATEGORY help "$@"
