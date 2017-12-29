@echo off
rem params:
rem URMDB_USER
rem URMDB_PWD
rem URMDB_DBHOST
rem URMDB_DBNAME

set P_SQLFILE=%1
set P_LOGFILE=%2

setlocal enabledelayedexpansion
set "find=*:"
call set XPORT=%%URMDB_DBHOST:!find!=%%

if "%XPORT%" == "%URMDB_DBHOST%" (
	set "XPORT="
) else (
	set "find=:%XPORT%"
	call set XHOST=%%URMDB_DBHOST:!find!=%%
	set "XPORT= -p %XPORT%"
)

psql -A -a -t -d %URMDB_DBNAME% -h %XHOST% %XPORT% -U %URMDB_USER% -f %P_SQLFILE% > %P_LOGFILE% 2>&1
