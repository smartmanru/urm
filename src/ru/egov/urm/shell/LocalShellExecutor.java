package ru.egov.urm.shell;

import ru.egov.urm.run.ActionBase;

public class LocalShellExecutor extends ShellExecutor {

	public LocalShellExecutor( String name , ShellExecutorPool pool , String rootPath ) {
		super( name , pool , rootPath );
	}
	
	public void start( ActionBase action ) throws Exception {
		ProcessBuilder builder = new ProcessBuilder( "sh" );
		super.createProcess( action , builder , rootPath );
	}
}
