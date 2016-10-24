package org.urm.action.deploy;

import org.urm.action.ActionBase;
import org.urm.common.Common;
import org.urm.engine.shell.ShellExecutor;
import org.urm.meta.product.MetaEnvServer;
import org.urm.meta.product.MetaEnvServerNode;
import org.urm.meta.product.Meta.VarPROCESSMODE;

public class ServerProcess {

	MetaEnvServer srv;
	MetaEnvServerNode node;
	
	public VarPROCESSMODE mode;
	public String pids;
	public String cmdValue;
	
	public static int defaultStartProcessTimeSecs = 10;
	public static int defaultStartServerTimeSecs = 60;
	public static int defaultStopServerTimeSecs = 60;
	
	public ServerProcess( MetaEnvServer srv , MetaEnvServerNode node ) {
		this.srv = srv;
		this.node = node;
		this.mode = VarPROCESSMODE.UNKNOWN;
		this.pids = "ignore";
	}

	public boolean isGeneric( ActionBase action ) throws Exception {
		return( srv.isGeneric() );
	}

	public boolean isService( ActionBase action ) throws Exception {
		return( srv.isService() );
	}

	public boolean isPacemaker( ActionBase action ) throws Exception {
		return( srv.isPacemaker() );
	}

	public void gatherStatus( ActionBase action ) throws Exception {
		action.debug( node.HOSTLOGIN + ": check status srv=" + srv.NAME + " ..." );
		
		mode = VarPROCESSMODE.UNKNOWN;
		if( isService( action ) )
			gatherServiceStatus( action );
		else
		if( isPacemaker( action ) )
			gatherPacemakerStatus( action );
		else
		if( isGeneric( action ) )
			gatherGenericStatus( action );
		else
			action.exitUnexpectedState();
	}

	public boolean isStarted( ActionBase action ) throws Exception {
		if( mode == VarPROCESSMODE.UNKNOWN )
			action.exit1( _Error.UnknownHostState1 , "state is unknown for node=" + node.HOSTLOGIN , node.HOSTLOGIN );
		
		if( mode == VarPROCESSMODE.STARTED )
			return( true );
		
		return( false );
	}

	private boolean isStoppedStatus( ActionBase action , String check ) throws Exception {
		if( check.indexOf( "STARTED=FALSE" ) >= 0 || 
			check.indexOf( "STOPPED" ) >= 0 || 
			check.indexOf( "OFFLINE" ) >= 0 || 
			check.indexOf( "NOT RUNNING" ) >= 0 ||
			check.indexOf( "NOT STARTED" ) >= 0 )
			return( true );
		return( false );
	}
	
	private boolean isStartedStatus( ActionBase action , String check ) throws Exception {
		if( check.indexOf( "STARTED=TRUE" ) >= 0 || 
			check.indexOf( "ONLINE" ) >= 0 || 
			check.indexOf( "RUNNING" ) >= 0 ||
			check.indexOf( "STARTED" ) >= 0 )
			return( true );
		return( false );
	}

	private boolean isStartingStatus( ActionBase action , String check ) throws Exception {
		if( check.indexOf( "STARTING" ) >= 0 || 
			check.isEmpty() )
			return( true );
		return( false );
	}

	private void gatherPacemakerStatus( ActionBase action ) throws Exception {
		if( srv.isLinux() )
			action.exitNotImplemented();
		
		ShellExecutor shell = action.getShell( node );
		
		try {
			cmdValue = shell.customGetValue( action , "crm_resource -W -r " + srv.SYSNAME + " 2>&1 | grep `hostname`" );
			String check = cmdValue.toUpperCase();
			if( isStoppedStatus( action , check ) ) {
				mode = VarPROCESSMODE.STOPPED;
				return;
			}
			
			if( isStartedStatus( action , check ) ) {
				mode = VarPROCESSMODE.STARTED;
				return;
			}
			
			if( check.indexOf( "not found" ) >= 0 )
				action.error( "unknown pacemaker resource: " + srv.SYSNAME );
			
			mode = VarPROCESSMODE.ERRORS;
		}
		finally {
			shell.release( action );
		}
	}
	
	private void gatherServiceStatus( ActionBase action ) throws Exception {
		ShellExecutor shell = action.getShell( node );
		
		// linux operations
		try {
			if( srv.isLinux() ) {
				cmdValue = shell.customGetValue( action , "service " + srv.SYSNAME + " status 2>&1" );
				
				String check = cmdValue.toUpperCase();
				if( isStoppedStatus( action , check ) ) {
					mode = VarPROCESSMODE.STOPPED;
					return;
				}
				
				if( isStartedStatus( action , check ) ) {
					mode = VarPROCESSMODE.STARTED;
					return;
				}
		
				if( isStartingStatus( action , check ) ) {
					mode = VarPROCESSMODE.STARTING;
					return;
				}
				
				mode = VarPROCESSMODE.ERRORS;
				return;
			}
			
			// windows operations
			if( srv.isWindows() ) {
				action.exitNotImplemented();
				return;
			}
			
			action.exitUnexpectedState();
		}
		finally {
			shell.release( action );
		}
	}

	private void gatherGenericStatus( ActionBase action ) throws Exception {
		mode = VarPROCESSMODE.UNKNOWN;
		
		if( !srv.NOPIDS ) {
			getPids( action );
			
			if( pids.isEmpty() ) {
				mode = VarPROCESSMODE.STOPPED;
				return;
			}
		}

		// check process status
		ShellExecutor shell = action.getShell( node );
		try {
			if( srv.isLinux() )
				cmdValue = shell.customGetValue( action , srv.getFullBinPath( action ) , "./server.status.sh " + srv.NAME + " " + action.context.CTX_EXTRAARGS );
			else
			if( srv.isWindows() )
				cmdValue = shell.customGetValue( action , srv.getFullBinPath( action ) , "call server.status.cmd " + srv.NAME + " " + action.context.CTX_EXTRAARGS );
			else
				action.exitUnexpectedState();
		}
		finally {
			shell.release( action );
		}

		String check = cmdValue.toUpperCase();
		if( isStartingStatus( action , check ) ) {
			mode = VarPROCESSMODE.STARTING;
			return;
		}
		
		if( srv.NOPIDS ) {
			if( isStoppedStatus( action , check  ) ) {
				mode = VarPROCESSMODE.STOPPED;
				return;
			}
		}
		
		if( isStartedStatus( action , check ) ) {
			mode = VarPROCESSMODE.STARTED;
			return;
		}
		
		mode = VarPROCESSMODE.ERRORS;
	}

	private void getPids( ActionBase action ) throws Exception {
		pids = "";
		
		// find program process
		ShellExecutor shell = action.getShell( node );
		try {
			// linux operations
			if( srv.isLinux() ) {
				String value = shell.customGetValue( action , "pgrep -f \"Dprogram.name=" + srv.NAME + " \"" );
				if( !value.isEmpty() )
					pids = value.replace( '\n' ,  ' ' );
				return;
			}
			
			// windows operations
			if( srv.isWindows() ) {
				action.exitNotImplemented();
				return;
			}
			
			action.exitUnexpectedState();
		}
		finally {
			shell.release( action );
		}
	}

	public boolean stop( ActionBase action ) throws Exception {
		action.debug( node.HOSTLOGIN + ": stop srv=" + srv.NAME + " ..." );
		
		boolean res = false;
		if( isService( action ) )
			res = stopService( action );
		else
		if( isPacemaker( action ) )
			res = stopPacemaker( action );
		else
		if( isGeneric( action ) )
			res = stopGeneric( action );
		else
			action.exitUnexpectedState();
		return( res );
	}

	private boolean stopPacemaker( ActionBase action ) throws Exception {
		if( srv.isWindows() )
			action.exitNotImplemented();
			
		// check status
		gatherStatus( action );

		if( mode == VarPROCESSMODE.STOPPED ) {
			action.debug( node.HOSTLOGIN + ": pacemaker resource=" + srv.SYSNAME + " already stopped" );
			return( true );
		}

		ShellExecutor shell = action.getShell( node );
		try {
			shell.customCritical( action , "crm_resource -r " + srv.SYSNAME + " --host `hostname` --ban --quiet" );
			return( true );
		}
		finally {
			shell.release( action );
		}
	}
	
	private boolean stopService( ActionBase action ) throws Exception {
		// check status
		gatherStatus( action );

		if( mode == VarPROCESSMODE.STOPPED ) {
			action.debug( node.HOSTLOGIN + ": service=" + srv.SYSNAME + " already stopped" );
			return( true );
		}

		ShellExecutor shell = action.getShell( node );
		try {
			// linux operations
			if( srv.isLinux() ) {
				shell.customCritical( action , "service " + srv.SYSNAME + " stop > /dev/null 2>&1" );
				return( true );
			}
			
			// windows operations
			if( srv.isWindows() ) {
				action.exitNotImplemented();
				return( false );
			}
			
			action.exitUnexpectedState();
			return( false );
		}
		finally {
			shell.release( action );
		}
	}
	
	private boolean stopGeneric( ActionBase action ) throws Exception {
		// check status
		if( srv.NOPIDS ) {
			gatherStatus( action );
			if( mode == VarPROCESSMODE.STOPPED ) {
				action.debug( node.HOSTLOGIN + ": server already stopped" );
				return( true );
			}
		}
		else {
			getPids( action );
			if( pids.isEmpty() ) {
				action.debug( node.HOSTLOGIN + ": server already stopped" );
				return( true );
			}
		}

		// stop kindly
		String F_FULLBINPATH = srv.getFullBinPath( action );
		ShellExecutor shell = action.getShell( node );
		try {
			// linux operations
			if( srv.isLinux() ) {
				shell.customCritical( action , F_FULLBINPATH , "./server.stop.sh " + srv.NAME + " " +
						Common.getQuoted( pids ) + " " + action.context.CTX_EXTRAARGS + " > /dev/null" );
				shell.checkErrors( action );
				return( true );
			}
			
			// windows operations
			if( srv.isWindows() ) {
				shell.customCritical( action , F_FULLBINPATH , "call server.stop.cmd " + srv.NAME + " " +
						Common.getQuoted( pids ) + " " + action.context.CTX_EXTRAARGS );
				shell.checkErrors( action );
				return( true );
			}
			
			action.exitUnexpectedState();
			return( false );
		}
		finally {
			shell.release( action );
		}
	}

	public boolean waitStopped( ActionBase action , long startMillis ) throws Exception {
		action.debug( node.HOSTLOGIN + ": wait stopped srv=" + srv.NAME + " ..." );
		
		boolean res = false;
		if( isService( action ) )
			res = waitStoppedService( action , startMillis );
		else
		if( isPacemaker( action ) )
			res = waitStoppedPacemaker( action , startMillis );
		else
		if( isGeneric( action ) )
			res = waitStoppedGeneric( action , startMillis );
		else
			action.exitUnexpectedState();
		return( res );
	}

	public boolean waitStoppedPacemaker( ActionBase action , long startMillis ) throws Exception {
		// wait for stop for a while
		action.debug( node.HOSTLOGIN + ": wait for stop pacemaker resource=" + srv.SYSNAME + " ..." );
		
		int stoptime = srv.STOPTIME;
		if( stoptime == 0 )
			stoptime = defaultStopServerTimeSecs;
		long stopMillis = startMillis + stoptime * 1000;
		
		while( mode != VarPROCESSMODE.STOPPED ) {
			if( System.currentTimeMillis() > stopMillis ) {
				action.error( node.HOSTLOGIN + ": failed to stop pacemaker resource=" + srv.SYSNAME + " within " + stoptime + " seconds" );
				return( false );
			}
						
			// check stopped
			gatherStatus( action );
		    synchronized( this ) {
		    	Thread.sleep( 1000 );
		    }
		}

		action.info( node.HOSTLOGIN + " pacemaker resource=" + srv.SYSNAME + " successfully stopped" );
		return( true );
	}
	
	public boolean waitStoppedService( ActionBase action , long startMillis ) throws Exception {
		// wait for stop for a while
		action.debug( node.HOSTLOGIN + ": wait for stop service=" + srv.SYSNAME + " ..." );
		
		int stoptime = srv.STOPTIME;
		if( stoptime == 0 )
			stoptime = defaultStopServerTimeSecs;
		long stopMillis = startMillis + stoptime * 1000;
		
		while( mode != VarPROCESSMODE.STOPPED ) {
			if( System.currentTimeMillis() > stopMillis ) {
				action.error( node.HOSTLOGIN + ": failed to stop service=" + srv.SYSNAME + " within " + stoptime + " seconds" );
				return( false );
			}
						
			// check stopped
			gatherStatus( action );
		    synchronized( this ) {
		    	Thread.sleep( 1000 );
		    }
		}

		action.info( node.HOSTLOGIN + " service=" + srv.SYSNAME + " successfully stopped" );
		return( true );
	}
	
	public boolean waitStoppedGeneric( ActionBase action , long startMillis ) throws Exception {
		action.debug( node.HOSTLOGIN + ": wait for stop generic server=" + srv.NAME + " ..." );
	
		int stoptime = srv.STOPTIME;
		if( stoptime == 0 )
			stoptime = defaultStopServerTimeSecs;
		long stopMillis = startMillis + stoptime * 1000;
		
		if( srv.NOPIDS )
			gatherStatus( action );
		else {
			mode = VarPROCESSMODE.UNKNOWN;
			getPids( action );
		}
		
		while( true ) {
			if( srv.NOPIDS ) {
				if( mode == VarPROCESSMODE.STOPPED )
					break;
			}
			else {
				if( pids.isEmpty() )
					break;
			}
			
		    synchronized( this ) {
		    	Thread.sleep( 1000 );
		    }
		    
			if( System.currentTimeMillis() > stopMillis ) {
				if( srv.NOPIDS ) {
					action.error( node.HOSTLOGIN + ": failed to stop generic server=" + srv.NAME + " within " + stoptime + " seconds" );
					return( false );
				}
				
				action.error( node.HOSTLOGIN + ": failed to stop generic server=" + srv.NAME + " within " + stoptime + " seconds. Killing ..." );
				
				// enforced stop
				killServer( action );
				getPids( action );
				
				if( pids.isEmpty() ) {
					action.info( node.HOSTLOGIN + ": server successfully killed" );
					return( true );
				}
				
				action.info( node.HOSTLOGIN + ": generic server=" + srv.NAME + " - unable to kill" );
				return( false );
			}
			
			// check stopped
			if( srv.NOPIDS )
				gatherStatus( action );
			else
				getPids( action );
		}
	
		action.info( node.HOSTLOGIN + ": server successfully stopped" );
		return( true );
	}

	private void killServer( ActionBase action ) throws Exception {
		ShellExecutor shell = action.getShell( node );
		try {
			// linux operations
			if( srv.isLinux() ) {
				shell.customCritical( action , "kill -9 " + pids );
				return;
			}
			
			// windows operations
			if( srv.isWindows() ) {
				action.exitNotImplemented();
				return;
			}
			
			action.exitUnexpectedState();
			return;
		}
		finally {
			shell.release( action );
		}
	}
	
	public boolean start( ActionBase action ) throws Exception {
		action.debug( node.HOSTLOGIN + ": start srv=" + srv.NAME + " ..." );
		
		boolean res = false;
		if( isService( action ) )
			res = startService( action );
		else
		if( isPacemaker( action ) )
			res = startPacemaker( action );
		else
		if( isGeneric( action ) )
			res = startGeneric( action );
		else
			action.exitUnexpectedState();
		return( res );
	}

	private boolean startPacemaker( ActionBase action ) throws Exception {
		if( srv.isWindows() )
			action.exitNotImplemented();
			
		// check status
		gatherStatus( action );

		if( mode == VarPROCESSMODE.STARTED ) {
			action.debug( node.HOSTLOGIN + ": pacemaker resource=" + srv.SYSNAME + " already started" );
			return( true );
		}

		if( mode != VarPROCESSMODE.STOPPED ) {
			action.error( node.HOSTLOGIN + ": pacemaker resource=" + srv.SYSNAME + " is in unexpected state" );
			return( false );
		}

		ShellExecutor shell = action.getShell( node );
		try {
			shell.customCritical( action , "crm_resource -r " + srv.SYSNAME + " --host `hostname` --clear --quiet" );
			return( true );
		}
		finally {
			shell.release( action );
		}
	}
	
	private boolean startService( ActionBase action ) throws Exception {
		// check status
		gatherStatus( action );

		if( mode == VarPROCESSMODE.STARTED ) {
			action.debug( node.HOSTLOGIN + ": service=" + srv.SYSNAME + " already started" );
			return( true );
		}

		if( mode != VarPROCESSMODE.STOPPED ) {
			action.error( node.HOSTLOGIN + ": service=" + srv.SYSNAME + " is in unexpected state" );
			return( false );
		}

		ShellExecutor shell = action.getShell( node );
		try {
			// linux operations
			if( srv.isLinux() ) {
				shell.customCritical( action , "service " + srv.SYSNAME + " start > /dev/null 2>&1" );
				return( true );
			}
			
			// windows operations
			if( srv.isWindows() ) {
				action.exitNotImplemented();
				return( false );
			}
			
			action.exitUnexpectedState();
			return( false );
		}
		finally {
			shell.release( action );
		}
	}
	
	private boolean startGeneric( ActionBase action ) throws Exception {
		// check status
		gatherStatus( action );

		if( mode == VarPROCESSMODE.STARTED ) {
			action.debug( node.HOSTLOGIN + ": server=" + srv.NAME + " already started (pids=" + pids + ")" );
			return( true );
		}

		if( mode != VarPROCESSMODE.STOPPED ) {
			action.error( node.HOSTLOGIN + ": server=" + srv.NAME + " is in unexpected state (pids=" + pids + ")" );
			return( false );
		}
		
		// proceed with startup
		String F_FULLBINPATH = srv.getFullBinPath( action );
		ShellExecutor shell = action.getShell( node );
		try {
			// linux operations
			if( srv.isLinux() ) {
				shell.customCritical( action , F_FULLBINPATH , "./server.start.sh " + srv.NAME + " " +
					action.context.CTX_EXTRAARGS + " > /dev/null" );
				shell.checkErrors( action );
				return( true );
			}
			
			// windows operations
			if( srv.isWindows() ) {
				shell.customCritical( action , F_FULLBINPATH , "call server.start.cmd " + srv.NAME + " " +
					action.context.CTX_EXTRAARGS );
					shell.checkErrors( action );
				return( true );
			}
			
			action.exitUnexpectedState();
			return( false );
		}
		finally {
			shell.release( action );
		}
	}

	public boolean waitStarted( ActionBase action , long startMillis ) throws Exception {
		action.debug( node.HOSTLOGIN + ": wait started srv=" + srv.NAME + " ..." );
		
		boolean res = false;
		if( isService( action ) )
			res = waitStartedService( action , startMillis );
		else
		if( isPacemaker( action ) )
			res = waitStartedPacemaker( action , startMillis );
		else
		if( isGeneric( action ) )
			res = waitStartedGeneric( action , startMillis );
		else
			action.exitUnexpectedState();
		return( res );
	}

	public boolean waitStartedPacemaker( ActionBase action , long startMillis ) throws Exception {
		// wait for stop for a while
		action.debug( node.HOSTLOGIN + ": wait for start pacemaker resource=" + srv.SYSNAME + " ..." );
		
		int starttime = srv.STARTTIME;
		if( starttime == 0 )
			starttime = defaultStartServerTimeSecs;
		long stopMillis = startMillis + starttime * 1000;
		long startTimeoutMillis = startMillis + defaultStartProcessTimeSecs * 1000;
				
		gatherStatus( action );
		while( mode != VarPROCESSMODE.STARTED ) {
			Common.sleep( 1000 );
		    
			if( System.currentTimeMillis() > stopMillis ) {
				action.error( node.HOSTLOGIN + ": failed to start pacemaker resource=" + srv.SYSNAME + " within " + starttime + " seconds" );
				return( false );
			}

			if( mode == VarPROCESSMODE.STOPPED && System.currentTimeMillis() > startTimeoutMillis ) {
				action.info( node.HOSTLOGIN + ": failed to start pacemaker resource=" + srv.SYSNAME + " - process launch timeout is " + defaultStartProcessTimeSecs + " seconds" );
				return( false );
			}

			if( mode != VarPROCESSMODE.STOPPED && mode != VarPROCESSMODE.STARTING ) {
				action.info( node.HOSTLOGIN + ": failed to start pacemaker resource=" + srv.SYSNAME + " - process is in unexpected state (" + cmdValue + ")" );
				return( false );
			}
			
			// check stopped
			gatherStatus( action );
		}

		action.info( node.HOSTLOGIN + " pacemaker resource=" + srv.SYSNAME + " successfully started" );
		return( true );
	}
	
	public boolean waitStartedService( ActionBase action , long startMillis ) throws Exception {
		// wait for stop for a while
		action.debug( node.HOSTLOGIN + ": wait for start service=" + srv.SYSNAME + " ..." );
		
		int starttime = srv.STARTTIME;
		if( starttime == 0 )
			starttime = defaultStartServerTimeSecs;
		long stopMillis = startMillis + starttime * 1000;
		long startTimeoutMillis = startMillis + defaultStartProcessTimeSecs * 1000;
				
		gatherStatus( action );
		while( mode != VarPROCESSMODE.STARTED ) {
			Common.sleep( 1000 );
		    
			if( System.currentTimeMillis() > stopMillis ) {
				action.error( node.HOSTLOGIN + ": failed to start service=" + srv.SYSNAME + " within " + starttime + " seconds" );
				return( false );
			}

			if( mode == VarPROCESSMODE.STOPPED && System.currentTimeMillis() > startTimeoutMillis ) {
				action.info( node.HOSTLOGIN + ": failed to start service=" + srv.SYSNAME + " - process launch timeout is " + defaultStartProcessTimeSecs + " seconds" );
				return( false );
			}

			if( mode != VarPROCESSMODE.STOPPED && mode != VarPROCESSMODE.STARTING ) {
				action.info( node.HOSTLOGIN + ": failed to start service=" + srv.SYSNAME + " - process is in unexpected state (" + cmdValue + ")" );
				return( false );
			}
			
			// check stopped
			gatherStatus( action );
		}

		action.info( node.HOSTLOGIN + " service=" + srv.SYSNAME + " successfully started" );
		return( true );
	}
	
	public boolean waitStartedGeneric( ActionBase action , long startMillis ) throws Exception {
		// wait for stop for a while
		action.debug( node.HOSTLOGIN + ": wait for start generic server=" + srv.NAME + " ..." );
		
		int starttime = srv.STARTTIME;
		if( starttime == 0 )
			starttime = defaultStartServerTimeSecs;
		long stopMillis = startMillis + starttime * 1000;
		long startTimeoutMillis = startMillis + defaultStartProcessTimeSecs * 1000;
				
		gatherStatus( action );
		while( mode != VarPROCESSMODE.STARTED ) {
			Common.sleep( 1000 );
		    
			if( System.currentTimeMillis() > stopMillis ) {
				action.error( node.HOSTLOGIN + ": failed to start generic server=" + srv.NAME + " within " + starttime + " seconds" );
				return( false );
			}

			if( mode == VarPROCESSMODE.STOPPED && System.currentTimeMillis() > startTimeoutMillis ) {
				action.info( node.HOSTLOGIN + ": failed to start generic server=" + srv.NAME + " - process launch timeout is " + defaultStartProcessTimeSecs + " seconds" );
				return( false );
			}

			if( mode != VarPROCESSMODE.STOPPED && mode != VarPROCESSMODE.STARTING ) {
				action.info( node.HOSTLOGIN + ": failed to start generic server=" + srv.NAME + " - process is in unexpected state (" + cmdValue + ")" );
				return( false );
			}
			
			// check stopped
			gatherStatus( action );
		}

		action.info( node.HOSTLOGIN + " generic server=" + srv.NAME + " successfully started" );
		return( true );
	}
	
	public boolean prepare( ActionBase action ) throws Exception {
		action.info( "prepare server ..." );
		
		boolean res = false;
		int timeout = action.setTimeoutUnlimited();
		
		if( isService( action ) )
			res = prepareService( action );
		else
		if( isPacemaker( action ) )
			res = preparePacemaker( action );
		else
		if( isGeneric( action ) )
			res = prepareGeneric( action );
		else
			action.exitUnexpectedState();
		
		action.setTimeout( timeout );
		return( res );
	}

	private boolean preparePacemaker( ActionBase action ) throws Exception {
		action.exitNotImplemented();
		return( false );
	}
	
	private boolean prepareService( ActionBase action ) throws Exception {
		ShellExecutor shell = action.getShell( node );
		try {
			// linux operations
			if( srv.isLinux() ) {
				shell.customCritical( action , "service " + srv.SYSNAME + " prepare" );
				shell.checkErrors( action );
				return( true );
			}
			
			// windows operations
			if( srv.isWindows() ) {
				action.exitNotImplemented();
				return( false );
			}
			
			action.exitUnexpectedState();
			return( false );
		}
		finally {
			shell.release( action );
		}
	}
	
	private boolean prepareGeneric( ActionBase action ) throws Exception {
		// prepare instance
		String F_FULLBINPATH = srv.getFullBinPath( action );
		ShellExecutor shell = action.getShell( node );
		try {
			// linux operations
			if( srv.isLinux() ) {
				shell.customCritical( action , F_FULLBINPATH , "./server.prepare.sh " + srv.NAME + " " +
						srv.ROOTPATH + " " + action.context.CTX_EXTRAARGS + " > /dev/null" );
				shell.checkErrors( action );
				return( true );
			}
			
			// windows operations
			if( srv.isWindows() ) {
				String wpath = Common.getWinPath( srv.ROOTPATH ); 
				shell.customCritical( action , F_FULLBINPATH , "call server.prepare.cmd " + srv.NAME + " " +
						wpath + " " + action.context.CTX_EXTRAARGS );
				shell.checkErrors( action );
				return( true );
			}
			
			action.exitUnexpectedState();
			return( false );
		}
		finally {
			shell.release( action );
		}
	}

}
