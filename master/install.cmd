@echo off

cd %~dp0

if "%1" == "" (
	echo Steps to install URM:
	echo install.cmd path or install.cmd path standalone - to install standalone single-product instance
	echo install.cmd path server - to install multi-product server instance
	echo.
	echo Path must not exist.
	echo Script will create initial URM structure under the path.
	exit 0
)

set P_DSTDIR=%1

set URM_TYPE=
if "%2" == "server" (
	set URM_TYPE=server
)
if "%2" == "standalone" (
	set URM_TYPE=standalone
)

if "%URM_TYPE%" == "" (
	echo install.cmd: unknown install type - "%2". Exiting
	exit 1
)

IF EXIST %P_DSTDIR% (
	echo install.cmd: URM directory %P_DSTDIR% should not exist. Exiting
	exit 1
)

md %P_DSTDIR%\master
IF NOT EXIST %P_DSTDIR%\master (
	echo install.cmd: Unable to create %P_DSTDIR%\master. Exiting
	exit 1
)

robocopy bin %P_DSTDIR%\master\bin /s /e
robocopy database %P_DSTDIR%\master\database /s /e
robocopy lib %P_DSTDIR%\master\lib /s /e

if "%URM_TYPE%" == "server" (
	robocopy samples\server\etc %P_DSTDIR%\etc /s /e
	robocopy samples\server\products %P_DSTDIR%\products /s /e

	echo.
	echo Define products configuration in %P_DSTDIR%\products
)

if "%URM_TYPE%" == "standalone" (
	robocopy samples\standalone\etc %P_DSTDIR%\etc /s /e
	echo.
	echo Define products configuration in %P_DSTDIR%\etc
)

echo After any changes run %P_DSTDIR%\master\bin\configure.cmd to create console helper scripts
echo Optionally add all files to svn and run svnsave.cmd to update instance.
echo.
echo Installation successfully completed.
