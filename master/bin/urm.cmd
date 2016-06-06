@echo off

cd %~dp0
cd ..\..

set PRODUCT_HOME=%CD%

if "%C_CONTEXT_URMSERVER%" == "" (
	set JAVACP=master/bin/urms.jar;master/lib/jna-4.1.0.jar
)
else (
	set JAVACP=master/bin/urmc.jar
)

java -cp %JAVACP% -Duser.language=ru -Durm.os=windows -Dproduct.home=%PRODUCT_HOME% -Dbuild.mode=%C_CONTEXT_VERSIONMODE% -Denv=%C_CONTEXT_ENV% -Ddc=%C_CONTEXT_DC% org.urm.client.Engine %*
