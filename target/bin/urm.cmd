@echo off

cd %~dp0
cd ..
set C_URM_INSTALLPATH=%CD%

set C_UMR_CLASS=org.urm.client.Main
set JAVACP=bin\urmc.jar;bin\jmxtools-1.2.1.jar;bin\jmxremote_optional-repackaged-4.0.jar

chcp 65001 > NUL
java -cp %JAVACP% -Duser.language=ru -Durm.mode=%C_URM_MODE% -Durm.os=windows -Durm.installpath=%C_URM_INSTALLPATH% -Durm.server=%C_URM_SERVER% -Durm.product=%C_URM_PRODUCT% -Durm.build=%C_URM_VERSIONMODE% -Durm.env=%C_URM_ENV% -Durm.sg=%C_URM_SG% %C_UMR_CLASS% %*
