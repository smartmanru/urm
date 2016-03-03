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
	local F_DIR_GIT=$F_CURDIR/urm.git
	local F_DIR_REL=$F_CURDIR/urm.rel

	echo "github.sh: upload release $P_RELEASE to github ..."
	rm -rf $F_DIR_GIT $F_DIR_REL
	git clone $S_URMMIRROR --shared -b master $F_DIR_GIT
	mkdir -p $F_DIR_REL
	cd $F_DIR_REL
	tar xf $F_DIR_MASTER/master.tar > /dev/null

	# copy release over git
	cp -R * $F_DIR_GIT/

	cd $F_DIR_GIT
	local F_NEW=`git ls-files --others --exclude-standard`
	if [ "$F_NEW" != "" ]; then
		git add $F_NEW
	fi

	local F_CHANGED=`git diff --name-only`
	if [ "$F_CHANGED" != "" ]; then
		git add $F_CHANGED
	fi

	# note: remove deleted manually
	git commit -m "push updates"
	git push origin
	git -C $S_URMMIRROR push origin

	cd $F_CURDIR
	rm -rf $F_DIR_REL $F_DIR_REL
}

f_execute_all
