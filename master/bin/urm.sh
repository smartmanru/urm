#!/bin/bash 
cd `dirname $0`
cd ../..

PRODUCT_HOME=`pwd`
java -cp master/bin/urm.jar:master/lib/jna-4.1.0.jar -Duser.language=ru -Dfile.encoding=utf-8 -Durm.os=linux -Dproduct.home=$PRODUCT_HOME -Dbuild.mode=$C_CONTEXT_VERSIONMODE -Denv=$C_CONTEXT_ENV -Ddc=$C_CONTEXT_DC org.urm.client.Engine "$@"
