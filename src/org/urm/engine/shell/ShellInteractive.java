package org.urm.engine.shell;

import org.urm.action.ActionBase;

public class ShellInteractive extends Shell {

	public static ShellInteractive getShell( ActionBase action , String name , ShellPool pool , Account account ) throws Exception {
		ShellInteractive shell = new ShellInteractive( name , pool , account );
		return( shell );
	}

	public ShellInteractive( String name , ShellPool pool , Account account ) {
		super( name , pool , account );
	}
	
	@Override
	public boolean start( ActionBase action ) throws Exception {
		String KEY = action.context.CTX_KEYNAME;
		
		ShellProcess process = new ShellProcess( this ); 
		if( action.isLocalLinux() ) {
			if( action.context.call != null )
				process.runRemoteInteractiveSshLinux( action , KEY );
			else
				process.runLocalInteractiveSshLinux( action , KEY );
		}
		else
		if( action.isLocalWindows() ) {
			if( action.context.call != null )
				process.runRemoteInteractiveSshWindows( action , KEY );
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
		action.trace( name + " wait process to finish ..." );
		waitFor( action );
		pool.removeInteractive( action , this );
	}

	public boolean executeCommand( ActionBase action , String input ) throws Exception {
		return( process.executeCommand( action , input ) );
	}
	
}
