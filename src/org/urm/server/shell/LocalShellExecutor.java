package org.urm.server.shell;

import org.urm.server.action.ActionBase;
import org.urm.server.storage.Folder;

public class LocalShellExecutor extends ShellExecutor {

	public LocalShellExecutor( String name , ShellPool pool , String rootPath , Folder tmpFolder ) {
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
