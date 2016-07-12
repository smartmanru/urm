package org.urm.server.shell;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.List;

import org.urm.common.Common;
import org.urm.server.action.ActionBase;

abstract public class Shell {

	abstract public boolean start( ActionBase action ) throws Exception;
	abstract public void kill( ActionBase action ) throws Exception;
	
	public String name;
	public ShellPool pool;
	public Account account;
	public String rootPath;

	private Process process = null;
	public int processId = -1;
	
	private OutputStream stdin;
	private InputStream stderr;
	private InputStream stdout;
	private BufferedReader reader;
	private Writer writer;
	private BufferedReader errreader;

	public long tsCreated;
	public long tsLastInput = 0;
	public long tsLastOutput = 0;
	
	public Shell( String name , ShellPool pool , Account account ) {
		this.name = name;
		this.pool = pool;
		this.account = account;
		
		tsCreated = System.currentTimeMillis();
	}

	protected void startProcess( ActionBase action , ProcessBuilder builder , String rootPath , boolean redirect ) throws Exception {
		this.rootPath = rootPath;
		
		builder.directory( new File( rootPath ) );
		process = builder.start();
		
		// get process ID
		ShellCoreJNI osapi = pool.getOSAPI();
		if( action.isLocalLinux() )
			processId = osapi.getLinuxProcessId( action , process );
		else
			processId = osapi.getWindowsProcessId( action , process );
		action.debug( "process started: name=" + name + ", id=" + processId );

		if( !redirect )
			return;
		
		stdin = process.getOutputStream();
		writer = new OutputStreamWriter( stdin );
		
		stderr = process.getErrorStream();
		stdout = process.getInputStream();
		
		reader = new BufferedReader( new InputStreamReader( stdout ) );
		errreader = new BufferedReader( new InputStreamReader( stderr ) );
	}
	
	public void killShellProcess( ActionBase action ) throws Exception {
		if( this != pool.master && processId > 0 )
			pool.killShellProcess( action , processId );
	}

	public synchronized void killOSProcess( ActionBase action ) throws Exception {
		if( process == null )
			return;
		
		process.destroy();
			
		process = null;
		stdin = null;
		writer = null;
			
		stderr = null;
		stdout = null;
			
		reader = null;
		errreader = null;
	}
	
	public void setRootPath( String rootPath ) {
		this.rootPath = rootPath;
	}
	
	public synchronized String getOSPath( ActionBase action , String path ) throws Exception {
		if( account.isWindows() )
			return( Common.getWinPath( path ) );
		return( path );
	}
	
	public boolean isWindows() {
		return( account.isWindows() );
	}

	public boolean isLinux() {
		return( account.isLinux() );
	}

	public String getOSDevNull() {
		if( isLinux() )
			return( "/dev/null" );
		if( isWindows() )
			return( "nul" );
		return( null );
	}
	
	public void addInput( ActionBase action , String input , boolean system ) throws Exception {
		tsLastInput = System.currentTimeMillis();
		
		if( system == false || action.context.CTX_TRACEINTERNAL )
			action.trace( name + " execute: " + input );
		
		if( account.isLinux() )
			writer.write( input + "\n" );
		else
			writer.write( input + "\r\n" );
		
		writer.flush();
	}

	public void addInput( ActionBase action , byte[] input , boolean system ) throws Exception {
		tsLastInput = System.currentTimeMillis();
		
		if( system == false || action.context.CTX_TRACEINTERNAL )
			action.trace( name + " execute: " + input );
		
		stdin.write( input );
	}

	public void waitCommandFinished( ActionBase action , int logLevel , List<String> cmdout , List<String> cmderr , boolean windowsHelper ) throws Exception {
		WaiterCommand wc = new WaiterCommand( this , logLevel , reader , cmdout , errreader , cmderr );
		wc.waitForCommandFinished( action , false );
	}

	public synchronized int waitFor( ActionBase action ) throws Exception {
		return( process.waitFor() );
	}

	public boolean waitForMarker( ActionBase action , String marker ) throws Exception {
		WaiterCommand waiter = new WaiterCommand( this , action.context.logLevelLimit , reader , errreader );
		return( waiter.waitForMarker( action , marker ) );
	}
	
}
