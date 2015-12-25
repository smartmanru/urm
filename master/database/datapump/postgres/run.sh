#!/bin/bash

P_CMD=$1
P_SET=$2
P_SCHEMASET="$3"

if [ "$P_CMD" = "" ] || [ "$P_SET" = "" ] || [ "$P_SCHEMASET" = "" ]; then
	echo invalid params. Exiting
	exit 1
fi

function f_execute_start() {
	echo EXPORT-STARTED > run.sh.log

	if [ "$P_SET" = "meta" ]; then
		nohup ./run-meta.sh "$P_SCHEMASET" >> run.sh.log 2>&1 &

	elif [ "$P_SET" = "data" ]; then
		nohup ./run-data.sh "$P_SCHEMASET" >> run.sh.log 2>&1 &
	fi
}

function f_execute_status() {
	if [ "`grep -c EXPORT-STARTED run.sh.log`" = "0" ]; then
		echo UNKNOWN
	fi

	if [ "`grep -c EXPORT-FINISHED run.sh.log`" = "0" ]; then
		echo RUNNING
	fi

	echo FINISHED
}

function f_execute_all() {
	if [ "$CMD" = "start" ]; then
		f_execute_start
	elif [ "$CMD" = "status" ]; then
		f_execute_status
	fi
}

f_execute_all
