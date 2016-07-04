@echo off

cd %~dp0
set C_URM_MODE=main

set URM_TRACE=
if "%1" == "-trace" (
	set URM_TRACE=-trace
	set P_DSTDIR=%2
) else (
	set P_DSTDIR=%1
)

if "%P_DSTDIR" == "" (
	echo P_DSTDIR is empty. Exiting
	exit 1
)

IF NOT EXIST %P_DSTDIR%\master (
	echo upgrade.cmd: invalid product URM directory %P_DSTDIR% . Exiting"
	exit 1
)

robocopy master.*.info %P_DSTDIR%\master\ /s /e
robocopy bin %P_DSTDIR%\master\bin /s /e
robocopy database %P_DSTDIR%\master\database /s /e
robocopy lib %P_DSTDIR%\master\lib /s /e

cd %P_DSTDIR%\master\bin
configure.cmd %URM_TRACE% default
