@echo off

cd %~dp0
cd ..\..

set PRODUCT_HOME=%CD%

if ! "%C_URM_MODE%" == "main" (
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

java -cp %JAVACP% -Duser.language=ru -Durm.mode=%C_URM_MODE% -Durm.os=windows -Durm.server=%C_CONTEXT_URMSERVER% -Dproduct.home=%PRODUCT_HOME% -Dbuild.mode=%C_CONTEXT_VERSIONMODE% -Denv=%C_CONTEXT_ENV% -Ddc=%C_CONTEXT_DC% %C_UMR_CLASS% %*
