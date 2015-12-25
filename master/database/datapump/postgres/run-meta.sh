#!/bin/bash

P_SCHEMASET="$1"

function f_execute_one() {
	P_SCHEMA=$1

	echo "run pg_dump -s -f meta-$P_SCHEMA.dump -F c $P_SCHEMA ..."
	pg_dump -s -f ../data/meta-$P_SCHEMA.dump -F c $P_SCHEMA > ../log/meta-$P_SCHEMA.dump.log 2>&1
	F_STATUS=$?

	if [ "$F_STATUS" != "0" ]; then
		echo pg_dump failed with status=$F_STATUS. Exiting
		exit 1
	fi
}

function f_execute_all() {
	for schema in $P_SCHEMASET; do
		f_execute_one $schema
	done
	echo EXPORT-FINISHED
}

f_execute_all
