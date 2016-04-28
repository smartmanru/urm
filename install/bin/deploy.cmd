@echo off

cd /d %~dp0

set RELEASE=%1
set ADDOPT=%2

if "%RELEASE%" == "" (
	echo RELEASE not set. Exiting
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

cd %ROOTPATH%\install\master\bin
call urm.cmd deploy redist %STDOPTS% default paymain

cd %ROOTPATH%\install\master\bin
call urm.cmd deploy getredistinfo %STDOPTS% -release default paymain

cd %ROOTPATH%\install\master\bin
call urm.cmd deploy rollout %STDOPTS% default paymain
