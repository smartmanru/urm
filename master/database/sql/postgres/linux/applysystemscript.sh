#!/bin/bash
# params:
# CONF_USER
# CONF_PWD
# CONF_DBHOST
# CONF_DBPORT
# CONF_DBNAME

P_SQLFILE=$1
P_LOGFILE=$2

XPORT=
if [ "$CONF_DBPORT" != "" ]; then
	XPORT=" -p $CONF_DBPORT"
fi

psql -a -e $XPORT < $P_SQLFILE > $P_LOGFILE 2>&1
