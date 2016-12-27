package org.urm.engine.shell;

import org.urm.action.ActionBase;
import org.urm.meta.engine.ServerAuthResource;

public class ShellInteractive extends Shell {

	ServerAuthResource auth;
	
	public static ShellInteractive getShell( ActionBase action , String name , ShellPool pool , Account account , ServerAuthResource auth ) throws Exception {
		ShellInteractive shell = new ShellInteractive( name , pool , account , auth );
		return( shell );
	}

	private ShellInteractive( String name , ShellPool pool , Account account , ServerAuthResource auth ) {
		super( name , pool , account );
		this.auth = auth;
	}
	
	@Override
	public boolean start( ActionBase action ) throws Exception {
		String KEY = action.context.CTX_KEYNAME;
		
		ShellProcess process = new ShellProcess( this ); 
		if( action.isLocalLinux() ) {
			if( action.context.call != null )
				process.runRemoteInteractiveSshLinux( action , KEY , auth );
			else
				process.runLocalInteractiveSshLinux( action , KEY );
		}
		else
		if( action.isLocalWindows() ) {
			if( action.context.call != null )
				process.runRemoteInteractiveSshWindows( action , KEY , auth );
			else
				process.runLocalInteractiveSshWindows( action , KEY );
		}
		else
			action.exitUnexpectedState();
		
		tsLastInput = tsLastOutput = System.currentTimeMillis();
		return( true );
	}
	
	@Override
	public void kill( ActionBase action ) throws Exception {
		super.killProcess( action );
	}
	
	public void stop( ActionBase action ) throws Exception {
		kill( action );
	}

	public void runInteractive( ActionBase action ) throws Exception {
		start( action );
		pool.engine.serverAction.trace( name + " wait interactive process to finish ..." );
		int status = waitForInteractive( action );
		action.trace( name + " interactive process has been finished with status=" + status );
		pool.removeInteractive( action , this );
	}

	public boolean executeInteractiveCommand( ActionBase action , String input ) throws Exception {
		return( process.executeInteractiveCommand( action , input ) );
	}
	
}
