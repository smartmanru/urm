#!/bin/bash

P_SCHEMA="$1"

. ./run.conf

if [ "$CONF_SETENV" != "" ]; then
	. $CONF_SETENV
fi

S_DATADIR=
S_LOGDIR=

XPORT=
if [ "$CONF_DBPORT" != "" ]; then
	XPORT=" -p $CONF_DBPORT"
fi

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
		) | psql $XPORT
	fi

	F_CMD="pg_dump -v -b $XPORT -f $S_DATADIR/data-$P_SCHEMA-all.dump -F c $F_TABLEFILTER $P_DBNAME"
	echo "run: $F_CMD ..."
	$F_CMD > $S_LOGDIR/data-$P_SCHEMA-all.dump.log 2>&1
	F_STATUS=$?

	# execute resume standby replication if any
	if [ "$CONF_STANDBY" = "yes" ]; then
		echo resume standby replication ...
		( 
			echo "select pg_is_xlog_replay_paused();"
			echo "select pg_xlog_replay_resume();"
			echo "select pg_is_xlog_replay_paused();"
		) | psql $XPORT
	fi

	if [ "$F_STATUS" != "0" ]; then
		echo pg_dump failed with status=$F_STATUS. Exiting
		exit 1
	fi

	echo EXPORT-FINISHED
}

function f_execute_all() {
	if [ "$CONF_NFS" = "yes" ]; then
		S_DATADIR=$CONF_NFSDATA
		S_LOGDIR=$CONF_NFSLOG
	else
		S_DATADIR=../data
		S_LOGDIR=../log
	fi

	echo "prepare export data to $S_DATADIR, logs to $S_LOGDIR ..."
	mkdir -p $S_DATADIR
	mkdir -p $S_LOGDIR

	# get schema name
	local F_DBNAME=`echo "$CONF_MAPPING" | tr " " "\n" | grep ^$P_SCHEMA= | cut -d "=" -f2`
	if [ "$F_DBNAME" = "" ]; then
		echo unable to find schema=$P_SCHEMA in run.conf in CONF_MAPPING variable. Exiting
		exit 1
	fi

	f_execute_db "$F_DBNAME"
}

f_execute_all
