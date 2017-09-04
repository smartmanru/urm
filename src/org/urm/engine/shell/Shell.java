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
import org.urm.meta.engine.EngineAuthResource;

abstract public class Shell {

	abstract public boolean start( ActionBase action ) throws Exception;
	abstract public void kill( ActionBase action ) throws Exception;
	
	public int id;
	public String name;
	public EngineShellPool pool;
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
	
	public Shell( int id , String name , EngineShellPool pool , Account account ) {
		this.id = id;
		this.name = name;
		this.pool = pool;
		this.account = account;
		
		tsCreated = System.currentTimeMillis();
	}

	public void startProcess( ActionBase action , ShellProcess process , String rootPath , boolean redirect , EngineAuthResource auth ) throws Exception {
		this.rootPath = rootPath;
		this.process = process;
		
		process.start( action , rootPath , auth );
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
		available = false;
		wc.stop( action );
		killShellProcess( action );
		killOSProcess( action );
	}
	
	private void killShellProcess( ActionBase action ) throws Exception {
		if( this != pool.master )
			process.kill( action );
	}

	private synchronized void killOSProcess( ActionBase action ) {
		if( process == null )
			return;
		
		process.destroy( action );
		process = null;
		
		try {
			if( writer != null )
				writer.close();
		}
		catch( Throwable e ) {
			action.log( "kill process" , e );
		}

		try {
			if( stdin != null )
				stdin.close();
		}
		catch( Throwable e ) {
			action.log( "kill process" , e );
		}

		try {
			if( reader != null )
				reader.close();
		}
		catch( Throwable e ) {
			action.log( "kill process" , e );
		}

		try {
			if( stdout != null )
				stdout.close();
		}
		catch( Throwable e ) {
			action.log( "kill process" , e );
		}

		try {
			if( errreader != null )
				errreader.close();
		}
		catch( Throwable e ) {
			action.log( "kill process" , e );
		}

		try {
			if( stderr != null )
				stderr.close();
		}
		catch( Throwable e ) {
			action.log( "kill process" , e );
		}
		
		writer = null;
		stdin = null;
		reader = null;
		stdout = null;
		errreader = null;
		stderr = null;
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
		if( wc.waitForCommandFinished( action , logLevel , system , cmdout , cmderr ) )
			return;
		
		kill( action );
		action.exit0( _Error.CommandKilled , "Wait failed, command has been killed" );
	}

	public synchronized int waitForInteractive( ActionBase action ) throws Exception {
		if( process == null )
			return( -1 );
		return( process.waitForInteractive( action ) );
	}

	public boolean waitForMarker( ActionBase action , String marker , boolean system ) throws Exception {
		return( wc.waitForMarker( action , action.context.logLevelLimit , system , marker ) );
	}

	public String getPathBreak() {
		return( ( isWindows() )? ";" : ":" );
	}

	public String getVariable( String name ) {
		if( isWindows() )
			return( "%" + name + "%" );
		return( "$" + name );
	}
	
	public String getPathDelimiter() {
		if( isWindows() )
			return( "\\" );
		return( "/" );
	}

	public String getLocalPath( String path ) {
		if( isWindows() )
			return( Common.getWinPath( path ) );
		return( Common.getLinuxPath( path ) );
	}
	
}
