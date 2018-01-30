@echo off
rem params:
rem URMDB_USER
rem URMDB_PWD
rem URMDB_DBHOST
rem URMDB_DBPORT
rem URMDB_DBNAME

set P_SQLFILE=%1
set P_LOGFILE=%2

if NOT "%XPORT%" == "" (
	set "XPORT= -p %XPORT%"
)

psql -A -a -t -d %URMDB_DBNAME% -h %URMDB_DBHOST% %XPORT% -U %URMDB_USER% -f %P_SQLFILE% > %P_LOGFILE% 2>&1
