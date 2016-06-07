#!/bin/bash

cd `dirname $0`

export URM_MODE=main

./urm.sh bin svnsave "$@"
