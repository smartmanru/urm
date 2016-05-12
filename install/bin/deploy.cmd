@echo off

cd /d %~dp0

set RELEASE=%1
set CMD=%2
set ADDOPT=%3

if "%RELEASE%" == "" (
	echo RELEASE not set. Exiting
	exit 1
)
if "%CMD%" == "" (
	echo CMD not set. Exiting
	exit 1
)
echo deploy %RELEASE% ...

set PATH=%cd%;%PATH%
cd ..\..
set ROOTPATH=%cd%
call _context.cmd

set RELEASEPATH=%ROOTPATH%\releases\%RELEASE%
set PATH=%ROOTPATH%\install\jre7\bin;%PATH%

set STDOPTS=%ADDOPT% -local -etcpath %RELEASEPATH%\urm.conf -distpath %RELEASEPATH%\urm.distr -hiddenpath %ROOTPATH%\secured\%C_CONTEXT_SECUREDPATH%

set F_DBAPPLY=yes
set F_REDIST=yes
set F_DEPLOY=yes
if "%CMD%" != "all" if "%CMD%" != "dbapply" set F_DBAPPLY=no
if "%CMD%" != "all" if "%CMD%" != "redist" set F_REDIST=no
if "%CMD%" != "all" if "%CMD%" != "deployredist" set F_DEPLOY=no

if "%F_DBAPPLY%" == "yes" (
cd %ROOTPATH%\install\master\bin
call urm.cmd database dbapply %STDOPTS% -db maindb default all
if %errorlevel% != 0 (
	echo unable to apply database changes
	exit /B 1
)
)

if "%F_REDIST%" == "yes" (
cd %ROOTPATH%\install\master\bin
call urm.cmd deploy redist %STDOPTS% default paymain
if %errorlevel% != 0 (
	echo unable to redist release
	exit /B 1
)

cd %ROOTPATH%\install\master\bin
call urm.cmd deploy getredistinfo %STDOPTS% -release default paymain
if %errorlevel% != 0 (
	echo unable to get dist info
	exit /B 1
)
)

if "%F_DEPLOY%" == "yes" (
cd %ROOTPATH%\install\master\bin
call urm.cmd deploy deployredist %STDOPTS% default paymain
if %errorlevel% != 0 (
	echo unable to deploy release
	exit /B 1
)
)

exit /B 0
