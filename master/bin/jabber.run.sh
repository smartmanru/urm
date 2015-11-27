#!/bin/sh

P_JABBER_PROPS=$1

cd `dirname $0`

if [ ! -f "$P_JABBER_PROPS" ]; then
	echo unknown property file=$P_JABBER_PROPS. Exiting
	exit 1
fi

EXTRA_LIB="../lib"
JAVA_CLASSPATH=".:urm.jar:$EXTRA_LIB/smack-3.4.1-0cec571.jar:$EXTRA_LIB/smackx-3.4.1-0cec571.jar"
FNAME=$P_JABBER_PROPS

echo "jabber-messenger: use $FNAME"
/usr/bin/nohup java -classpath "$JAVA_CLASSPATH" ru.egov.urm.messenger.jabber.JabberMain $FNAME > jabber.log 2>&1&
echo "jabber-messenger: finished"
