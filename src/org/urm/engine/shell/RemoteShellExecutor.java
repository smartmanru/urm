package org.urm.engine.shell;

import org.urm.action.ActionBase;
import org.urm.engine.ShellService;
import org.urm.engine.storage.Folder;
import org.urm.meta.engine.AuthResource;

public class RemoteShellExecutor extends ShellExecutor {
	
	AuthResource auth;
	
	public RemoteShellExecutor( int id , String name , ShellService pool , Account account , Folder tmpFolder , AuthResource auth , boolean dedicated ) {
		super( id , name , pool , account , "" , tmpFolder , dedicated );
		this.auth = auth;
	}

	@Override
	public boolean start( ActionBase action ) throws Exception {
		ShellProcess process = new ShellProcess( this );
		if( super.isWindowsFromUnix() )
			return( process.createRemoteWindowsProcessFromLinux( action , auth ) );
			
		if( super.isUnixFromWindows() )
			return( process.createRemoteLinuxProcessFromWindows( action , auth ) );
		
		if( super.isUnixRemote() )
			return( process.createRemoteLinuxProcessFromLinux( action , auth ) );

		action.exitUnexpectedState();
		return( false );
	}
	
}
