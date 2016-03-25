@cd %~dp0

@if ".%1." == ".." (
@urm.cmd help "$@"
) else (
@urm.cmd %1 help %2
)
