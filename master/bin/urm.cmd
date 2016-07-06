@echo off

cd %~dp0
cd ..\..

set C_URM_INSTALLPATH=%CD%

if "%C_URM_MODE%" == "main" (
	set C_URM_SERVER=
	set C_UMR_CLASS=org.urm.server.Main
) else (
	set C_UMR_CLASS=org.urm.client.Main
)

set x=0
if "%1" == "-offline" set x=1
if "%2" == "-offline" set x=1
if "%3" == "-offline" set x=1
if "%4" == "-offline" set x=1
if "%x%" == "1" (
	set C_URM_SERVER=
)

set x=0
if "%1" == "-local" set x=1
if "%2" == "-local" set x=1
if "%3" == "-local" set x=1
if "%4" == "-local" set x=1
if "%C_URM_SERVER%" == "" set x=1
if "%x%" == "1" (
	set JAVACP=master/bin/urms.jar;master/lib/jmxtools-1.2.1.jar;master/lib/jna-4.1.0.jar;master/lib/jmxremote_optional-repackaged-4.0.jar
) else (
	set JAVACP=master/bin/urmc.jar;master/lib/jmxtools-1.2.1.jar;master/lib/jmxremote_optional-repackaged-4.0.jar
)

set x=0
if "%1" == "-trace" set x=1
if "%2" == "-trace" set x=1
if "%3" == "-trace" set x=1
if "%4" == "-trace" set x=1
if "%x%" == "1" (
	echo run: java -cp %JAVACP% -Duser.language=ru -Durm.mode=%C_URM_MODE% -Durm.os=windows -Durm.installpath=%C_URM_INSTALLPATH% -Durm.server=%C_URM_SERVER% -Durm.product=%C_URM_PRODUCT% -Durm.build=%C_URM_VERSIONMODE% -Durm.env=%C_URM_ENV% -Durm.dc=%C_URM_DC% %C_UMR_CLASS% %*
)

java -cp %JAVACP% -Duser.language=ru -Durm.mode=%C_URM_MODE% -Durm.os=windows -Durm.installpath=%C_URM_INSTALLPATH% -Durm.server=%C_URM_SERVER% -Durm.product=%C_URM_PRODUCT% -Durm.build=%C_URM_VERSIONMODE% -Durm.env=%C_URM_ENV% -Durm.dc=%C_URM_DC% %C_UMR_CLASS% %*
