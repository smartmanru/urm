#!/bin/bash

. ./run.conf

function f_execute_one() {
	P_SCHEMA=$1
	P_DBNAME=$2

	echo load meta schema=$P_SCHEMA dbname=$P_DBNAME ...

	F_CMD="pg_restore -s -c -j 4 -d $P_DBNAME ../data/meta-$P_SCHEMA.dump"
	echo "run: $F_CMD ..."
	$F_CMD > ../log/meta-$P_SCHEMA.dump.log 2>&1
	F_STATUS=$?

	if [ "$F_STATUS" != "0" ]; then
		echo pg_dump failed with status=$F_STATUS. Exiting
		exit 1
	fi
}

function f_execute_all() {
	# get schema names
	local F_SCHEMASET=`echo "$CONF_MAPPING" | tr " " "\n" | cut -d "=" -f1 | tr "\n" " "`
	F_SCHEMASET="${F_SCHEMASET# }
	F_SCHEMASET="${F_SCHEMASET% }
	if [ "$F_SCHEMASET" = "" ]; then
		echo unable to find schema set in run.conf in CONF_MAPPING variable. Exiting
		exit 1
	fi

	local F_DBNAME
	for schema in $F_SCHEMASET; do
		local F_DBNAME=`echo "$CONF_MAPPING" | tr " " "\n" | grep ^$P_SCHEMA= | cut -d "=" -f2`
		f_execute_one "$schema" "$F_DBNAME"
	done
	echo IMPORT-FINISHED
}

f_execute_all
