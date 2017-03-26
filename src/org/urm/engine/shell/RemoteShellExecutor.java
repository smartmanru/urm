package org.urm.engine.shell;

import org.urm.action.ActionBase;
import org.urm.engine.storage.Folder;
import org.urm.meta.engine.ServerAuthResource;

public class RemoteShellExecutor extends ShellExecutor {
	
	ServerAuthResource auth;
	
	public RemoteShellExecutor( int id , String name , ShellPool pool , Account account , Folder tmpFolder , ServerAuthResource auth , boolean dedicated ) {
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
