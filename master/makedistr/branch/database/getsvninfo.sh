#!/bin/bash 

cd `dirname $0`

. ../_context.sh
../../../database/getsvninfo.sh "$@"
