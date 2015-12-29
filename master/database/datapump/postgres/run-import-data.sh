#!/bin/bash

P_SCHEMA="$1"

. ./run.conf

function f_execute_db() {
	local P_DBNAME=$1

	echo load data schema=$P_SCHEMA dbname=$P_DBNAME ...

	# get table set
	F_TABLES=`cat tableset.txt | grep ^$P_DBNAME/ | cut -d "/" -f2 | tr "\n" " "`
	F_TABLES=${F_TABLES% }
	F_TABLES=${F_TABLES# }
	if [ "$F_TABLES" = "" ]; then
		echo invalid call due to empty tableset. Exiting
		exit 1
	fi

	F_TABLEFILTER=
	if [ "$F_TABLES" != "*" ]; then
		echo load selected tables ...
		for table in $F_TABLES; do
			F_TABLEFILTER="$F_TABLEFILTER -t \"$table\""
		done
	else
		echo load all schema tables ...
	fi

	F_CMD="pg_restore -v -a -j 4 --disable-triggers -d $P_DBNAME $F_TABLEFILTER ../data/data-$P_SCHEMA-all.dump"
	echo "run: $F_CMD ..."
	$F_CMD > ../log/data-$P_SCHEMA-all.dump.log 2>&1
	F_STATUS=$?

	if [ "$F_STATUS" != "0" ]; then
		echo pg_dump failed with status=$F_STATUS. Exiting
		exit 1
	fi

	echo IMPORT-FINISHED
}

function f_execute_all() {
	# get schema name
	local F_DBNAME=`echo "$CONF_MAPPING" | tr " " "\n" | grep ^$P_SCHEMA= | cut -d "=" -f2`
	if [ "$F_DBNAME" = "" ]; then
		echo unable to find schema=$P_SCHEMA in run.conf in CONF_MAPPING variable. Exiting
		exit 1
	fi

	f_execute_db "$F_DBNAME"
}

f_execute_all
