#!/bin/bash

cd `dirname $0`

export URM_MODE=main

echo run: ./urm.sh bin configure-linux "$@"
./urm.sh bin configure-linux "$@"
