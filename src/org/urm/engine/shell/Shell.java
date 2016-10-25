package org.urm.engine.shell;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.List;

import org.urm.action.ActionBase;
import org.urm.common.Common;

abstract public class Shell {

	abstract public boolean start( ActionBase action ) throws Exception;
	abstract public void kill( ActionBase action ) throws Exception;
	
	public String name;
	public ShellPool pool;
	public Account account;
	public String rootPath;

	public ShellProcess process;
	public boolean available;
	
	private OutputStream stdin;
	private InputStream stderr;
	private InputStream stdout;
	private BufferedReader reader;
	private Writer writer;
	private BufferedReader errreader;

	public long tsCreated;
	public long tsLastInput = 0;
	public long tsLastOutput = 0;
	
	ShellOutputWaiter wc;
	
	public Shell( String name , ShellPool pool , Account account ) {
		this.name = name;
		this.pool = pool;
		this.account = account;
		
		tsCreated = System.currentTimeMillis();
	}

	public void startProcess( ActionBase action , ShellProcess process , String rootPath , boolean redirect ) throws Exception {
		this.rootPath = rootPath;
		this.process = process;
		
		process.start( action , rootPath );
		available = true;

		if( !redirect )
			return;

		// redirect streams
		String encoding = pool.engine.execrc.encoding;
		stdin = process.getOutputStream();
		if( !encoding.isEmpty() )
			writer = new OutputStreamWriter( stdin , encoding );
		else
			writer = new OutputStreamWriter( stdin );
		
		stderr = process.getErrorStream();
		stdout = process.getInputStream();
		
		reader = getStreamReader( stdout );
		errreader = getStreamReader( stderr );
		
		wc = new ShellOutputWaiter( this , reader , errreader );
	}

	private BufferedReader getStreamReader( InputStream stream ) throws Exception {
		String encoding = pool.engine.execrc.encoding;
		if( !encoding.isEmpty() )
			return( new BufferedReader( new InputStreamReader( stream , encoding ) ) );
		return( new BufferedReader( new InputStreamReader( stream ) ) );
	}
	
	public void setUnavailable() {
		process.setUnavailable();
		available = false;
	}
	
	public void killProcess( ActionBase action ) throws Exception {
		action.trace( "kill process shell=" + name + " ..." );
		killShellProcess( action );
		killOSProcess( action );
		wc.stop( action );
	}
	
	private void killShellProcess( ActionBase action ) throws Exception {
		if( this != pool.master )
			process.kill( action );
	}

	private synchronized void killOSProcess( ActionBase action ) throws Exception {
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
			action.trace( name + " add to input stream: " + input );
		
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

	public void waitCommandFinished( ActionBase action , int logLevel , List<String> cmdout , List<String> cmderr , boolean system ) throws Exception {
		wc.waitForCommandFinished( action , logLevel , system , cmdout , cmderr );
	}

	public synchronized int waitFor( ActionBase action ) throws Exception {
		return( process.waitFor( action ) );
	}

	public boolean waitForMarker( ActionBase action , String marker , boolean system ) throws Exception {
		return( wc.waitForMarker( action , action.context.logLevelLimit , system , marker ) );
	}
	
}
