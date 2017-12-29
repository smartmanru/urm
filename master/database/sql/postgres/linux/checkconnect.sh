#!/bin/bash
# params:
# CONF_USER
# CONF_PWD
# CONF_DBHOST
# CONF_DBPORT
# CONF_DBNAME

export PGPASSWORD="$CONF_PWD"
XPORT=
if [ "$CONF_DBPORT" != "" ]; then
	XPORT=" -p $CONF_DBPORT"
fi

VALUE=`echo "select 'value=ok' as x;" | psql -d $CONF_DBNAME -h $CONF_DBHOST $XPORT -U $CONF_USER`

if [[ "$VALUE" =~ "value=ok" ]]; then
	exit 0
else
	exit 1
fi
