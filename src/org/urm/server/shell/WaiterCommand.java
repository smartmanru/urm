package org.urm.server.shell;

import java.io.BufferedReader;
import java.util.List;

import org.urm.server.action.ActionBase;

public class WaiterCommand implements Runnable {

	public ActionBase action;
	public Thread thread;
	
	protected boolean windowsHelper;
	public boolean finished;
	public boolean exception;

	static final String FINISH_MARKER = "URM.MARKER";
	
	private int logLevel;
	private BufferedReader reader;
	private List<String> cmdout;
	private BufferedReader errreader;
	private List<String> cmderr;

	boolean waitForCommandFinished = false;
	boolean waitForMarker = false;
	boolean waitForProcess = false;
	
	public String waitMarker;
	public Process waitProcess;
	
	public WaiterCommand( int logLevel , BufferedReader reader , List<String> cmdout , BufferedReader errreader , List<String> cmderr ) {
		this.logLevel = logLevel;
		this.reader = reader;
		this.cmdout = cmdout;
		this.errreader = errreader;
		this.cmderr = cmderr;
	}
	
	public WaiterCommand( int logLevel , BufferedReader reader , BufferedReader errreader ) {
		this.logLevel = logLevel;
		this.reader = reader;
		this.errreader = errreader;
	}
	
	public void setWindowsHelper() {
		windowsHelper = true;
	}
	
    public void run() {
        try {
            finished = false;
            exception = false;

            if( waitForCommandFinished )
            	runWaitForCommandFinished();
            else
            if( waitForMarker )
            	runWaitForMarker();
            else
            if( waitForProcess )
            	runWaitForProcess();
            else
            	action.exitUnexpectedState();
        }
        catch (Exception e) {
            exception = true;
            action.log( e );
        }
        
        finished = true;
        synchronized ( this ) {
            notifyAll();
        }
    }
	
	protected String readBuffer( ActionBase action , BufferedReader textreader , String buffer , char lineTerm ) throws Exception {
		if( action.context.CTX_TRACEINTERNAL )
			action.trace( "readBuffer start reading ... " );
		
		String s = "";
		if( !textreader.ready() ) {
			char[] c = new char[1];
			if( textreader.read( c , 0 , 1 ) != 1 )
				return( null );
				
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
			action.trace( "readBuffer part=" + s.replaceAll("\\p{C}", "?") );
		
		return( nextBuffer );
	}

	protected void skipUpTo( ActionBase action , BufferedReader textreader , char endChar ) throws Exception {
		if( action.context.CTX_TRACEINTERNAL )
			System.out.print( "TRACEINTERNAL: skipUpTo part=" );

		char[] c = new char[1];
		while( true ) {
			if( textreader.read( c , 0 , 1 ) != 1 )
				action.exit( "unable to read" );
			
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

	protected void readStreamToMarker( ActionBase action , BufferedReader textreader , List<String> text , String prompt ) throws Exception {
		String line;
		boolean first = true;
		
		String buffer = "";
		if( action.context.CTX_TRACEINTERNAL )
			action.trace( "readStreamToMarker - start reading ..." );
		
		while ( true ) {
			int index = buffer.indexOf( '\n' );
			if( index < 0 ) {
				String newBuffer = readBuffer( action , textreader , buffer , '\n' );
				if( newBuffer != null )
					buffer = newBuffer;
				continue;
			}
			
			line = buffer.substring( 0 , index );
			buffer = buffer.substring( index + 1 );
			
			if( action.context.CTX_TRACEINTERNAL )
				action.trace( "readStreamToMarker - line=" + line.replaceAll("\\p{C}", "?") );
			
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
	}
		
	protected void outStreamLine( ActionBase action , String line , List<String> text ) throws Exception {
		if( text != null )
			text.add( line );
		action.logExact( line , logLevel );
	}

	public void waitForCommandFinished( ActionBase action , ShellExecutor shell , boolean windowsHelper ) throws Exception {
		waitForCommandFinished = true;
		waitMarker = FINISH_MARKER;
		ShellWaiter waiter = new ShellWaiter( this );
		
		if( windowsHelper )
			waiter.setWindowsHelper();
		
		if( !waiter.wait( action , action.commandTimeout ) )
			shell.exitError( action , "command has been killed" );
	}
	
	public boolean waitForMarker( ActionBase action , String marker ) throws Exception {
		waitForMarker = true;
		waitMarker = marker;
		ShellWaiter waiter = new ShellWaiter( this );
		
		if( windowsHelper )
			waiter.setWindowsHelper();
		
		return( waiter.wait( action , action.commandTimeout ) );
	}

	public boolean waitForProcess( ActionBase action , Process process ) throws Exception {
		waitForProcess = true;
		waitProcess = process;
		ShellWaiter waiter = new ShellWaiter( this );
		
		if( windowsHelper )
			waiter.setWindowsHelper();
		
		return( waiter.wait( action , action.commandTimeout ) );
	}
	
	private void runWaitForCommandFinished() throws Exception {
		readStreamToMarker( action , reader , cmdout , "" );
		readStreamToMarker( action , errreader , cmderr , "stderr:" );
	}

	private void runWaitForMarker() throws Exception {
		readStreamToMarker( action , reader , null , "" );
	}
	
	private void runWaitForProcess() throws Exception {
		waitProcess.waitFor();
	}
	
}
