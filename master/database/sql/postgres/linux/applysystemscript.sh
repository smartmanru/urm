#!/bin/bash
# params:
# URMDB_USER
# URMDB_PWD
# URMDB_DBHOST
# URMDB_DBNAME

P_SQLFILE=$1
P_LOGFILE=$2

psql -a -e < $P_SQLFILE > $P_LOGFILE 2>&1
