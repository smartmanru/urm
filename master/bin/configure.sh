#!/bin/bash

cd `dirname $0`

export C_URM_MODE=main

echo run: ./urm.sh bin configure-linux "$@"
./urm.sh bin configure-linux "$@"
