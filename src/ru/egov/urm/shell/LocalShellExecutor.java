package ru.egov.urm.shell;

import ru.egov.urm.run.ActionBase;
import ru.egov.urm.storage.Folder;

public class LocalShellExecutor extends ShellExecutor {

	public LocalShellExecutor( String name , ShellExecutorPool pool , String rootPath , Folder tmpFolder ) {
		super( name , pool , pool.account , rootPath , tmpFolder );
	}
	
	public void start( ActionBase action ) throws Exception {
		ProcessBuilder builder = new ProcessBuilder( "sh" );
		super.createProcess( action , builder , rootPath );
	}
}
