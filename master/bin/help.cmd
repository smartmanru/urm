@echo off

cd %~dp0
set URM_MODE=main

if ".%1." == ".." (
	call urm.cmd help "%*"
) else (
	call urm.cmd %1 help %2
)
