@echo off

cd %~dp0
set C_URM_MODE=help

call urm.cmd bin help %*
