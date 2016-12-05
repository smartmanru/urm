@echo off

cd %~dp0
set C_URM_MODE=client

call urm.cmd bin auth %*
