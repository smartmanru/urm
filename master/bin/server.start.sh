#!/bin/bash

cd `dirname $0`
nohup ./server.sh "$@" start > server.log 2>&1&
