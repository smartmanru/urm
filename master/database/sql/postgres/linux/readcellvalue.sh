#!/bin/bash
# params:
# URMDB_USER
# URMDB_PWD
# URMDB_DBHOST
# URMDB_DBNAME

P_TABLE=$1
P_COLUMN=$2
P_CONDITION=$3

export PGPASSWORD="$URMDB_PWD"
VALUE=`echo "select 'value=$P_COLUMN as x from $P_TABLE where $P_CONDITION; ) | psql -A -q -t -d $URMDB_DBNAME -h $URMDB_DBHOST -U $URMDB_USER`

if [[ "$VALUE" =~ "ERROR:" ]]; then
	echo unexpected error: $VALUE
	exit 1
fi

echo $VALUE
exit 0
