@echo off

cd %~dp0
set URM_MODE=main

call urm.cmd bin configure-windows %*
