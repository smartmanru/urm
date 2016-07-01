#!/bin/bash

cd `dirname $0`

if [ "$1" = "" ]; then
	echo Steps to install URM:
	echo install.sh server path - to install multi-product server instance
	echo install.sh standalone path - to install standalone single-product instance
	echo.
	echo Path must not exist.
	echo Script will create initial URM structure under the path.
	exit 0
fi

URM_TYPE=
if [ "$1" = "server" ]; then
	URM_TYPE=server
fi
if [ "$1" = "standalone" ]; then
	URM_TYPE=standalone
fi

if [ "$URM_TYPE" == "" ]; then
	echo install.sh: unknown install type - "$1". Exiting
	exit 1
fi

P_DSTDIR=$2
if [ "$P_DSTDIR" = "" ]; then
	echo install.sh: DSTDIR is empty. Exiting
	exit 1
fi

if [ -d $P_DSTDIR ]; then
	echo install.sh: URM directory $P_DSTDIR should not exist. Exiting
	exit 1
fi

mkdir $P_DSTDIR/master
IF [ ! -d $P_DSTDIR/master ]; then
	echo install.sh: Unable to create $P_DSTDIR/master. Exiting
	exit 1
)

cp -R bin $P_DSTDIR/master /s /e
cp -R database $P_DSTDIR/master /s /e
cp -R lib $P_DSTDIR/master /s /e

if [ "$URM_TYPE" = "server" ]; then
	cp -R samples/server/etc $P_DSTDIR /s /e
	cp -R samples/server/products $P_DSTDIR /s /e

	echo.
	echo Define products configuration in $P_DSTDIR/products
fi

if [ "$URM_TYPE" = "standalone" ]; then
	cp -R samples/standalone/etc $P_DSTDIR/etc /s /e
	echo.
	echo Define products configuration in $P_DSTDIR/etc
fi

echo After any changes run %P_DSTDIR%\master\bin\configure.cmd to create console helper scripts
echo Optionally add all files to svn and run svnsave.cmd to update instance.
echo.
echo Installation successfully completed.
