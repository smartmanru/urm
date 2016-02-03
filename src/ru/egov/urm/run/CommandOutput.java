package ru.egov.urm.run;

import java.io.PrintWriter;
import java.util.LinkedList;
import java.util.List;

import ru.egov.urm.Common;
import ru.egov.urm.ExitException;

public class CommandOutput {

	boolean debugOutput;
	boolean traceOutput;
	boolean traceInternal;
	PrintWriter outchild = null;
	PrintWriter outtee = null;

	List<PrintWriter> parentOutputs = new LinkedList<PrintWriter>();
	
	public CommandOutput() {
	}

	public void setContext( CommandContext context ) {
		this.traceInternal = context.CTX_TRACEINTERNAL;
		this.debugOutput = context.CTX_SHOWALL;
		this.traceOutput = context.CTX_TRACE;
	}
	
	public void log( String s ) throws Exception {
		out( s );
	}
	
	public void logExact( String s ) throws Exception {
		outExact( s );
	}
	
	public void log( String prompt , Throwable e ) throws Exception {
		if( !debugOutput ) {
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

		if( traceInternal ) {
			e.printStackTrace();
			return;
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
	
	public void debug( String s ) throws Exception {
		if( debugOutput )
			out( s );
	}
	
	public void trace( String s ) throws Exception {
		if( traceOutput )
			out( s );
	}
	
	public void trace( String s , boolean trace ) throws Exception {
		if( ( traceOutput && trace ) || trace == false )
			out( s );
	}

	private void outExact( String s ) throws Exception {
		if( traceInternal ) {
			System.out.println( "TRACEINTERNAL: outExact, line=" + s.replaceAll("\\p{C}", "?") );
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

	public void exit( String s ) throws Exception {
		String errmsg = "ERROR: " + s + ". Exiting";

		if( traceInternal ) {
			System.out.println( errmsg );
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
		out( title );
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
	}
}
