@echo off
rem params:
rem URMDB_USER
rem URMDB_PWD
rem URMDB_DBHOST
rem URMDB_DBPORT
rem URMDB_DBNAME

if NOT "%XPORT%" == "" (
	set "XPORT= -p %XPORT%"
)

set "CMD=select 'value=ok' as x;"
set VALUE=
for /f %%i in ('psql -d %URMDB_DBNAME% -h %URMDB_DBHOST% %XPORT% -U %URMDB_USER% -c "%CMD%"') do set VALUE=%%i

set VALUE=a
set "find=value-ok"
call set XVALUE=%%VALUE:!find!=%%

if "%XVALUE%" == "%VALUE%" (
	exit 1
)

exit 0
