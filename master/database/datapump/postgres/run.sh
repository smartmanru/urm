#!/bin/bash

P_CMD=$1
P_SET=$2
P_SCHEMA="$3"

if [ "$P_CMD" = "" ]; then
	echo invalid params. Exiting
	exit 1
fi

function f_execute_data() {
	nohup ./run-meta.sh "$P_SCHEMA" >> run.sh.log 2>&1 &
}

function f_execute_meta() {
	nohup ./run-data.sh "$P_SCHEMA" >> run.sh.log 2>&1 &
}

function f_execute_start() {
	if [ "$P_SET" = "" ] || [ "$P_SCHEMA" = "" ]; then
		echo invalid params. Exiting
		exit 1
	fi

	echo EXPORT-STARTED > run.sh.log

	if [ "$P_SET" = "meta" ]; then
		f_execute_data

	elif [ "$P_SET" = "data" ]; then
		f_execute_meta
	fi
}

function f_execute_status() {
	if [ "`grep -c EXPORT-STARTED run.sh.log`" = "0" ]; then
		echo UNKNOWN
		return
	fi

	if [ "`pgrep -f pg_dump`" != "" ]; then
		echo RUNNING
		return
	fi

	if [ "`grep -c EXPORT-FINISHED run.sh.log`" = "0" ]; then
		echo BROKEN
		return
	fi

	echo FINISHED
}

function f_execute_all() {
	if [ "$P_CMD" = "start" ]; then
		f_execute_start
	elif [ "$P_CMD" = "status" ]; then
		f_execute_status
	else
		echo invalid command=$P_CMD
	fi
}

f_execute_all
