package org.urm.server.shell;

import org.urm.common.jmx.ServerCommandCall;
import org.urm.server.action.ActionBase;

public class ShellInteractive extends Shell {

	public final static String CONNECT_MARKER = "URM.CONNECTED";  
	public final static String COMMAND_MARKER = "URM.COMMAND";  

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
		
		if( action.isLocalLinux() ) {
			if( action.context.call != null )
				runRemoteInteractiveSshLinux( action , account , KEY );
			else
				runLocalInteractiveSshLinux( action , account , KEY );
		}
		else
		if( action.isLocalWindows() ) {
			if( action.context.call != null )
				runRemoteInteractiveSshWindows( action , account , KEY );
			else
				runLocalInteractiveSshWindows( action , account , KEY );
		}
		else
			action.exitUnexpectedState();
		
		tsLastInput = tsLastOutput = System.currentTimeMillis();
		return( true );
	}
	
	@Override
	public void kill( ActionBase action ) throws Exception {
		killShellProcess( action );
		killOSProcess( action );
	}
	
	public void stop( ActionBase action ) throws Exception {
		kill( action );
	}

	private void runLocalInteractiveSshLinux( ActionBase action , Account account , String KEY ) throws Exception {
		String cmd = "ssh " + account.getSshAddr();
		if( !KEY.isEmpty() )
			cmd += " -i " + KEY;
		cmd += " < /dev/tty > /dev/tty 2>&1";
		
		action.trace( account.getPrintName() + " execute: " + cmd );

		ProcessBuilder pb = new ProcessBuilder( "sh" , "-c" , cmd );
		startProcess( action , pb , null , false );
	}
	
	private void runLocalInteractiveSshWindows( ActionBase action , Account account , String KEY ) throws Exception {
		String cmd = "plink -ssh ";
		if( !KEY.isEmpty() )
			cmd += "-i " + KEY + " ";
		if( account.PORT != 22 )
			cmd += "-P " + account.PORT + " ";
		cmd += account.USER + "@" + account.HOST;
		
		action.trace( account.getPrintName() + " execute: " + cmd );
		
		ProcessBuilder pb = new ProcessBuilder( "cmd" , "/C" , cmd );
		startProcess( action , pb , null , false );
	}
	
	private void runRemoteInteractiveSshLinux( ActionBase action , Account account , String KEY ) throws Exception {
		String cmd = "ssh -T " + account.getSshAddr();
		if( !KEY.isEmpty() )
			cmd += " -i " + KEY;
		
		action.trace( account.getPrintName() + " execute: " + cmd );
		
		ServerCommandCall call = action.context.call;
		ProcessBuilder pb = new ProcessBuilder( "sh" , "-c" , cmd );
		
		executeInteractive( action , call , pb );
	}

	private void runRemoteInteractiveSshWindows( ActionBase action , Account account , String KEY ) throws Exception {
		String cmd = "plink ";
		if( !KEY.isEmpty() )
			cmd += "-i " + KEY + " ";
		if( account.PORT != 22 )
			cmd += "-P " + account.PORT + " ";
		cmd += account.USER + "@" + account.HOST;
		
		action.trace( account.getPrintName() + " execute: " + cmd );
		
		ServerCommandCall call = action.context.call;
		ProcessBuilder pb = new ProcessBuilder( "cmd" , "/C" , cmd );
		
		executeInteractive( action , call , pb );
	}
	
	private void executeInteractive( ActionBase action , ServerCommandCall call , ProcessBuilder pb ) throws Exception {
		pb.redirectErrorStream( true );
		startProcess( action , pb , null , true );
		
		int timeout = action.setTimeoutDefault();
		addInput( action , "echo " + CONNECT_MARKER , true );
		if( !waitForMarker( action , CONNECT_MARKER ) ) {
			call.connectFinished( false );
			action.exit( "unable to connect to " + name );
		}
		
		action.setTimeout( timeout );
		call.connectFinished( true );
	}

	public void runInteractive( ActionBase action ) throws Exception {
		start( action );
		action.trace( name + " wait process to finish ..." );
		waitFor( action );
		pool.removeInteractive( action , this );
	}

	public boolean executeCommand( ActionBase action , String input ) throws Exception {
		addInput( action , input , false );
		addInput( action , "echo " + COMMAND_MARKER , true );
		
		// wait for finish
		return( waitForMarker( action , CONNECT_MARKER ) );
	}
	
}
