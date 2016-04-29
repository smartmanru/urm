#!/bin/bash
cd `dirname $0`

echo run: ./urm.sh bin configure-linux "$@"
./urm.sh bin configure-linux "$@"
