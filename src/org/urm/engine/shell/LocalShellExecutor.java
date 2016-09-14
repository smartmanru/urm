package org.urm.engine.shell;

import org.urm.action.ActionBase;
import org.urm.engine.storage.Folder;

public class LocalShellExecutor extends ShellExecutor {

	public LocalShellExecutor( String name , ShellPool pool , String rootPath , Folder tmpFolder ) {
		super( name , pool , pool.masterAccount , rootPath , tmpFolder );
	}
	
	@Override
	public boolean start( ActionBase action ) throws Exception {
		ProcessBuilder builder;
		if( account.isWindows() )
			builder = new ProcessBuilder( "cmd" , "/Q" , "/D" , "/A" , "/V:OFF" );
		else
			builder = new ProcessBuilder( "sh" );
		
		return( super.createProcess( action , builder , rootPath ) );
	}
}
