@echo off

cd %~dp0
set C_URM_MODE=main

call urm.cmd bin help %*
