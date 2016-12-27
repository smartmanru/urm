package org.urm.engine.shell;

import org.urm.action.ActionBase;
import org.urm.engine.storage.Folder;
import org.urm.meta.Types.*;
import org.urm.meta.engine.ServerAuthResource;

public class RemoteShellExecutor extends ShellExecutor {
	
	ServerAuthResource auth;
	
	public RemoteShellExecutor( String name , ShellPool pool , Account account , Folder tmpFolder , ServerAuthResource auth ) {
		super( name , pool , account , "" , tmpFolder );
		this.auth = auth;
	}

	@Override
	public boolean start( ActionBase action ) throws Exception {
		ShellProcess process = new ShellProcess( this );
		if( core.sessionType == VarSESSIONTYPE.WINDOWSFROMUNIX )
			return( process.createRemoteWindowsProcessFromLinux( action , auth ) );
			
		if( core.sessionType == VarSESSIONTYPE.UNIXFROMWINDOWS )
			return( process.createRemoteLinuxProcessFromWindows( action , auth ) );
		
		if( core.sessionType == VarSESSIONTYPE.UNIXREMOTE )
			return( process.createRemoteLinuxProcessFromLinux( action , auth ) );

		action.exitUnexpectedState();
		return( false );
	}
	
}
