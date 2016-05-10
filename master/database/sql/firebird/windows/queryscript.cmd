set P_SQLFILE=%1

%FBPATH%\isql -q -u %URMDB_USER% -p %URMDB_PWD% -e -i %P_SQLFILE% "%URMDB_DBHOST%:%URMDB_DBNAME%"
SET status=%errorlevel%
echo status=%status%

if %status% == 0 exit /B 0
exit /B 1
