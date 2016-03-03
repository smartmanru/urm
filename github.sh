#!/bin/bash

P_RELEASE=$1

if [ "$P_RELEASE" = "" ]; then
	echo P_RELEASE not set. Exiting
	exit 1
fi

S_URMHOME=~/jurm
S_URMMIRROR=~/mirror/urm.git

function f_execute_all() {
	# check release
	cd ..
	if [ ! -f "releases/$P_RELEASE/master.tar" ]; then
		echo "redist.sh: unknown release $P_RELEASE. Exiting"
		exit 1
	fi

	# get release
	local F_CURDIR=`pwd`
	local F_DIR_MASTER=$F_CURDIR/releases/$P_RELEASE
	local F_DIR_TMP=$F_CURDIR/urm.git

	echo "github.sh: upload release $P_RELEASE to github ..."
	rm -rf $F_DIR_TMP
	git clone $S_URMMIRROR --shared -b master $F_DIR_TMP

	mkdir -p $F_DIR_TMP
	cd $F_DIR_TMP
	tar xf $F_DIR_MASTER/master.tar > /dev/null
	git add `git diff --name-only`
	git commit -m "push updates"
	git push origin
	git -C $S_URMMIRROR push origin

	rm -rf $F_DIR_TMP
}

f_execute_all
