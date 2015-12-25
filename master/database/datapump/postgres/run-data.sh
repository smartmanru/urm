#!/bin/bash

P_SCHEMA="$1"

function f_execute_all() {
	# get table set
	F_TABLES=`cat tableset.txt | grep ^$P_SCHEMA/ | cut -d "/" -f2 | tr "\n" " "`
	F_TABLES=${F_TABLES% }
	F_TABLES=${F_TABLES# }
	if [ "$F_TABLES" = "" ]; then
		echo invalid call due to empty tableset. Exiting
		exit 1
	fi

	F_TABLEFILTER=
	if [ "$F_TABLES" != "*" ]; then
		for table in $F_TABLES; do
			F_TABLEFILTER="$F_TABLEFILTER -t \"$table\""
		done
	fi

	F_CMD="pg_dump -b -f ../data/data-$P_SCHEMA.dump -F c $F_TABLEFILTER $P_SCHEMA"
	echo "run: $F_CMD ..."
	$F_CMD > ../log/data-$P_SCHEMA.dump.log 2>&1
	F_STATUS=$?

	if [ "$F_STATUS" != "0" ]; then
		echo pg_dump failed with status=$F_STATUS. Exiting
		exit 1
	fi

	echo EXPORT-FINISHED
}

f_execute_all
