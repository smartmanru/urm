@cd %~dp0

set P_DSTDIR=%1

if "%P_DSTDIR" == "" (
	echo P_DSTDIR is empty. Exiting
	exit 1
)

IF NOT EXIST %P_DSTDIR%\master (
	echo upgrade.cmd: invalid product URM directory %P_DSTDIR% . Exiting"
	exit 1
)

cd ..\..
xcopy master %P_DSTDIR%\master /s /e

cd %P_DSTDIR%\master\bin
configure.cmd default
