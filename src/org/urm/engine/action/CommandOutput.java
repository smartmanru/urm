package org.urm.engine.action;

import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.LinkedList;
import java.util.List;

import org.urm.action.ActionBase;
import org.urm.common.Common;
import org.urm.common.RunContext;
import org.urm.common.RunError;

public class CommandOutput {

	class OutputFile {
		String fname;
		FileOutputStream outstream;
		PrintWriter outfile;
		int channelBase;
		boolean stopped;
		
		public OutputFile( RunContext execrc , String fname , int channelBase ) throws Exception {
			this.fname = fname;
			this.outstream = new FileOutputStream( execrc.getLocalPath( fname ) );
			this.outfile = new PrintWriter( outstream );
			this.channelBase = channelBase;
			this.stopped = false;
		}

		public void close() throws Exception {
			outfile.flush();
			outfile.close();
			outstream.flush();
			outstream.close();
			stopped = true;
		}
		
		public void printStackTrace( Throwable e ) {
			e.printStackTrace( outfile );
			outfile.flush();
		}
		
		public void println( String s ) {
			outfile.println( s );
			outfile.flush();
		}
		
	};
	
	List<OutputFile> channels;
	OutputFile outtee;
	int logActionLevelLimit;
	int logServerLevelLimit;

	public static Object syncStatic = new Object(); 
	
	public static int LOGLEVEL_INTERNAL = -1;
	public static int LOGLEVEL_ERROR = 0;
	public static int LOGLEVEL_INFO = 1;
	public static int LOGLEVEL_DEBUG = 2;
	public static int LOGLEVEL_TRACE = 3;
	
	public CommandOutput() {
		logActionLevelLimit = LOGLEVEL_ERROR;
		logServerLevelLimit = LOGLEVEL_ERROR;
		channels = new LinkedList<OutputFile>(); 
	}

	public synchronized void setLogLevel( ActionBase action , int logActionLevelLimit ) {
		this.logActionLevelLimit = logActionLevelLimit;
		
		if( action.engine.serverAction == null )
			this.logServerLevelLimit = this.logActionLevelLimit;
		else
			this.logServerLevelLimit = action.engine.serverAction.context.logLevelLimit;
	}
	
	private void outExactStatic( String s ) {
		synchronized( syncStatic ) {
			System.out.println( s );
		}
	}

	private void log( CommandContext context , int channel , String s , int logLevel ) {
		if( logActionLevelLimit >= 0 && logServerLevelLimit >= 0 && 
			logLevel > logActionLevelLimit && logLevel > logServerLevelLimit )
			return;
		
		String prefix = null;
		if( logLevel == LOGLEVEL_ERROR )
			prefix = "[ERROR] ";
		else
		if( logLevel == LOGLEVEL_INFO )
			prefix = "[INFO ] ";
		else
		if( logLevel == LOGLEVEL_DEBUG )
			prefix = "[DEBUG] ";
		else
		if( logLevel == LOGLEVEL_TRACE )
			prefix = "[TRACE] ";
		else
			outExact( context , channel , "unexpected log level=" + logLevel + ", msg=" + s );

		String ts = Common.getLogTimeStamp() + " " + prefix + s;
		outExact( context , channel , ts + " " + context.streamLog );
		
		if( context.call != null ) {
			if( logActionLevelLimit >= 0 &&  
				logLevel > logActionLevelLimit )
				return;
		
			context.call.addLog( ts );
		}
	}
	
	public synchronized void logExact( CommandContext context , int channel , String s , int logLevel ) {
		if( logActionLevelLimit >= 0 && logServerLevelLimit >= 0 && 
			logLevel > logActionLevelLimit && logLevel > logServerLevelLimit )
			return;
		
		outExact( context , channel , s );
		
		if( context.call != null ) {
			if( logActionLevelLimit >= 0 &&  
				logLevel > logActionLevelLimit )
				return;
		
			context.call.addLog( s );
		}
	}
	
	public synchronized void logExactInteractive( CommandContext context , int channel , String s , int logLevel ) {
		if( context.call != null )
			context.call.addLog( s );
		
		if( logActionLevelLimit >= 0 && logServerLevelLimit >= 0 && 
			logLevel > logActionLevelLimit && logLevel > logServerLevelLimit )
			return;
		
		outExact( context , channel , s );
	}
	
	public synchronized void log( CommandContext context , int channel , String prompt , Throwable e ) {
		if( logActionLevelLimit < 0 || logServerLevelLimit < 0 ) {
			synchronized( syncStatic ) {
				System.out.println( "TRACEINTERNAL: " + prompt );
				e.printStackTrace();
				System.out.flush();
			}
			return;
		}
		
		RunError ee = Common.getExitException( e );
		String s = prompt;
		if( !s.isEmpty() )
			s += " - ";
		
		if( ee != null ) {
			s += "exception: " + ee.getCode() + ", " + ee.getMessage();
		}
		else {
			s += "exception: " + e.getClass().getName();
			String msg = e.getMessage();
			if( msg != null )
				s += " - " + msg;
		}
		
		synchronized( syncStatic ) {
			log( context , channel , s , LOGLEVEL_ERROR );
			if( logActionLevelLimit >= LOGLEVEL_DEBUG || logServerLevelLimit >= LOGLEVEL_DEBUG ) {
				if( channel >= 0 && channel < channels.size() ) {
					OutputFile outchild = channels.get( channel );
					outchild.printStackTrace( e );
				}
				else {
					if( outtee != null )
						outtee.printStackTrace( e );
					
					e.printStackTrace();
					System.out.flush();
				}
			}
		}
	}
	
	public synchronized void error( CommandContext context , int channel , String s ) {
		log( context , channel , s , LOGLEVEL_ERROR );
	}
	
	public synchronized void info( CommandContext context , int channel , String s ) {
		log( context , channel , s , LOGLEVEL_INFO );
	}
	
	public synchronized void debug( CommandContext context , int channel , String s ) {
		log( context , channel , s , LOGLEVEL_DEBUG );
	}
	
	public synchronized void trace( CommandContext context , int channel , String s ) {
		log( context , channel , s , LOGLEVEL_TRACE );
	}

	private synchronized void outExact( CommandContext context , int channel , String s ) {
		if( logActionLevelLimit < 0 || logServerLevelLimit < 0 ) {
			outExactStatic( "TRACEINTERNAL: line=" + s.replaceAll("\\p{C}", "?") );
			return;
		}
		
		context.outExact( s );
		if( channel >= 0 && channel < channels.size() ) {
			OutputFile outchild = channels.get( channel );
			outchild.println( s );
		}
		else {
			if( outtee != null )
				outtee.println( s );
			
			outExactStatic( s );
		}
	}
	
	public String getTimeStampedName( String basename , String ext ) {
        String dates = Common.getNameTimeStamp();
		String fname = basename + "-" + dates + "." + ext;
		return( fname );
	}
	
	public synchronized void tee( RunContext execrc , String title , String file ) throws Exception {
		outtee = new OutputFile( execrc , file , 0 );
		outtee.println( "############# start logging on " + Common.getNameTimeStamp() );
	}
	
	public synchronized void stopAllOutputs() throws Exception {
		for( int index = channels.size() - 1; index >= 0; index-- ) {
			OutputFile outchild = channels.get( index );
			if( !outchild.stopped )
				stopOutputFile( outchild );
		}
		channels.clear();
		
		if( outtee != null ) {
			outtee.println( "############# stop logging on " + Common.getNameTimeStamp() );
			outtee.close();
			outtee = null;
		}
	}
	
	public synchronized int startRedirect( CommandContext context , int channel , String file , String msg , String title ) throws Exception {
		OutputFile outchild = new OutputFile( context.engine.execrc , file , channel );
		int newChannel = channels.size();
		channels.add( outchild );
		
		log( context , channel , msg , LOGLEVEL_INFO );
		info( context , newChannel , title );
		return( newChannel );
	}
	
	public synchronized int stopRedirect( int channel ) throws Exception {
		if( channel >= 0 && channel < channels.size() ) {
			OutputFile outchild = channels.get( channel );
			if( !outchild.stopped )
				stopOutputFile( outchild );
			
			int index = channels.size() - 1;
			if( channel == index ) {
				// remove closed tail
				for( ; index >= 0; index-- ) {
					outchild = channels.get( index );
					if( !outchild.stopped )
						break;
					
					channels.remove( index );
				}
			}
			
			if( outchild.channelBase < channels.size() )
				return( outchild.channelBase );
		}
		
		return( -1 );
	}
	
	private void stopOutputFile( OutputFile outchild ) throws Exception {
		outchild.close();
	}
	
}
