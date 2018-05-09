package org.urm.engine.shell;

import java.io.BufferedReader;
import java.util.List;

import org.urm.action.ActionBase;

public class ShellOutputWaiter {

	public Shell shell;
	private BufferedReader reader;
	private BufferedReader errreader;

	static final String FINISH_MARKER = "URM.MARKER";
	
	private ActionBase action;
	private int logLevel;
	private List<String> cmdout;
	private List<String> cmderr;

	public boolean waitForCommandFinished = false;
	public boolean waitForMarker = false;
	
	public String waitMarker;
	
	ShellWaiter waiter;
	
	public ShellOutputWaiter( Shell shell , BufferedReader reader , BufferedReader errreader ) {
		this.shell = shell;
		this.reader = reader;
		this.errreader = errreader;
		
		waiter = new ShellWaiter( this );
	}

	public void stop( ActionBase action ) {
		waiter.stop( action );
	}
	
	protected String readBuffer( ActionBase action , BufferedReader textreader , String buffer , char lineTerm , String stream ) throws Exception {
		if( action.context.CTX_TRACEINTERNAL )
			action.trace( shell.name + "-" + stream + ": readBuffer start reading ... " );
		
		String s = "";
		if( !textreader.ready() ) {
			char[] c = new char[1];
			int readN = textreader.read( c , 0 , 1 );
			if( readN != 1 ) {
				action.trace( shell.name + "-" + stream + ": readBuffer closed" );
				return( null );
			}
				
			s += c[0];
			buffer += c[0];
		}

		char buf[] = new char[ 100 ];
		String nextBuffer = buffer;
		while( textreader.ready() ) {
			int len = textreader.read( buf , 0 , 100 );
			
			if( len > 0 ) {
				boolean lineFound = false;
				for( int k = 0; k < len; k++ ) {
					s += buf[ k ];
					
					if( buf[ k ] == '\r' )
						continue;
					if( buf[ k ] == lineTerm )
						lineFound = true; 

					nextBuffer += buf[ k ];
				}
				
				if( lineFound )
					break;
			}
		}
		
		if( action.context.CTX_TRACEINTERNAL )
			action.trace( shell.name + "-" + stream + ": readBuffer part=" + s.replaceAll("\\p{C}", "?") );
		
		return( nextBuffer );
	}

	protected void skipUpTo( ActionBase action , BufferedReader textreader , char endChar ) throws Exception {
		if( action.context.CTX_TRACEINTERNAL )
			System.out.print( "INNER: skipUpTo part=" );

		char[] c = new char[1];
		while( true ) {
			if( textreader.read( c , 0 , 1 ) != 1 )
				action.exit0( _Error.UnableReadStream0 , "unable to read stream" );
			
			if( action.context.CTX_TRACEINTERNAL ) {
				String s = "" + c[0];
				System.out.print( s.replaceAll("\\p{C}", "?") );
			}
			
			if( c[0] == endChar )
				break;
		}
		
		if( action.context.CTX_TRACEINTERNAL )
			System.out.print( "\n" );
	}		

	protected boolean readStream( ActionBase action , BufferedReader textreader , List<String> text , String prompt , String stream ) throws Exception {
		if( textreader == null )
			action.exit0( _Error.MissingTextReader , "missing textreader" );
		
		String line;
		boolean first = true;
		
		String buffer = "";
		if( action.context.CTX_TRACEINTERNAL )
			action.trace( "readStream - start reading " + stream + " ..." );
		
		while ( true ) {
			int index = buffer.indexOf( '\n' );
			if( index < 0 ) {
				String newBuffer = readBuffer( action , textreader , buffer , '\n' , stream );
				if( newBuffer == null )
					return( false );
				
				buffer = newBuffer;
				continue;
			}
			
			line = buffer.substring( 0 , index );
			buffer = buffer.substring( index + 1 );
			
			if( action.context.CTX_TRACEINTERNAL )
				action.trace( "readStream - line=" + line.replaceAll("\\p{C}", "?") );
			
			index = line.indexOf( waitMarker );
			if( index >= 0 ) {
				line = line.substring( 0 , index );
				if( index > 0 ) {
					if( first && !prompt.isEmpty() ) {
						outStreamLine( action , prompt , text );
						first = false;
					}
					outStreamLine( action , line , text );
				}
			}
			else {
				if( first && !prompt.isEmpty() ) {
					outStreamLine( action , prompt , text );
					first = false;
				}
				outStreamLine( action , line , text );
			}
			
			if( index >= 0 )
				break;
		}
		
		return( true );
	}
		
	protected void outStreamLine( ActionBase action , String line , List<String> text ) throws Exception {
		if( text != null )
			text.add( line );
		
		action.logExact( line , logLevel );
	}

	public boolean waitForCommandFinished( ActionBase action , int logLevel , boolean system , List<String> cmdout , List<String> cmderr , int commandTimeoutMillis ) throws Exception {
		this.action = action;
		this.logLevel = logLevel;
		this.waitForCommandFinished = true;
		this.waitMarker = FINISH_MARKER;
		this.cmdout = cmdout;
		this.cmderr = cmderr;
		
		if( commandTimeoutMillis == Shell.WAIT_DEFAULT )
			commandTimeoutMillis = action.context.CTX_TIMEOUT;
		else
		if( commandTimeoutMillis == Shell.WAIT_INFINITE )
			commandTimeoutMillis = 0;
		
		return( waiter.wait( action , commandTimeoutMillis , logLevel , system ) );
	}
	
	public boolean waitForMarker( ActionBase action , int logLevel , boolean system , String marker , int commandTimeoutMillis ) throws Exception {
		this.action = action;
		this.logLevel = logLevel;
		this.waitForMarker = true;
		this.waitMarker = marker;
		
		if( commandTimeoutMillis == Shell.WAIT_DEFAULT )
			commandTimeoutMillis = action.context.CTX_TIMEOUT;
		else
		if( commandTimeoutMillis == Shell.WAIT_INFINITE )
			commandTimeoutMillis = 0;
		
		return( waiter.wait( action , commandTimeoutMillis , logLevel , system ) );
	}

	public boolean runWaitForCommandFinished() throws Exception {
		if( action.context.CTX_TRACEINTERNAL )
			action.trace( shell.name + ": runWaitForCommandFinished - read streams ..." );
		
		boolean reso = readStream( action , reader , cmdout , "" , "stdout" );
		boolean rese = readStream( action , errreader , cmderr , "stderr:" , "stderr" );
		if( reso == false || rese == false ) {
			if( action.context.CTX_TRACEINTERNAL )
				action.trace( shell.name + ": runWaitForCommandFinished - failed" );
			return( false );
		}
		
		if( action.context.CTX_TRACEINTERNAL )
			action.trace( shell.name + ": runWaitForCommandFinished - successfully completed" );
		return( true );
	}

	public boolean runWaitForMarker() throws Exception {
		if( !readStream( action , reader , null , "" , "stdout" ) ) {
			if( action.context.CTX_TRACEINTERNAL )
				action.trace( shell.name + ": runWaitForMarker - stdout failed" );
			return( false );
		}
		if( !readStream( action , errreader , null , "" , "stderr" ) ) {
			if( action.context.CTX_TRACEINTERNAL )
				action.trace( shell.name + ": runWaitForMarker - stderr failed" );
			return( false );
		}
		
		if( action.context.CTX_TRACEINTERNAL )
			action.trace( shell.name + ": runWaitForMarker - successfully completed" );
		return( true );
	}
	
	public boolean runWaitInteractive( ActionBase action ) throws Exception {
		while( true ) {
			try {
				synchronized( waiter ) {
					waiter.wait();
					if( waiter.isFinished() )
						break;
				}
			}
			catch( Throwable e ) {
				action.log( "runWaitInteractive" , e );
				return( false );
			}
		}
		return( true );
	}
	
}
