@echo off

cd %~dp0
cd ..\..

set PRODUCT_HOME=%CD%

if "%C_URM_MODE%" == "main" (
	set C_CONTEXT_URMSERVER=
	set C_UMR_CLASS=org.urm.server.Main
) else (
	set C_UMR_CLASS=org.urm.client.Main
)

if "%C_CONTEXT_URMSERVER%" == "" (
	set JAVACP=master/bin/urms.jar;master/lib/jna-4.1.0.jar
) else (
	set JAVACP=master/bin/urmc.jar
)

java -cp %JAVACP% -Duser.language=ru -Durm.mode=%C_URM_MODE% -Durm.os=windows -Durm.server=%C_CONTEXT_URMSERVER% -Durm.product=%C_CONTEXT_PRODUCT% -Durm.producthome=%PRODUCT_HOME% -Durm.build=%C_CONTEXT_VERSIONMODE% -Durm.env=%C_CONTEXT_ENV% -Durm.dc=%C_CONTEXT_DC% %C_UMR_CLASS% %*
