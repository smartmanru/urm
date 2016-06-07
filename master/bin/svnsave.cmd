@echo off

cd %~dp0
set URM_MODE=main

urm.cmd bin svnsave %*
