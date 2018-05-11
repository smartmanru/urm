#!/bin/bash
# params:
# URMDB_USER
# URMDB_PWD
# URMDB_DBHOST
# URMDB_DBNAME

export PGPASSWORD="$URMDB_PWD"
VALUE=`echo "select 'value=ok' as x;" | psql -d $URMDB_DBNAME -h $URMDB_DBHOST -U $URMDB_USER`

if [[ "$VALUE" =~ "value=ok" ]]; then
	exit 0
else
	exit 1
fi
