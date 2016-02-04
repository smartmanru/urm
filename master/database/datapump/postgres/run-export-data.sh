#!/bin/bash

P_SCHEMA="$1"

. ./run.conf

function f_execute_db() {
	local P_DBNAME=$1

	echo dump data schema=$P_SCHEMA dbname=$P_DBNAME ...

	# get table set
	F_TABLES=`cat tableset.txt | grep ^$P_DBNAME/ | cut -d "/" -f2 | tr "\n" " "`
	F_TABLES=${F_TABLES% }
	F_TABLES=${F_TABLES# }
	if [ "$F_TABLES" = "" ]; then
		echo ignore data schema=$P_SCHEMA due to empty tableset. Skipped.
		echo "EXPORT-EMPTY"
		exit 2
	fi

	F_TABLEFILTER=
	if [ "$F_TABLES" != "*" ]; then
		echo dump selected tables ...
		for table in $F_TABLES; do
			F_TABLEFILTER="$F_TABLEFILTER -t \"$table\""
		done
	else
		echo dump all schema tables ...
	fi

	# execute suspend standby replication if any
	if [ "$CONF_STANDBY" = "yes" ]; then
		echo suspend standby replication ...
		( 	echo "select pg_is_xlog_replay_paused();"
			echo "select pg_xlog_replay_pause();"
			echo "select pg_is_xlog_replay_paused();"
		) | psql
	fi

	F_CMD="pg_dump -v -b -f ../data/data-$P_SCHEMA-all.dump -F c $F_TABLEFILTER $P_DBNAME"
	echo "run: $F_CMD ..."
	$F_CMD > ../log/data-$P_SCHEMA-all.dump.log 2>&1
	F_STATUS=$?

	# execute resume standby replication if any
	if [ "$CONF_STANDBY" = "yes" ]; then
		echo resume standby replication ...
		( 
			echo "select pg_is_xlog_replay_paused();"
			echo "select pg_xlog_replay_pause();"
			echo "select pg_xlog_replay_resume();"
		) | psql
	fi

	if [ "$F_STATUS" != "0" ]; then
		echo pg_dump failed with status=$F_STATUS. Exiting
		exit 1
	fi

	echo EXPORT-FINISHED
}

function f_execute_all() {
	mkdir -p ../data
	mkdir -p ../log

	# get schema name
	local F_DBNAME=`echo "$CONF_MAPPING" | tr " " "\n" | grep ^$P_SCHEMA= | cut -d "=" -f2`
	if [ "$F_DBNAME" = "" ]; then
		echo unable to find schema=$P_SCHEMA in run.conf in CONF_MAPPING variable. Exiting
		exit 1
	fi

	f_execute_db "$F_DBNAME"
}

f_execute_all
