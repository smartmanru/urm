#!/bin/bash 

cd `dirname $0`
cd ../..

PRODUCT_HOME=`pwd`

if [ "$C_URM_MODE" == "main" ]; then
	C_CONTEXT_URMSERVER=
	C_UMR_CLASS=org.urm.client.Main
else
	C_UMR_CLASS=org.urm.client.Client
fi

if [ "$C_CONTEXT_URMSERVER" = "" ]; then
	JAVACP=master/bin/urms.jar:master/lib/jna-4.1.0.jar
else
	JAVACP=master/bin/urmc.jar
fi

java -cp $JAVACP -Duser.language=ru -Dfile.encoding=utf-8 -Durm.mode=$C_URM_MODE -Durm.os=linux -Durm.server=%C_CONTEXT_URMSERVER% -Dproduct.home=$PRODUCT_HOME -Dbuild.mode=$C_CONTEXT_VERSIONMODE -Denv=$C_CONTEXT_ENV -Ddc=$C_CONTEXT_DC $C_UMR_CLASS "$@"
