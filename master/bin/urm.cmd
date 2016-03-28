@cd %~dp0
@cd ..\..

@set PRODUCT_HOME=%CD%
@java -cp master/bin/urm.jar -Duser.language=ru -Durm.os=windows -Dproduct.home=%PRODUCT_HOME% -Dbuild.mode=%C_CONTEXT_VERSIONMODE% -Denv=%C_CONTEXT_ENV% -Ddc=%C_CONTEXT_DC% ru.egov.urm.Engine %*
