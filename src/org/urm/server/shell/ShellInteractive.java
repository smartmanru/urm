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
	}
	
	@Override
	public void kill( ActionBase action ) throws Exception {
		process.destroy();
	}
	
	public void runLocalInteractiveSshLinux( ActionBase action , Account account , String KEY ) throws Exception {
		String cmd = "ssh " + account.getSshAddr();
		if( !KEY.isEmpty() )
			cmd += " -i " + KEY;
		cmd += " < /dev/tty > /dev/tty 2>&1";
		
		action.trace( account.getPrintName() + " execute: " + cmd );

		ProcessBuilder pb = new ProcessBuilder( "sh" , "-c" , cmd );
		process = pb.start();
	}
	
	public void runLocalInteractiveSshWindows( ActionBase action , Account account , String KEY ) throws Exception {
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
	
	public void runRemoteInteractiveSshLinux( ActionBase action , Account account , String KEY ) throws Exception {
		String cmd = "ssh -T " + account.getSshAddr();
		if( !KEY.isEmpty() )
			cmd += " -i " + KEY;
		
		action.trace( account.getPrintName() + " execute: " + cmd );
		
		ServerCommandCall call = action.context.call;
		ProcessBuilder pb = new ProcessBuilder( "sh" , "-c" , cmd , "2>&1" );
		executeInteractive( action , call , pb );
	}

	public void runRemoteInteractiveSshWindows( ActionBase action , Account account , String KEY ) throws Exception {
		String cmd = "plink ";
		if( !KEY.isEmpty() )
			cmd += "-i " + KEY + " ";
		if( account.PORT != 22 )
			cmd += "-P " + account.PORT + " ";
		cmd += account.USER + "@" + account.HOST;
		
		action.trace( account.getPrintName() + " execute: " + cmd );
		
		ServerCommandCall call = action.context.call;
		ProcessBuilder pb = new ProcessBuilder( "cmd" , "/C" , cmd , "2>&1" );
		executeInteractive( action , call , pb );
	}
	
	public void executeInteractive( ActionBase action , ServerCommandCall call , ProcessBuilder pb ) throws Exception {
		Process process = pb.start();
		
		stdin = process.getOutputStream();
		writer = new OutputStreamWriter( stdin );
		
		stderr = process.getErrorStream();
		stdout = process.getInputStream();
		
		reader = new BufferedReader( new InputStreamReader( stdout ) );
		errreader = new BufferedReader( new InputStreamReader( stderr ) );
		
		addInput( "echo " + CONNECT_MARKER );
		
		WaiterCommand waiter = new WaiterCommand( action.context.logLevelLimit , reader , errreader );
		if( !waiter.waitForMarker( action , CONNECT_MARKER ) )
			action.exit( "unable to connect to " + name );
	}

	public void runInteractive( ActionBase action ) throws Exception {
		start( action );
		process.waitFor();
	}
	
	public void waitFinished( ActionBase action ) throws Exception {
		WaiterCommand waiter = new WaiterCommand( action.context.logLevelLimit , reader , errreader );
		waiter.waitForProcess( action , process );
	}
	
	public void addInput( String input ) throws Exception {
		if( account.isLinux() )
			writer.write( input + "\n" );
		else
			writer.write( input + "\r\n" );
		writer.flush();
	}
	
}
