#!/bin/bash 

cd `dirname $0`
cd ../..

INSTALL_PATH=`pwd`

if [ "$C_URM_MODE" = "main" ]; then
	C_CONTEXT_URMSERVER=
	C_UMR_CLASS=org.urm.server.Main
else
	C_UMR_CLASS=org.urm.client.Main
fi

if [ "$C_CONTEXT_URMSERVER" = "" ]; then
	JAVACP=master/bin/urms.jar:master/lib/jmxtools-1.2.1.jar:master/lib/jna-4.1.0.jar
else
	JAVACP=master/bin/urmc.jar:master/lib/jmxtools-1.2.1.jar
fi

if [ "$1" = "-trace" ] || [ "$2" = "-trace" ] || [ "$3" = "-trace" ]; then
	echo run: java -cp $JAVACP -Duser.language=ru -Dfile.encoding=utf-8 -Durm.mode=$C_URM_MODE -Durm.os=linux -Durm.installpath=$INSTALL_PATH -Durm.server=$C_CONTEXT_URMSERVER -Durm.product=$C_CONTEXT_PRODUCT -Durm.build=$C_CONTEXT_VERSIONMODE -Durm.env=$C_CONTEXT_ENV -Durm.dc=$C_CONTEXT_DC $C_UMR_CLASS "$@"
fi

java -cp $JAVACP -Duser.language=ru -Dfile.encoding=utf-8 -Durm.mode=$C_URM_MODE -Durm.os=linux -Durm.installpath=$INSTALL_PATH -Durm.server=$C_CONTEXT_URMSERVER -Durm.product=$C_CONTEXT_PRODUCT -Durm.build=$C_CONTEXT_VERSIONMODE -Durm.env=$C_CONTEXT_ENV -Durm.dc=$C_CONTEXT_DC $C_UMR_CLASS "$@"
