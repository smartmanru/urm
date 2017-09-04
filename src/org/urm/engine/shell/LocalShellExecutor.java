package org.urm.engine.shell;

import org.urm.action.ActionBase;
import org.urm.engine.storage.Folder;

public class LocalShellExecutor extends ShellExecutor {

	public LocalShellExecutor( int id , String name , EngineShellPool pool , String rootPath , Folder tmpFolder , boolean dedicated ) {
		super( id , name , pool , pool.masterAccount , rootPath , tmpFolder , dedicated );
	}
	
	@Override
	public boolean start( ActionBase action ) throws Exception {
		ShellProcess process = new ShellProcess( this );
		if( account.isWindows() )
			return( process.createLocalWindowsProcess( action ) );
		
		return( process.createLocalLinuxProcess( action ) );
	}
}
