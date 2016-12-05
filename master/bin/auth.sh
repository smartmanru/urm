#!/bin/bash 

cd `dirname $0`

export C_URM_MODE=client

./urm.sh bin auth "$@"
