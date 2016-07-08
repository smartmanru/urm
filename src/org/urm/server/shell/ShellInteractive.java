package org.urm.server.shell;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

import org.urm.common.jmx.ServerCommandCall;
import org.urm.server.action.ActionBase;

public class ShellInteractive extends Shell {

	Process process = null;
	OutputStream stdin;
	InputStream stderr;
	InputStream stdout;
	BufferedReader reader;
	Writer writer;
	BufferedReader errreader;

	public final static String CONNECT_MARKER = "URM.CONNECTED";  

	public static ShellInteractive getShell( ActionBase action , String name , ShellPool pool , Account account ) throws Exception {
		ShellInteractive shell = new ShellInteractive( name , pool , account );
		return( shell );
	}

	public ShellInteractive( String name , ShellPool pool , Account account ) {
		super( name , pool , account );
	}
	
	@Override
	public void start( ActionBase action ) throws Exception {
		String KEY = action.context.CTX_KEYNAME;
		
		if( action.context.call != null ) {
			if( action.isLocalLinux() )
				runRemoteInteractiveSshLinux( action , account , KEY );
			else
			if( action.isLocalWindows() )
				runRemoteInteractiveSshWindows( action , account , KEY );
			else
				action.exitUnexpectedState();
			return;
		}

		if( action.isLocalLinux() )
			runLocalInteractiveSshLinux( action , account , KEY );
		else
		if( action.isLocalWindows() )
			runLocalInteractiveSshWindows( action , account , KEY );
		else
			action.exitUnexpectedState();
		
		tsLastInput = tsLastOutput = System.currentTimeMillis();
	}
	
	@Override
	public void kill( ActionBase action ) throws Exception {
		process.destroy();
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
		process = pb.start();
	}
	
	private void runLocalInteractiveSshWindows( ActionBase action , Account account , String KEY ) throws Exception {
		String cmd = "plink ";
		if( !KEY.isEmpty() )
			cmd += "-i " + KEY + " ";
		if( account.PORT != 22 )
			cmd += "-P " + account.PORT + " ";
		cmd += account.USER + "@" + account.HOST;
		
		action.trace( account.getPrintName() + " execute: " + cmd );
		
		ProcessBuilder pb = new ProcessBuilder( "cmd" , "/C" , cmd );
		process = pb.start();
	}
	
	private void runRemoteInteractiveSshLinux( ActionBase action , Account account , String KEY ) throws Exception {
		String cmd = "ssh -T " + account.getSshAddr();
		if( !KEY.isEmpty() )
			cmd += " -i " + KEY;
		
		action.trace( account.getPrintName() + " execute: " + cmd );
		
		ServerCommandCall call = action.context.call;
		ProcessBuilder pb = new ProcessBuilder( "sh" , "-c" , cmd );
		pb.redirectErrorStream( true );
		
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
		pb.redirectErrorStream( true );
		
		executeInteractive( action , call , pb );
	}
	
	private void executeInteractive( ActionBase action , ServerCommandCall call , ProcessBuilder pb ) throws Exception {
		process = pb.start();
		
		stdin = process.getOutputStream();
		writer = new OutputStreamWriter( stdin );
		
		stderr = process.getErrorStream();
		stdout = process.getInputStream();
		
		reader = new BufferedReader( new InputStreamReader( stdout ) );
		errreader = new BufferedReader( new InputStreamReader( stderr ) );
		
		addInput( action , "echo " + CONNECT_MARKER );
		
		WaiterCommand waiter = new WaiterCommand( this , action.context.logLevelLimit , reader , errreader );
		if( !waiter.waitForMarker( action , CONNECT_MARKER ) ) {
			call.connectFinished( false );
			action.exit( "unable to connect to " + name );
		}
		
		call.connectFinished( true );
	}

	public void runLocalInteractive( ActionBase action ) throws Exception {
		start( action );
		action.trace( name + " wait process to finish ..." );
		process.waitFor();
		pool.removeInteractive( action , this );
	}

	public void runRemoteInteractive( ActionBase action ) throws Exception {
		start( action );
		WaiterCommand waiter = new WaiterCommand( this , action.context.logLevelLimit , reader , errreader );
		waiter.waitInteractive( action , process );
		pool.removeInteractive( action , this );
	}
	
	public void addInput( ActionBase action , String input ) throws Exception {
		action.trace( name + " add to input: " + input );
		if( account.isLinux() )
			writer.write( input + "\n" );
		else
			writer.write( input + "\r\n" );
		writer.flush();
		tsLastInput = System.currentTimeMillis();
	}
	
}
