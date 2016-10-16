#!/bin/bash 

cd `dirname $0`
cd ../..

C_URM_INSTALLPATH=`pwd`

if [ "$C_URM_MODE" = "main" ]; then
	C_URM_SERVER=
	C_UMR_CLASS=org.urm.server.Main
else
	C_UMR_CLASS=org.urm.client.Main
	if [ "$1" = "-offline" ] || [ "$2" = "-offline" ] || [ "$3" = "-offline" ] || [ "$4" = "-offline" ]; then
		C_URM_SERVER=
		C_URM_MODE=server
	fi
fi

if [ "$1" = "-local" ] || [ "$2" = "-local" ] || [ "$3" = "-local" ] || [ "$4" = "-local" ] || [ "$C_URM_SERVER" = "" ]; then
	JAVACP=master/bin/urms.jar:master/lib/jmxtools-1.2.1.jar:master/lib/jna-4.1.0.jar:master/lib/jmxremote_optional-repackaged-4.0.jar:jsch-0.1.54.jar
else
	JAVACP=master/bin/urmc.jar:master/lib/jmxtools-1.2.1.jar:master/lib/jmxremote_optional-repackaged-4.0.jar
fi

if [ "$1" = "-trace" ] || [ "$2" = "-trace" ] || [ "$3" = "-trace" ] || [ "$4" = "-trace" ]; then
	echo run: java -cp $JAVACP -Duser.language=ru -Dfile.encoding=utf-8 -Durm.mode=$C_URM_MODE -Durm.os=linux -Durm.installpath=$C_URM_INSTALLPATH -Durm.server=$C_URM_SERVER -Durm.product=$C_URM_PRODUCT -Durm.build=$C_URM_VERSIONMODE -Durm.env=$C_URM_ENV -Durm.dc=$C_URM_DC $C_UMR_CLASS "$@"
fi

java -cp $JAVACP -Duser.language=ru -Dfile.encoding=utf-8 -Durm.mode=$C_URM_MODE -Durm.os=linux -Durm.installpath=$C_URM_INSTALLPATH -Durm.server=$C_URM_SERVER -Durm.product=$C_URM_PRODUCT -Durm.build=$C_URM_VERSIONMODE -Durm.env=$C_URM_ENV -Durm.dc=$C_URM_DC $C_UMR_CLASS "$@"
