#!/bin/bash

cd `dirname $0`

export C_URM_MODE=main

URM_TRACE=
if [ "$1" = "-trace" ]; then
	URM_TRACE="-trace"
	P_DSTDIR=$2
else
	P_DSTDIR=$1
fi

# check target dir
if [ "$P_DSTDIR" = "" ]; then
	echo "P_DSTDIR is empty. Exiting"
	exit 1
fi
if [ ! -d "$P_DSTDIR/master" ]; then
	echo "upgrade.sh: invalid product URM directory $P_DSTDIR . Exiting"
	exit 1
fi

function f_execute_all() {
	# copy master files
	cd ..
	cp -R * $P_DSTDIR/master
	local F_STATUS=$?
	if [ "$F_STATUS" != "0" ]; then
		echo "upgrade.sh: cannot copy master to $P_DSTDIR/master . Exiting"
		exit 1
	fi

	# execute configuration script
	local F_SAVEDIR=`pwd`
	cd $P_DSTDIR/master
	if [ -f bin/configure.sh ]; then
		echo "run: configure.sh $URM_TRACE default ..."
		bin/configure.sh $URM_TRACE default

		F_STATUS=$?
		if [ "$F_STATUS" != "0" ]; then
			echo "upgrade.sh: confugure.sh failed. Exiting"
			exit 1
		fi
	fi
}

f_execute_all

echo upgrade.sh: successfully done
