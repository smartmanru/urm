#!/bin/bash
# params:
# URMDB_USER
# URMDB_PWD
# URMDB_DBHOST (host[:port])
# URMDB_DBNAME

P_SQLFILE=$1
P_LOGFILE=$2

XHOST=${URMDB_DBHOST%:*}
XPORT=
if [ "$XHOST" != "$URMDB_DBHOST" ]; then
	XPORT=" -p ${URMDB_DBHOST#*:}"
fi

psql -a -e $XPORT < $P_SQLFILE > $P_LOGFILE 2>&1
