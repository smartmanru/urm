#!/bin/bash 

cd `dirname $0`

../bin/urm.sh release getdist "$@"
