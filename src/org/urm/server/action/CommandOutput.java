package org.urm.server.action;

import java.io.PrintWriter;
import java.util.LinkedList;
import java.util.List;

import org.urm.common.Common;
import org.urm.common.ExitException;

public class CommandOutput {

	PrintWriter outchild = null;
	PrintWriter outtee = null;
	int logLevelLimit;

	public static int LOGLEVEL_INTERNAL = -1;
	public static int LOGLEVEL_ERROR = 0;
	public static int LOGLEVEL_INFO = 1;
	public static int LOGLEVEL_DEBUG = 2;
	public static int LOGLEVEL_TRACE = 3;
	
	List<PrintWriter> parentOutputs = new LinkedList<PrintWriter>();
	
	public CommandOutput() {
		logLevelLimit = 0;
	}

	public void setLogLevel( int logLevelLimit ) {
		this.logLevelLimit = logLevelLimit;
	}
	
	public synchronized void log( String s , int logLevel ) {
		if( logLevelLimit < 0 || logLevel <= logLevelLimit ) {
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
				outExact( "unexpected log level=" + logLevel + ", msg=" + s );
			out( prefix + s );
		}
	}
	
	public synchronized void logExact( String s , int logLevel ) {
		if( logLevelLimit < 0 || logLevel <= logLevelLimit )
			outExact( s );
	}
	
	public synchronized void log( String prompt , String stream , Throwable e ) {
		if( logLevelLimit < 0 ) {
			System.out.println( "TRACEINTERNAL: " + prompt );
			e.printStackTrace();
			return;
		}
		
		ExitException ee = Common.getExitException( e );
		String s = prompt;
		if( !s.isEmpty() )
			s += " ";
		
		if( ee != null ) {
			s += "exception: " + ee.getMessage();
			s += ", exiting [" + stream + "]";
		}
		else {
			s += "exception: " + e.getMessage();
			s += ", exiting [" + stream + "]";
		}
		error( s );

		if( logLevelLimit < LOGLEVEL_DEBUG )
			return;
		
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
	}
	
	public void error( String s ) {
		log( s , LOGLEVEL_ERROR );
	}
	
	public void info( String s ) {
		log( s , LOGLEVEL_INFO );
	}
	
	public void debug( String s ) {
		log( s , LOGLEVEL_DEBUG );
	}
	
	public void trace( String s ) {
		log( s , LOGLEVEL_TRACE );
	}
	
	private void outExact( String s ) {
		if( logLevelLimit < 0 ) {
			System.out.println( "TRACEINTERNAL: line=" + s.replaceAll("\\p{C}", "?") );
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
			
			System.out.println( s );
		}
	}
	
	private void out( String s ) {
		String ts = Common.getLogTimeStamp() + " " + s;
		outExact( ts );
	}

	public synchronized void exit( String s ) throws Exception {
		String errmsg = "ERROR: " + s + ". Exiting";

		if( logLevelLimit < 0 ) {
			System.out.println( "TRACEINTERNAL: exit, line=" + errmsg );
		}
		else {
			if( outchild != null ) {
				outchild.println( errmsg );
				outchild.flush();
			}
			else if( outtee != null ) {
				outtee.println( errmsg );
				outtee.flush();
			}
		}
			
		throw new ExitException( s );
	}
	
	public String getTimeStampedName( String basename , String ext ) {
        String dates = Common.getNameTimeStamp();
		String fname = basename + "-" + dates + "." + ext;
		return( fname );
	}
	
	public void tee( String title , String file ) throws Exception {
		outtee = Common.createOutfileFile( file );
		outtee.println( "############# start logging" );
		outtee.flush();
	}
	
	public void createOutputFile( String title , String file ) throws Exception {
		// add current to stack
		if( outchild != null )
			parentOutputs.add( outchild );
		
		outchild = Common.createOutfileFile( file );
		out( title );
	}
	
	public void stopOutputFile() throws Exception {
		outchild.flush();
		outchild.close();
		
		if( parentOutputs.isEmpty() )
			outchild = null;
		else {
			outchild = parentOutputs.get( parentOutputs.size() - 1 );
			parentOutputs.remove( parentOutputs.size() - 1 );
		}
	}
	
	public void stopAllOutputs() throws Exception {
		while( outchild != null )
			stopOutputFile();
		
		if( outtee != null ) {
			outtee.println( "############# stop logging" );
			outtee.flush();
			outtee.close();
			outtee = null;
		}
	}
}
