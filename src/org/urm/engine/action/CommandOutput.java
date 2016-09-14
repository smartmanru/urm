package org.urm.engine.action;

import java.io.PrintWriter;
import java.util.LinkedList;
import java.util.List;

import org.urm.action.ActionBase;
import org.urm.common.Common;
import org.urm.common.RunError;

public class CommandOutput {

	PrintWriter outchild = null;
	PrintWriter outtee = null;
	int logActionLevelLimit;
	int logServerLevelLimit;

	public static Object syncStatic = new Object(); 
	
	public static int LOGLEVEL_INTERNAL = -1;
	public static int LOGLEVEL_ERROR = 0;
	public static int LOGLEVEL_INFO = 1;
	public static int LOGLEVEL_DEBUG = 2;
	public static int LOGLEVEL_TRACE = 3;
	
	List<PrintWriter> parentOutputs = new LinkedList<PrintWriter>();
	
	public CommandOutput() {
		logActionLevelLimit = LOGLEVEL_ERROR;
		logServerLevelLimit = LOGLEVEL_ERROR;
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

	private void log( CommandContext context , String s , int logLevel ) {
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
			outExact( context , "unexpected log level=" + logLevel + ", msg=" + s );

		String ts = Common.getLogTimeStamp() + " " + prefix + s;
		outExact( context , ts + " " + context.streamLog );
		
		if( context.call != null ) {
			if( logActionLevelLimit >= 0 &&  
				logLevel > logActionLevelLimit )
				return;
		
			context.call.addLog( ts );
		}
	}
	
	public synchronized void logExact( CommandContext context , String s , int logLevel ) {
		if( logActionLevelLimit >= 0 && logServerLevelLimit >= 0 && 
			logLevel > logActionLevelLimit && logLevel > logServerLevelLimit )
			return;
		
		outExact( context , s );
		
		if( context.call != null ) {
			if( logActionLevelLimit >= 0 &&  
				logLevel > logActionLevelLimit )
				return;
		
			context.call.addLog( s );
		}
	}
	
	public synchronized void logExactInteractive( CommandContext context , String s , int logLevel ) {
		if( context.call != null )
			context.call.addLog( s );
		
		if( logActionLevelLimit >= 0 && logServerLevelLimit >= 0 && 
			logLevel > logActionLevelLimit && logLevel > logServerLevelLimit )
			return;
		
		outExact( context , s );
	}
	
	public synchronized void log( CommandContext context , String prompt , Throwable e ) {
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
			s += " ";
		
		if( ee != null ) {
			s += "exception: " + ee.errorCode + ", " + ee.errorMessage;
			s += ", exiting ";
		}
		else {
			s += "exception: " + e.getClass().getName();
			String msg = e.getMessage();
			if( msg != null )
				s += " - " + msg;
			s += ", exiting ";
		}
		
		synchronized( syncStatic ) {
			log( context , s , LOGLEVEL_ERROR );
			if( logActionLevelLimit >= LOGLEVEL_DEBUG || logServerLevelLimit >= LOGLEVEL_DEBUG ) {
				if( outchild != null ) {
					e.printStackTrace( outchild );
					outchild.flush();
				}
				else {
					if( outtee != null ) {
						e.printStackTrace( outtee );
						outtee.flush();
					}
					
					e.printStackTrace();
				}
				System.out.flush();
			}
		}
	}
	
	public synchronized void error( CommandContext context , String s ) {
		log( context , s , LOGLEVEL_ERROR );
	}
	
	public synchronized void info( CommandContext context , String s ) {
		log( context , s , LOGLEVEL_INFO );
	}
	
	public synchronized void debug( CommandContext context , String s ) {
		log( context , s , LOGLEVEL_DEBUG );
	}
	
	public synchronized void trace( CommandContext context , String s ) {
		log( context , s , LOGLEVEL_TRACE );
	}

	private void outExact( CommandContext context , String s ) {
		if( logActionLevelLimit < 0 || logServerLevelLimit < 0 ) {
			outExactStatic( "TRACEINTERNAL: line=" + s.replaceAll("\\p{C}", "?") );
			return;
		}
		
		if( outchild != null ) {
			outchild.println( s );
			outchild.flush();
		}
		else {
			if( outtee != null ) {
				outtee.println( s );
				outtee.flush();
			}
			
			outExactStatic( s );
		}
	}
	
	public String getTimeStampedName( String basename , String ext ) {
        String dates = Common.getNameTimeStamp();
		String fname = basename + "-" + dates + "." + ext;
		return( fname );
	}
	
	public synchronized void tee( String title , String file ) throws Exception {
		outtee = Common.createOutfileFile( file );
		outtee.println( "############# start logging on " + Common.getNameTimeStamp() );
		outtee.flush();
	}
	
	public synchronized void createOutputFile( CommandContext context , String title , String file ) throws Exception {
		// add current to stack
		if( outchild != null )
			parentOutputs.add( outchild );
		
		outchild = Common.createOutfileFile( file );
		log( context , title , LOGLEVEL_INFO );
	}
	
	public synchronized void stopOutputFile() throws Exception {
		outchild.flush();
		outchild.close();
		
		if( parentOutputs.isEmpty() )
			outchild = null;
		else {
			outchild = parentOutputs.get( parentOutputs.size() - 1 );
			parentOutputs.remove( parentOutputs.size() - 1 );
		}
	}
	
	public synchronized void stopAllOutputs() throws Exception {
		while( outchild != null )
			stopOutputFile();
		
		if( outtee != null ) {
			outtee.println( "############# stop logging on " + Common.getNameTimeStamp() );
			outtee.flush();
			outtee.close();
			outtee = null;
		}
	}
}
