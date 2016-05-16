#!/bin/bash

echo "show database;" | %FBPATH%/isql -q -u %URMDB_USER% -p %URMDB_PWD% "%URMDB_DBHOST%:%URMDB_DBNAME%"
F_STATUS=?
echo status=$F_STATUS

if [ "$F_STATUS" == "0" ]; then
	 exit 0
fi

exit 1
