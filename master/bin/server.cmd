@echo off

cd %~dp0
set C_URM_MODE=main

urm.cmd bin server %*
