#!/bin/bash 

cd `dirname $0`
cd ../..

PRODUCT_HOME=`pwd`

if [ "$C_URM_MODE" = "main" ]; then
	C_CONTEXT_URMSERVER=
	C_UMR_CLASS=org.urm.server.Main
else
	C_UMR_CLASS=org.urm.client.Main
fi

if [ "$C_CONTEXT_URMSERVER" = "" ]; then
	JAVACP=master/bin/urms.jar:master/lib/jna-4.1.0.jar
else
	JAVACP=master/bin/urmc.jar
fi

java -cp $JAVACP -Duser.language=ru -Dfile.encoding=utf-8 -Durm.mode=$C_URM_MODE -Durm.os=linux -Durm.server=%C_CONTEXT_URMSERVER% -Durm.product=$C_CONTEXT_PRODUCT -Durm.producthome=$PRODUCT_HOME -Durm.build=$C_CONTEXT_VERSIONMODE -Durm.env=$C_CONTEXT_ENV -Durm.dc=$C_CONTEXT_DC $C_UMR_CLASS "$@"
