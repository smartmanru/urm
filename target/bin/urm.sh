#!/bin/bash 

cd `dirname $0`
cd ..

C_URM_INSTALLPATH=`pwd`
C_UMR_CLASS=org.urm.client.Main
JAVACP=bin/urmc.jar:bin/jmxtools-1.2.1.jar:bin/jmxremote_optional-repackaged-4.0.jar

java -cp $JAVACP -Duser.language=ru -Dfile.encoding=utf-8 -Durm.mode=$C_URM_MODE -Durm.os=linux -Durm.installpath=$C_URM_INSTALLPATH -Durm.server=$C_URM_SERVER -Durm.product=$C_URM_PRODUCT -Durm.build=$C_URM_VERSIONMODE -Durm.env=$C_URM_ENV -Durm.sg=$C_URM_SG $C_UMR_CLASS "$@"
