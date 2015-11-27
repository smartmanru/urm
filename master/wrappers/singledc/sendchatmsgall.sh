#!/bin/bash

cd `dirname $0`
RUNPATH=`pwd`

cd ..
./sendchatmsg.sh "$@"
