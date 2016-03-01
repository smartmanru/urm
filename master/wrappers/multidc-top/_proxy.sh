#!/bin/bash

cd `dirname $0`
S_PROXY_SCRIPTDIR=`pwd`
		
P_PROXY_SCRIPT=$1
if [ "$P_PROXY_SCRIPT" = "" ]; then
	P_PROXY_SCRIPT is empty. Exiting
	exit 1
fi

shift 1

# set env context
. ./_context.sh

F_ACTION=${P_PROXY_SCRIPT%.sh}
../../bin/urm.sh deploy $F_ACTION $*
