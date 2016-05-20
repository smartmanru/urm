package ru.egov.urm.action;

import java.io.PrintWriter;
import java.util.LinkedList;
import java.util.List;

import ru.egov.urm.Common;
import ru.egov.urm.ExitException;

public class CommandOutput {

	PrintWriter outchild = null;
	PrintWriter outtee = null;
	int logLevelLimit;

	public static int LOGLEVEL_INTERNAL = -1;
	public static int LOGLEVEL_INFO = 0;
	public static int LOGLEVEL_DEBUG = 1;
	public static int LOGLEVEL_TRACE = 2;
	
	List<PrintWriter> parentOutputs = new LinkedList<PrintWriter>();
	
	public CommandOutput() {
		logLevelLimit = 0;
	}

	public void setLogLevel( int logLevelLimit ) {
		this.logLevelLimit = logLevelLimit;
	}
	
	public void log( String s , int logLevel ) throws Exception {
		if( logLevelLimit < 0 || logLevel <= logLevelLimit ) {
			String prefix = null;
			if( logLevel == LOGLEVEL_INFO )
				prefix = "[INFO ] ";
			else
			if( logLevel == LOGLEVEL_DEBUG )
				prefix = "[DEBUG] ";
			else
			if( logLevel == LOGLEVEL_TRACE )
				prefix = "[TRACE] ";
			else
				throw new ExitException( "unexpected log level" );
			out( prefix + s );
		}
	}
	
	public void logExact( String s , int logLevel ) throws Exception {
		if( logLevelLimit < 0 || logLevel <= logLevelLimit )
			outExact( s );
	}
	
	public synchronized void log( String prompt , Throwable e , int logLevel ) throws Exception {
		if( logLevelLimit < 0 ) {
			System.out.println( "TRACEINTERNAL: " + prompt );
			e.printStackTrace();
			return;
		}
		
		if( logLevel > logLevelLimit ) {
			ExitException ee = Common.getExitException( e );
			if( ee != null ) {
				String s = prompt;
				if( !s.isEmpty() )
					s += " ";
				s += ee.getMessage();
				out( s );
				return;
			}
		}

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
	
	public void info( String s ) throws Exception {
		log( s , LOGLEVEL_INFO );
	}
	
	public void debug( String s ) throws Exception {
		log( s , LOGLEVEL_DEBUG );
	}
	
	public void trace( String s ) throws Exception {
		log( s , LOGLEVEL_TRACE );
	}
	
	private synchronized void outExact( String s ) throws Exception {
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
	
	private void out( String s ) throws Exception {
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
			
		throw new ExitException( errmsg );
	}
	
	public String getTimeStampedName( String basename , String ext ) throws Exception {
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
