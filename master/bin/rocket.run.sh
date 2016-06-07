#!/bin/sh

P_PROPS=$1

cd `dirname $0`

export URM_MODE=main

if [ ! -f "$P_PROPS" ]; then
	echo unknown property file=$P_PROPS. Exiting
	exit 1
fi

EXTRA_LIB="../lib"
JAVA_CLASSPATH=".:urms.jar:$EXTRA_LIB/json-simple-1.1.jar"
FNAME=$P_PROPS

echo "rocket-messenger: use $FNAME"
/usr/bin/nohup java -classpath "$JAVA_CLASSPATH" org.urm.messenger.ChatMain rocket $FNAME > rocket.log 2>&1&
echo "rocket-messenger: finished"
