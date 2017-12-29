#!/bin/bash

P_SCHEMA="$1"

. ./run.conf

if [ "$CONF_SETENV" != "" ]; then
	. $CONF_SETENV
fi

S_DATADIR=
S_LOGDIR=

function f_execute_one() {
	local P_SCHEMA=$1
	local P_DBNAME=$2

	echo load meta schema=$P_SCHEMA dbname=$P_DBNAME ...

	# drop old
	local F_STATUS
	F_LOG=$S_LOGDIR/meta-$P_SCHEMA.dump.log

	echo "# drop schema=$P_SCHEMA using run-import-drop.sql ..." > $F_LOG
	psql -d $P_DBNAME < run-import-drop.sql >> $F_LOG 2>&1
	F_STATUS=$?
	if [ "$F_STATUS" != "0" ]; then
		echo drop schema failed with status=$F_STATUS. Exiting
		exit 1
	fi

	local F_CMD="pg_restore -v -s -j 4 -d $P_DBNAME $S_DATADIR/meta-$P_SCHEMA.dump"
	echo "# load dump: $F_CMD ..." >> $F_LOG
	$F_CMD >> $F_LOG 2>&1
	F_STATUS=$?

	if [ "$F_STATUS" != "0" ]; then
		echo pg_restore failed with status=$F_STATUS. Exiting
		exit 1
	fi
}

function f_execute_roles() {
	local F_DATA=$S_DATADIR/meta-roles.dump
	local F_LOG=$S_LOGDIR/meta-roles.dump.log

	if [ ! -f "$F_DATA" ]; then
		echo unable to find role set $F_DATA. Exiting
		exit 1
	fi

	local F_CMD="psql -f $F_DATA"
	echo "run: $F_CMD ..."
	$F_CMD > $F_LOG 2>&1
	F_STATUS=$?
	if [ "$F_STATUS" != "0" ]; then
		echo psql failed with status=$F_STATUS. Ignored.
	fi

	echo roles are successfully applied.
}

function f_execute_all() {
	if [ "$CONF_NFS" = "yes" ]; then
		S_DATADIR=$CONF_NFSDATA
		S_LOGDIR=$CONF_NFSLOG
	else
		S_DATADIR=../data
		S_LOGDIR=../log
	fi

	echo "prepare import meta from $S_DATADIR, logs to $S_LOGDIR ..."
	mkdir -p $S_DATADIR
	mkdir -p $S_LOGDIR

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

	# imports roles
	if [ "$P_SCHEMA" = "all" ]; then
		f_execute_roles
	fi

	local F_DBNAME
	for schema in $F_SCHEMASET; do
		local F_DBNAME=`echo "$CONF_MAPPING" | tr " " "\n" | grep ^$schema= | cut -d "=" -f2`
		f_execute_one "$schema" "$F_DBNAME"
	done
	echo IMPORT-FINISHED
}

f_execute_all
