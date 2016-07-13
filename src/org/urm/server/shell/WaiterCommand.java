package org.urm.server.shell;

import java.io.BufferedReader;
import java.util.List;

import org.urm.server.action.ActionBase;
import org.urm.server.action.CommandOutput;

public class WaiterCommand implements Runnable {

	public Shell shell;
	public ActionBase action;
	public Thread thread;
	
	protected boolean windowsHelper;
	public boolean finished;
	public boolean succeeded;

	static final String FINISH_MARKER = "URM.MARKER";
	
	private int logLevel;
	private BufferedReader reader;
	private List<String> cmdout;
	private BufferedReader errreader;
	private List<String> cmderr;

	boolean waitForCommandFinished = false;
	boolean waitForMarker = false;
	boolean waitInifinite = false;
	
	public String waitMarker;
	public Process waitProcess;
	
	public WaiterCommand( Shell shell , int logLevel , BufferedReader reader , List<String> cmdout , BufferedReader errreader , List<String> cmderr ) {
		this.shell = shell;
		this.logLevel = logLevel;
		this.reader = reader;
		this.cmdout = cmdout;
		this.errreader = errreader;
		this.cmderr = cmderr;
	}
	
	public WaiterCommand( Shell shell , int logLevel , BufferedReader reader , BufferedReader errreader ) {
		this.shell = shell;
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
            succeeded = false;

            if( waitForCommandFinished )
            	runWaitForCommandFinished();
            else
            if( waitForMarker )
            	runWaitForMarker();
            else
            if( waitInifinite )
            	runWaitInfinite();
            else
            	action.exitUnexpectedState();
            
            succeeded = true;
        }
        catch (Exception e) {
        	succeeded = false;
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

	protected boolean readStream( ActionBase action , BufferedReader textreader , List<String> text , String prompt ) throws Exception {
		if( textreader == null )
			action.exit( "missing reader" );
		
		String line;
		boolean first = true;
		
		String buffer = "";
		if( action.context.CTX_TRACEINTERNAL )
			action.trace( "readStream - start reading ..." );
		
		while ( true ) {
			int index = buffer.indexOf( '\n' );
			if( index < 0 ) {
				String newBuffer = readBuffer( action , textreader , buffer , '\n' );
				if( newBuffer == null )
					return( false );
				
				buffer = newBuffer;
				continue;
			}
			
			line = buffer.substring( 0 , index );
			buffer = buffer.substring( index + 1 );
			
			if( action.context.CTX_TRACEINTERNAL )
				action.trace( "readStream - line=" + line.replaceAll("\\p{C}", "?") );
			
			if( waitInifinite ) {
				if( first && !prompt.isEmpty() ) {
					outStreamLine( action , prompt , text );
					first = false;
				}
				
				outStreamLine( action , line , text );
				continue;
			}
			
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
		
		if( waitInifinite )
			action.logExactInteractive( line , CommandOutput.LOGLEVEL_TRACE );
		else
			action.logExact( line , logLevel );
	}

	public void waitForCommandFinished( ActionBase action , boolean windowsHelper ) throws Exception {
		waitForCommandFinished = true;
		waitMarker = FINISH_MARKER;
		ShellWaiter waiter = new ShellWaiter( this );
		
		if( windowsHelper )
			waiter.setWindowsHelper();
		
		if( !waiter.wait( action , action.commandTimeout ) )
			action.exit( "command has been killed" );
	}
	
	public boolean waitForMarker( ActionBase action , String marker ) throws Exception {
		waitForMarker = true;
		waitMarker = marker;
		ShellWaiter waiter = new ShellWaiter( this );
		
		if( windowsHelper )
			waiter.setWindowsHelper();
		
		return( waiter.wait( action , action.commandTimeout ) );
	}

	public boolean waitInfinite( ActionBase action , Process process ) throws Exception {
		waitInifinite = true;
		waitProcess = process;
		ShellWaiter waiter = new ShellWaiter( this );
		
		if( windowsHelper )
			waiter.setWindowsHelper();
		
		return( waiter.wait( action , action.commandTimeout ) );
	}
	
	private void runWaitForCommandFinished() throws Exception {
		boolean reso = readStream( action , reader , cmdout , "" );
		boolean rese = readStream( action , errreader , cmderr , "stderr:" );
		if( reso == false || rese == false )
			action.exit( "operation is canceled, stream is closed" );
	}

	private void runWaitForMarker() throws Exception {
		if( !readStream( action , reader , null , "" ) )
			action.exit( "operation is canceled, stream is closed" );
	}
	
	private void runWaitInfinite() throws Exception {
		readStream( action , reader , null , "" );
	}
	
}
