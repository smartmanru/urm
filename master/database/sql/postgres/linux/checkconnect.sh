#!/bin/bash
# params:
# URMDB_USER
# URMDB_PWD
# URMDB_DBHOST (host[:port])
# URMDB_DBNAME

export PGPASSWORD="$URMDB_PWD"
XHOST=${URMDB_DBHOST%:*}
XPORT=
if [ "$XHOST" != "$URMDB_DBHOST" ]; then
	XPORT=" -p ${URMDB_DBHOST#*:}"
fi

VALUE=`echo "select 'value=ok' as x;" | psql -d $URMDB_DBNAME -h $XHOST $XPORT -U $URMDB_USER`

if [[ "$VALUE" =~ "value=ok" ]]; then
	exit 0
else
	exit 1
fi
