package org.urm.engine.shell;

import org.urm.action.ActionBase;
import org.urm.engine.storage.Folder;
import org.urm.meta.product.Meta.VarSESSIONTYPE;

public class RemoteShellExecutor extends ShellExecutor {
	
	public RemoteShellExecutor( String name , ShellPool pool , Account account , Folder tmpFolder ) {
		super( name , pool , account , "" , tmpFolder );
	}

	@Override
	public boolean start( ActionBase action ) throws Exception {
		ShellProcess process = new ShellProcess( this );
		if( core.sessionType == VarSESSIONTYPE.WINDOWSFROMUNIX )
			return( process.createRemoteWindowsProcessFromLinux( action ) );
			
		if( core.sessionType == VarSESSIONTYPE.UNIXFROMWINDOWS )
			return( process.createRemoteLinuxProcessFromWindows( action ) );
		
		if( core.sessionType == VarSESSIONTYPE.UNIXREMOTE )
			return( process.createRemoteLinuxProcessFromLinux( action ) );

		action.exitUnexpectedState();
		return( false );
	}
	
}
