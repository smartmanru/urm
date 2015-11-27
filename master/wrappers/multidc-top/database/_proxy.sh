#!/bin/bash

cd `dirname $0`
		
P_PROXY_SCRIPT=$1
if [ "$P_PROXY_SCRIPT" = "" ]; then
	P_PROXY_SCRIPT is empty. Exiting
	exit 1
fi

shift 1
