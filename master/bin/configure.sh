#!/bin/bash

cd `dirname $0`

X_ENVLIST="$*"

S_LISTING_FILE=master.files.info

function f_execute_add_wrappers() {
	local P_PREFIX=$1
	local P_ENVFILE=$2
	local P_ENVID=$3
	local P_TYPE=$4

	local L_LISTING_FILE=$C_CONFIG_PRODUCT_DEPLOYMENT_HOME/master/$S_LISTING_FILE
	local L_WRAPPERS_DIR=$C_CONFIG_PRODUCT_DEPLOYMENT_HOME/master/wrappers/$P_TYPE

	# create top wrappers
	local L_FNAME
	local F_DIR
	cat $L_LISTING_FILE | grep "core:wrappers/$P_TYPE/" | sed "s/core:wrappers\/$P_TYPE\///" | while read line; do
		L_FNAME=$line
		if [ ! -f "$L_WRAPPERS_DIR/$L_FNAME" ]; then
			echo "configure.sh: unexpected - missing file $L_WRAPPERS_DIR/$L_FNAME. Exiting"
			exit 1
		fi

		F_DIR=`dirname $L_FNAME`
		mkdir -p $F_DIR

		cp $L_WRAPPERS_DIR/$L_FNAME $F_DIR
		echo "$P_PREFIX/$L_FNAME" >> $L_LISTING_FILE
	done
}

function f_execute_dc() {
	local P_PREFIX=$1
	local P_ENVFILE=$2
	local P_ENVID=$3
	local P_DC=$4
	local P_TYPE=$5

	local L_LISTING_FILE=$C_CONFIG_PRODUCT_DEPLOYMENT_HOME/master/$S_LISTING_FILE

	# make context file
	(
		echo "#!/bin/bash"
		echo ""
		echo "export C_CONTEXT_ENV=$P_ENVFILE"
		echo "export C_CONTEXT_DC=$P_DC"
	) > _context.sh

	echo "$P_PREFIX/_context.sh" >> $L_LISTING_FILE

	f_execute_add_wrappers $P_PREFIX $P_ENVFILE $P_ENVID $P_TYPE
}

S_ENV_ID=
S_ENV_DCLIST=
function f_execute_getenvinfo() {
	local P_ENVFILE=$1

	local F_ENV_PATH=$C_CONFIG_PRODUCT_DEPLOYMENT_HOME/etc/env/$P_ENVFILE
	S_ENV_ID=`xmlstarlet sel -t -m "env" -v "@id" $F_ENV_PATH`
	S_ENV_DCLIST=`xmlstarlet sel -t -m "env/datacenter" -v "@name" -o " " $F_ENV_PATH`
}

function f_execute_env() {
	local P_ENVFILE=$1

	# setup environment
	local F_CURDIR=`pwd`

	export C_CONFIG_PRODUCT_DEPLOYMENT_HOME=`dirname $F_CURDIR`
	cd deployment

	echo "configure environment $P_ENVFILE ..."
	f_execute_getenvinfo $P_ENVFILE

	# filter out given environment information
	local L_LISTING_FILE=$C_CONFIG_PRODUCT_DEPLOYMENT_HOME/master/$S_LISTING_FILE
	cat $L_LISTING_FILE | grep -v "^env:deployment/$S_ENV_ID/" > tmpfile
	mv tmpfile $L_LISTING_FILE

	# generate environment proxy files
	local F_DCN=`echo $S_ENV_DCLIST | wc -w`

	if [ "$F_DCN" = "0" ]; then
		echo "environment has no datacenters defined. Skipped."
		return 1
	fi

	# create env files
	mkdir -p $S_ENV_ID
	cd $S_ENV_ID

	local F_STAT
	if [ "$F_DCN" = "1" ]; then
		f_execute_dc "env:deployment/$S_ENV_ID" $P_ENVFILE $S_ENV_ID $S_ENV_DCLIST "singledc"
		F_STAT=$?
		if [ "$F_STAT" != "0" ]; then
			echo "configure.sh: f_execute_dc failed. Exiting"
			exit 1
		fi
	else
		# make context file
		(
			echo "#!/bin/bash"
			echo ""
			echo "export C_CONTEXT_ENV=$P_ENVFILE"
			echo "export C_CONTEXT_DC="
		) > _context.sh

		echo "env:deployment/$S_ENV_ID/_context.sh" >> $L_LISTING_FILE

		f_execute_add_wrappers "env:deployment/$S_ENV_ID" $P_ENVFILE $S_ENV_ID "multidc-top"
		F_STAT=$?
		if [ "$F_STAT" != "0" ]; then
			echo "configure.sh: f_execute_multi_top failed. Exiting"
			exit 1
		fi

		for dc in $S_ENV_DCLIST; do
			if [[ ! "$dc" =~ ^dc\. ]]; then
				echo "configure.sh: invalid dc=$dc, should be dc.xxx. Exiting"
				exit 1
			fi

			mkdir -p $dc
			cd $dc

			f_execute_dc "env:deployment/$S_ENV_ID/$dc" $P_ENVFILE $S_ENV_ID $dc "multidc"
			F_STAT=$?
			if [ "$F_STAT" != "0" ]; then
				echo "configure.sh: f_execute_dc failed. Exiting"
				exit 1
			fi
			cd ..
		done
	fi
}       	

function f_execute_all() {
	# go master
	cd ..

	# check exists
	local F_CONFUGURE_LIST
	if [ "$X_ENVLIST" != "" ]; then
		F_CONFUGURE_LIST="$X_ENVLIST"
	else
		# by default - configure all in etc/env
		F_CONFUGURE_LIST=`find ../etc/env -type f -name "*.xml" | sed "s/^\.\.\/etc\/env\///" | tr "\n" " "`

		# filter out all environment info from listing file
		cat $S_LISTING_FILE | grep -v "^env:" > tmpfile
		mv tmpfile $S_LISTING_FILE
	fi

	# do configuration
	for env in $F_CONFUGURE_LIST; do
		if [ ! -f "../etc/env/$env" ]; then
			echo "configure.sh: unable to find environment definition file ../etc/env/$env. Exiting"
			exit 1
		fi

		( f_execute_env $env )
	done
}

f_execute_all

echo "configure.sh: successully finished."
