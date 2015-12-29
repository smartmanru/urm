#!/bin/bash

P_SCHEMA="$1"

. ./run.conf

function f_execute_one() {
	local P_SCHEMA=$1
	local P_DBNAME=$2

	echo load meta schema=$P_SCHEMA dbname=$P_DBNAME ...

	# drop old
	local F_STATUS
	F_LOG=../log/meta-$P_SCHEMA.dump.log
	psql -d $P_DBNAME < run-import-drop.sql > $F_LOG 2>&1
	F_STATUS=$?
	if [ "$F_STATUS" != "0" ]; then
		echo drop schema failed with status=$F_STATUS. Exiting
		exit 1
	fi

	local F_CMD="pg_restore -s -j 4 -d $P_DBNAME ../data/meta-$P_SCHEMA.dump"
	echo "run: $F_CMD ..." >> $F_LOG
	$F_CMD >> $F_LOG 2>&1
	F_STATUS=$?

	if [ "$F_STATUS" != "0" ]; then
		echo pg_dump failed with status=$F_STATUS. Exiting
		exit 1
	fi
}

function f_execute_all() {
	# get schema names
	local F_SCHEMASET
	if [ "$P_SCHEMA" = "all" ]; then
		F_SCHEMASET=`echo "$CONF_MAPPING" | tr " " "\n" | cut -d "=" -f1 | tr "\n" " "`
		F_SCHEMASET=${F_SCHEMASET# }
		F_SCHEMASET=${F_SCHEMASET% }
		if [ "$F_SCHEMASET" = "" ]; then
			echo unable to find schema set in run.conf in CONF_MAPPING variable. Exiting
			exit 1
		fi
	else
		F_SCHEMASET="$P_SCHEMA"
	fi

	local F_DBNAME
	for schema in $F_SCHEMASET; do
		local F_DBNAME=`echo "$CONF_MAPPING" | tr " " "\n" | grep ^$schema= | cut -d "=" -f2`
		f_execute_one "$schema" "$F_DBNAME"
	done
	echo IMPORT-FINISHED
}

f_execute_all
