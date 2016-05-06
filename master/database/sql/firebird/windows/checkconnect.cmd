echo show database; | %FBPATH%\isql -u %URMDB_USER% -p %URMDB_PWD% "%URMDB_DBHOST%:%URMDB_DBNAME%"
if %errorlevel% 1 exit /B 0
exit /B 1
