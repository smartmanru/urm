package ru.egov.urm.shell;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import ru.egov.urm.Common;
import ru.egov.urm.run.ActionBase;

public class ShellCore {

	ShellExecutor executor;
	int commandTimeoutDefault;
	
	List<String> cmdout; 
	List<String> cmderr;
	public String rootPath;
	public String homePath;
	public String processId;
	
	Process process = null;
	OutputStream stdin;
	InputStream stderr;
	InputStream stdout;
	BufferedReader reader;
	Writer writer;
	BufferedReader errreader;

	String cmdCurrent;
	public boolean running = false;
	public boolean initialized = false;
	
	static String finishMarker = "URM.MARKER";  

	int commandTimeout;
	
	public ShellCore( ShellExecutor executor , int commandTimeoutDefault ) {
		this.executor = executor;
		this.commandTimeoutDefault = commandTimeoutDefault;
		
		cmdout = new LinkedList<String>();
		cmderr = new LinkedList<String>();
		running = false;
		
		commandTimeout = commandTimeoutDefault;
	}

	public void setTimeout( ActionBase action , int timeout ) throws Exception {
		commandTimeout = timeout;
	}
	
	public void createProcess( ActionBase action , ProcessBuilder builder , String rootPath ) throws Exception {
		this.rootPath = rootPath;
		
		builder.directory( new File( rootPath ) );
		process = builder.start();
		
		stdin = process.getOutputStream();
		writer = new OutputStreamWriter( stdin );
		
		stderr = process.getErrorStream();
		stdout = process.getInputStream();
		
		reader = new BufferedReader( new InputStreamReader( stdout ) );
		errreader = new BufferedReader( new InputStreamReader( stderr ) );

		running = true;
		
		// get process ID
		processId = runCommandGetValueCheckDebug( action , "echo $$" );
		homePath = runCommandGetValueCheckDebug( action , "echo $HOME" );
		
		// run predefined exports
		String cmd = getExportCmd( action );
		if( !cmd.isEmpty() )
			runCommandCheckDebug( action , cmd );
		
		initialized = true;
	}
	
	public void kill( ActionBase action ) throws Exception {
		if( process != null ) {
			if( !processId.isEmpty() )
				action.context.master.custom( action , "kill -9 -" + processId );
				
			process.destroy();
			
			process = null;
			stdin = null;
			writer = null;
			
			stderr = null;
			stdout = null;
			
			reader = null;
			errreader = null;
		}
		
		running = false;
		initialized = false;
	}

	private String getExportCmd( ActionBase action ) throws Exception {
		if( action.meta == null || action.meta.product == null )
			return( "" );
		
		Map<String,String> exports = action.meta.product.getExportProperties( action );
		String cmd = "";
		for( String key : exports.keySet() ) {
			if( !cmd.isEmpty() )
				cmd += "; ";
			cmd += "export " + key + "=" + exports.get( key );
		}
		return( cmd );
	}

	private void exitError( ActionBase action , String error ) throws Exception {
		 executor.exitError( action , error );
	}
	
	public void runCommand( ActionBase action , String cmd , boolean debug ) throws Exception {
		if( !running )
			exitError( action , "attempt to run command in closed session: " + cmd );
			
		cmdCurrent = cmd;

		cmdout.clear();
		cmderr.clear();
		
		String execLine = cmd + "; echo " + finishMarker + " >&2; echo " + finishMarker + "\n";
		action.trace( executor.name + " execute: " + cmd );
		
		writer.write( execLine );
		try {
			writer.flush();
		}
		catch( Throwable e ) {
		}
		
		ShellWaiter waiter = new ShellWaiter( executor , new CommandReader( debug ) );
		boolean res = waiter.wait( action , commandTimeout );
		commandTimeout = commandTimeoutDefault;
		
		if( !res )
			exitError( action , "command has been killed" );
	}

	public String getOut() {
		return( getListValue( cmdout ) );
	}
	
	public String getErr() {
		return( getListValue( cmderr ) );
	}
	
	private String getListValue( List<String> lst ) {
		String res;
		
		if( lst.size() <= 0 )
			return( "" );
		
		res = lst.get( 0 );
		for( int k = 1; k < lst.size(); k++ ) {
			res += "\n" + lst.get( k );
		}
		
		return( res );
	}
	
	public String runCommandCheckNormal( ActionBase action , String cmd ) throws Exception {
		return( runCommandCheck( action , cmd , false ) ); 
	}

	public String runCommandCheckNormal( ActionBase action , String dir , String cmd ) throws Exception {
		String cmdDir = getDirCmd( action , dir , cmd );
		return( runCommandCheck( action , cmdDir , false ) ); 
	}

	public String runCommandCheckDebug( ActionBase action , String cmd ) throws Exception {
		return( runCommandCheck( action , cmd , true ) ); 
	}

	public String runCommandCheckDebugIfDir( ActionBase action , String dir , String cmd ) throws Exception {
		String cmdDir = getDirCmdIfDir( action , dir , cmd );
		return( runCommandCheck( action , cmdDir , true ) ); 
	}

	public void runCommand( ActionBase action , String dir , String cmd , boolean debug ) throws Exception {
		String cmdDir = getDirCmd( action , dir , cmd );
		runCommand( action , cmdDir , debug ); 
	}

	public String runCommandCheckDebug( ActionBase action , String dir , String cmd ) throws Exception {
		String cmdDir = getDirCmd( action , dir , cmd );
		return( runCommandCheck( action , cmdDir , true ) ); 
	}

	public String[] runCommandGetLines( ActionBase action , String cmd , boolean debug ) throws Exception {
		runCommand( action , cmd , debug );
		String err = getErr();
		
		if( !err.isEmpty() )
			exitError( action , "error running command (" + cmd + ")" + " - " + err );
		
		return( cmdout.toArray( new String[0] ) );
	}
	
	public String runCommandCheck( ActionBase action , String cmd , boolean debug ) throws Exception {
		runCommand( action , cmd , debug );
		String err = getErr();
		
		if( !err.isEmpty() )
			exitError( action , "error running command (" + cmd + ")" + " - " + err );

		String out = getOut();
		return( out );
	}

	public int runCommandGetStatusNormal( ActionBase action , String cmd ) throws Exception {
		return( runCommandGetStatus( action , cmd , false ) );
	}
	
	public int runCommandGetStatusDebug( ActionBase action , String cmd ) throws Exception {
		return( runCommandGetStatus( action , cmd , true ) );
	}
	
	public int runCommandGetStatus( ActionBase action , String cmd , boolean debug ) throws Exception {
		runCommand( action , cmd + "; echo COMMAND_STATUS=$?" , debug );
		
		if( cmdout.size() > 0 ) {
			String last = cmdout.get( cmdout.size() - 1 );
			if( last.startsWith( "COMMAND_STATUS=" ) ) {
				String ss = last.substring( "COMMAND_STATUS=".length() );
				int value = Integer.parseInt( ss );
				cmdout.remove( cmdout.size() - 1 );
				return( value );
			}
		}
				
		exitError( action , "unable to obtain command status" );
		return( -1 );
	}

	public void runCommandCheckStatusNormal( ActionBase action , String cmd ) throws Exception {
		runCommandCheckStatus( action , cmd , false );
	}
	
	public void runCommandCheckStatusNormal( ActionBase action , String dir , String cmd ) throws Exception {
		String cmdDir = getDirCmd( action , dir , cmd );
		runCommandCheckStatus( action , cmdDir , false );
	}
	
	public void runCommandCheckStatusDebug( ActionBase action , String cmd ) throws Exception {
		runCommandCheckStatus( action , cmd , true );
	}
	
	public void runCommandCheckStatusDebug( ActionBase action , String dir , String cmd ) throws Exception {
		String cmdDir = getDirCmd( action , dir , cmd );
		runCommandCheckStatus( action , cmdDir , true );
	}
	
	public void runCommandCheckStatus( ActionBase action , String cmd , boolean debug ) throws Exception {
		int status = runCommandGetStatus( action , cmd , debug );
		if( status != 0 )
			exitError( action , "error executing command: " + cmd + ", status=" + status + ", stderr: " + getErr() );
	}

	public String runCommandGetValueCheckNormal( ActionBase action , String cmd ) throws Exception {
		return( runCommandGetValueCheck( action , cmd , false ) );
	}
	
	public String runCommandGetValueCheckNormal( ActionBase action , String dir , String cmd ) throws Exception {
		String cmdDir = getDirCmd( action , dir , cmd );
		return( runCommandGetValueCheck( action , cmdDir , false ) );
	}
	
	public String runCommandGetValueCheckDebug( ActionBase action , String cmd ) throws Exception {
		return( runCommandGetValueCheck( action , cmd , true ) );
	}
	
	public String runCommandGetValueCheckDebug( ActionBase action , String dir , String cmd ) throws Exception {
		String cmdDir = getDirCmd( action , dir , cmd );
		return( runCommandGetValueCheck( action , cmdDir , true ) );
	}
	
	public String runCommandGetValueCheck( ActionBase action , String cmd , boolean debug ) throws Exception {
		String value = runCommandCheck( action , cmd , debug );
		value = value.trim();
		return( value );
	}

	public String runCommandGetValueNoCheck( ActionBase action , String cmd , boolean debug ) throws Exception {
		runCommand( action , cmd , debug );
		String value = getOut();
		return( value );
	}

	public List<String> runCommandCheckGetOutputDebug( ActionBase action , String dir , String cmd ) throws Exception {
		String cmdDir = getDirCmd( action , dir , cmd );
		return( runCommandCheckGetOutputDebug( action , cmdDir ) );
	}
	
	public List<String> runCommandCheckGetOutputDebug( ActionBase action , String cmd ) throws Exception {
		runCommand( action , cmd , true );
		String err = getErr();
		
		if( !err.isEmpty() )
			exitError( action , "error running command (" + cmd + ")" + " - " + err );

		return( cmdout );
	}
	
	public String getDirCmd( ActionBase action , String dir , String cmd ) throws Exception {
		return( "( if [ -d " + Common.getQuoted( dir ) + " ]; then cd " + dir + "; " + cmd + "; else echo invalid directory: " + dir + " >&2; fi )" );
	}
	
	public String getDirCmdIfDir( ActionBase action , String dir , String cmd ) throws Exception {
		return( "( if [ -d " + Common.getQuoted( dir ) + " ]; then cd " + dir + "; " + cmd + "; fi )" );
	}
	
	class CommandReader extends WaiterCommand {
		
		boolean debug;
		
		public CommandReader( boolean debug ) {
			this.debug = debug;
		}
		
		public void run( ActionBase action ) throws Exception {
			readStreamToMarker( action , reader , cmdout , "" );
			readStreamToMarker( action , errreader , cmderr , "stderr:" );
		}
		
		private void outStreamLine( ActionBase action , String line ) throws Exception {
			if( debug )
				action.trace( line );
			else
				action.log( line );
		}
		
		private void readStreamToMarker( ActionBase action , BufferedReader textreader , List<String> text , String prompt ) throws Exception {
			String line;
			boolean first = true;
			while ( true ) {
				line = textreader.readLine();
				if( line == null )
					break;
				
				int index = line.indexOf( finishMarker );
				if( index >= 0 ) {
					line = line.substring( 0 , index );
					if( index > 0 ) {
						text.add( line );
						if( first && !prompt.isEmpty() ) {
							outStreamLine( action , prompt );
							first = false;
						}
						outStreamLine( action , line );
					}
				}
				else {
					text.add( line );
					if( first && !prompt.isEmpty() ) {
						outStreamLine( action , prompt );
						first = false;
					}
					outStreamLine( action , line );
				}
				
				if( index >= 0 )
					break;
			}
		}
	}
	
}
