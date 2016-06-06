package org.urm.shell;

import org.urm.action.ActionBase;
import org.urm.storage.Folder;

public class LocalShellExecutor extends ShellExecutor {

	public LocalShellExecutor( String name , ShellExecutorPool pool , String rootPath , Folder tmpFolder ) {
		super( name , pool , pool.account , rootPath , tmpFolder );
	}
	
	public void start( ActionBase action ) throws Exception {
		ProcessBuilder builder;
		if( account.isWindows() )
			builder = new ProcessBuilder( "cmd" , "/Q" , "/D" , "/A" , "/V:OFF" );
		else
			builder = new ProcessBuilder( "sh" );
		super.createProcess( action , builder , rootPath );
	}
}
