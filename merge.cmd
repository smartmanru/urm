@echo off

set P_REV=%1
if "%P_REV%" == "" (
	echo revision parameter is required
	exit 1
)

svn merge -r%P_REV%:HEAD http://usvn.ahuman.org/svn/urm/trunk .
