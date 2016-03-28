package ru.egov.urm.shell;

import ru.egov.urm.action.ActionBase;
import ru.egov.urm.storage.Folder;

public class LocalShellExecutor extends ShellExecutor {

	public LocalShellExecutor( String name , ShellExecutorPool pool , String rootPath , Folder tmpFolder ) {
		super( name , pool , pool.account , rootPath , tmpFolder );
	}
	
	public void start( ActionBase action ) throws Exception {
		String cmd = ( account.isWindows() )? "cmd" : "sh";
		ProcessBuilder builder = new ProcessBuilder( cmd );
		super.createProcess( action , builder , rootPath );
	}
}
