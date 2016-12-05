#!/bin/bash 

cd `dirname $0`

export C_URM_MODE=help

./urm.sh bin help "$@"
