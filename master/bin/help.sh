#!/bin/bash 

cd `dirname $0`

export C_URM_MODE=main

./urm.sh help "$@"
