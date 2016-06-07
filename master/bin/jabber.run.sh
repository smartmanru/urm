#!/bin/sh

P_PROPS=$1

cd `dirname $0`

export C_URM_MODE=main

if [ ! -f "$P_PROPS" ]; then
	echo unknown property file=$P_PROPS. Exiting
	exit 1
fi

EXTRA_LIB="../lib"
JAVA_CLASSPATH=".:urms.jar:$EXTRA_LIB/smack-3.4.1-0cec571.jar:$EXTRA_LIB/smackx-3.4.1-0cec571.jar"
FNAME=$P_PROPS

echo "jabber-messenger: use $FNAME"
/usr/bin/nohup java -classpath "$JAVA_CLASSPATH" org.urm.messenger.ChatMain jabber $FNAME > jabber.log 2>&1&
echo "jabber-messenger: finished"
