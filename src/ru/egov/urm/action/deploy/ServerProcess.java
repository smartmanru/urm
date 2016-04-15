package ru.egov.urm.action.deploy;

import ru.egov.urm.Common;
import ru.egov.urm.action.ActionBase;
import ru.egov.urm.action.database.DatabaseProcess;
import ru.egov.urm.meta.MetaEnvServer;
import ru.egov.urm.meta.MetaEnvServerNode;
import ru.egov.urm.meta.Metadata.VarPROCESSMODE;
import ru.egov.urm.shell.ShellExecutor;

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
		return( srv.isGeneric( action ) );
	}

	public boolean isService( ActionBase action ) throws Exception {
		return( srv.isService( action ) );
	}

	public boolean isDatabase( ActionBase action ) throws Exception {
		return( srv.isDatabase( action ) );
	}
	
	public void gatherStatus( ActionBase action ) throws Exception {
		mode = VarPROCESSMODE.UNKNOWN;
		if( isService( action ) )
			gatherServiceStatus( action );
		else
		if( isGeneric( action ) )
			gatherGenericStatus( action );
		else
		if( isDatabase( action ) )
			gatherDatabaseStatus( action );
		else
			action.exitUnexpectedState();
	}

	public boolean isStarted( ActionBase action ) throws Exception {
		if( mode == VarPROCESSMODE.UNKNOWN )
			action.exit( "state is unknown for node=" + node.HOSTLOGIN );
		
		if( mode == VarPROCESSMODE.STARTED )
			return( true );
		
		return( false );
	}
	
	private void gatherServiceStatus( ActionBase action ) throws Exception {
		ShellExecutor shell = action.getShell( node );
		
		// linux operations
		if( srv.isLinux( action ) ) {
			cmdValue = shell.customGetValue( action , "service " + srv.SERVICENAME + " status 2>&1" );
			
			if( cmdValue.indexOf( "is stopped" ) >= 0 || cmdValue.indexOf( "is not running" ) >= 0 ) {
				mode = VarPROCESSMODE.STOPPED;
				return;
			}
			
			if( cmdValue.indexOf( "is running" ) >= 0 || cmdValue.indexOf( "is already running" ) >= 0 ) {
				mode = VarPROCESSMODE.STARTED;
				return;
			}
	
			if( cmdValue.indexOf( "is starting" ) >= 0 ) {
				mode = VarPROCESSMODE.STARTING;
				return;
			}
			
			mode = VarPROCESSMODE.ERRORS;
			return;
		}
		
		// windows operations
		if( srv.isWindows( action ) ) {
			action.exitNotImplemented();
			return;
		}
		
		action.exitUnexpectedState();
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
		
		if( srv.isLinux( action ) )
			cmdValue = shell.customGetValue( action , srv.getFullBinPath( action ) , "./server.status.sh " + srv.NAME + " " + action.context.CTX_EXTRAARGS );
		else
		if( srv.isWindows( action ) )
			cmdValue = shell.customGetValue( action , srv.getFullBinPath( action ) , "server.status.cmd " + srv.NAME + " " + action.context.CTX_EXTRAARGS );
		else
			action.exitUnexpectedState();

		if( cmdValue.indexOf( "Started=true" ) >= 0 || 
			cmdValue.indexOf( "RUNNING" ) >= 0 || 
			cmdValue.indexOf( "is running" ) >= 0 ) {
			mode = VarPROCESSMODE.STARTED;
			return;
		}
		
		if( cmdValue.isEmpty() ) {
			mode = VarPROCESSMODE.STARTING;
			return;
		}
		
		if( !srv.NOPIDS ) {
			if( cmdValue.indexOf( "Started=false" ) >= 0 || 
				cmdValue.indexOf( "STOPPED" ) >= 0 || 
				cmdValue.indexOf( "is not running" ) >= 0 || 
				cmdValue.indexOf( "is stopped" ) >= 0 ) {
				mode = VarPROCESSMODE.STOPPED;
				return;
			}
		}
		
		mode = VarPROCESSMODE.ERRORS;
	}

	private void gatherDatabaseStatus( ActionBase action ) throws Exception {
		DatabaseProcess process = new DatabaseProcess( node );
		mode = process.getStatus( action );
	}

	private void getPids( ActionBase action ) throws Exception {
		pids = "";
		
		// find program process
		ShellExecutor shell = action.getShell( node );

		// linux operations
		if( srv.isLinux( action ) ) {
			String value = shell.customGetValue( action , "pgrep -f \"Dprogram.name=" + srv.NAME + " \"" );
			if( !value.isEmpty() )
				pids = value.replace( '\n' ,  ' ' );
			return;
		}
		
		// windows operations
		if( srv.isWindows( action ) ) {
			action.exitNotImplemented();
			return;
		}
		
		action.exitUnexpectedState();
	}

	public boolean stop( ActionBase action ) throws Exception {
		boolean res = false;
		if( isService( action ) )
			res = stopService( action );
		else
		if( isGeneric( action ) )
			res = stopGeneric( action );
		else
			action.exitUnexpectedState();
		return( res );
	}

	private boolean stopService( ActionBase action ) throws Exception {
		// check status
		gatherStatus( action );

		if( mode == VarPROCESSMODE.STOPPED ) {
			action.debug( node.HOSTLOGIN + ": service=" + srv.SERVICENAME + " already stopped" );
			return( true );
		}

		ShellExecutor shell = action.getShell( node );

		// linux operations
		if( srv.isLinux( action ) ) {
			shell.customCritical( action , "service " + srv.SERVICENAME + " stop > /dev/null 2>&1" );
			return( true );
		}
		
		// windows operations
		if( srv.isWindows( action ) ) {
			action.exitNotImplemented();
			return( false );
		}
		
		action.exitUnexpectedState();
		return( false );
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
		
		// linux operations
		if( srv.isLinux( action ) ) {
			shell.customCritical( action , F_FULLBINPATH , "./server.stop.sh " + srv.NAME + " " +
					Common.getQuoted( pids ) + " " + action.context.CTX_EXTRAARGS + " > /dev/null" );
			shell.checkErrors( action );
			return( true );
		}
		
		// windows operations
		if( srv.isWindows( action ) ) {
			shell.customCritical( action , F_FULLBINPATH , "server.stop.cmd " + srv.NAME + " " +
					Common.getQuoted( pids ) + " " + action.context.CTX_EXTRAARGS );
			shell.checkErrors( action );
			return( false );
		}
		
		action.exitUnexpectedState();
		return( false );
	}

	public boolean waitStopped( ActionBase action , long startMillis ) throws Exception {
		boolean res = false;
		if( isService( action ) )
			res = waitStoppedService( action , startMillis );
		else
		if( isGeneric( action ) )
			res = waitStoppedGeneric( action , startMillis );
		else
			action.exitUnexpectedState();
		return( res );
	}

	public boolean waitStoppedService( ActionBase action , long startMillis ) throws Exception {
		// wait for stop for a while
		action.debug( node.HOSTLOGIN + ": wait for stop service=" + srv.SERVICENAME + " ..." );
		
		int stoptime = srv.STOPTIME;
		if( stoptime == 0 )
			stoptime = defaultStopServerTimeSecs;
		long stopMillis = startMillis + stoptime * 1000;
		
		while( mode != VarPROCESSMODE.STOPPED ) {
			if( System.currentTimeMillis() > stopMillis ) {
				action.log( node.HOSTLOGIN + ": failed to stop service=" + srv.SERVICENAME + " within " + stoptime + " seconds" );
				return( false );
			}
						
			// check stopped
			gatherStatus( action );
		    synchronized( this ) {
		    	Thread.sleep( 1000 );
		    }
		}

		action.log( node.HOSTLOGIN + " service=" + srv.SERVICENAME + " successfully stopped" );
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
				action.log( node.HOSTLOGIN + ": failed to stop generic server=" + srv.NAME + " within " + stoptime + " seconds. Killing ..." );
				
				if( srv.NOPIDS )
					return( false );
				
				// enforced stop
				killServer( action );
				getPids( action );
				
				if( pids.isEmpty() ) {
					action.log( node.HOSTLOGIN + ": server successfully killed" );
					return( true );
				}
				
				action.log( node.HOSTLOGIN + ": generic server=" + srv.NAME + " - unable to kill" );
				return( false );
			}
			
			// check stopped
			if( srv.NOPIDS )
				gatherStatus( action );
			else
				getPids( action );
		}
	
		action.log( node.HOSTLOGIN + ": server successfully stopped" );
		return( true );
	}

	private void killServer( ActionBase action ) throws Exception {
		ShellExecutor shell = action.getShell( node );
		
		// linux operations
		if( srv.isLinux( action ) ) {
			shell.customCritical( action , "kill -9 " + pids );
			return;
		}
		
		// windows operations
		if( srv.isWindows( action ) ) {
			action.exitNotImplemented();
			return;
		}
		
		action.exitUnexpectedState();
		return;
	}
	
	public boolean start( ActionBase action ) throws Exception {
		boolean res = false;
		if( isService( action ) )
			res = startService( action );
		else
		if( isGeneric( action ) )
			res = startGeneric( action );
		else
			action.exitUnexpectedState();
		return( res );
	}

	private boolean startService( ActionBase action ) throws Exception {
		// check status
		gatherStatus( action );

		if( mode == VarPROCESSMODE.STARTED ) {
			action.debug( node.HOSTLOGIN + ": service=" + srv.SERVICENAME + " already started" );
			return( true );
		}

		if( mode != VarPROCESSMODE.STOPPED ) {
			action.log( node.HOSTLOGIN + ": " + srv.SERVICENAME + " is in unexpected state" );
			return( false );
		}

		ShellExecutor shell = action.getShell( node );
		
		// linux operations
		if( srv.isLinux( action ) ) {
			shell.customCritical( action , "service " + srv.SERVICENAME + " start > /dev/null 2>&1" );
			return( true );
		}
		
		// windows operations
		if( srv.isWindows( action ) ) {
			action.exitNotImplemented();
			return( false );
		}
		
		action.exitUnexpectedState();
		return( false );
	}
	
	private boolean startGeneric( ActionBase action ) throws Exception {
		// check status
		gatherStatus( action );

		if( mode == VarPROCESSMODE.STARTED ) {
			action.debug( node.HOSTLOGIN + ": server=" + srv.NAME + " already started (pids=" + pids + ")" );
			return( true );
		}

		if( mode != VarPROCESSMODE.STOPPED ) {
			action.log( node.HOSTLOGIN + ": server=" + srv.NAME + " is in unexpected state (pids=" + pids + ")" );
			return( false );
		}
		
		// proceed with startup
		String F_FULLBINPATH = srv.getFullBinPath( action );
		ShellExecutor shell = action.getShell( node );

		// linux operations
		if( srv.isLinux( action ) ) {
			shell.customCritical( action , F_FULLBINPATH , "./server.start.sh " + srv.NAME + " " +
				action.context.CTX_EXTRAARGS + " > /dev/null" );
			shell.checkErrors( action );
			return( true );
		}
		
		// windows operations
		if( srv.isWindows( action ) ) {
			shell.customCritical( action , F_FULLBINPATH , "server.start.cmd " + srv.NAME + " " +
				action.context.CTX_EXTRAARGS );
				shell.checkErrors( action );
			return( false );
		}
		
		action.exitUnexpectedState();
		return( false );
	}

	public boolean waitStarted( ActionBase action , long startMillis ) throws Exception {
		boolean res = false;
		if( isService( action ) )
			res = waitStartedService( action , startMillis );
		else
		if( isGeneric( action ) )
			res = waitStartedGeneric( action , startMillis );
		else
			action.exitUnexpectedState();
		return( res );
	}

	public boolean waitStartedService( ActionBase action , long startMillis ) throws Exception {
		// wait for stop for a while
		action.debug( node.HOSTLOGIN + ": wait for start service=" + srv.SERVICENAME + " ..." );
		
		int starttime = srv.STARTTIME;
		if( starttime == 0 )
			starttime = defaultStartServerTimeSecs;
		long stopMillis = startMillis + starttime * 1000;
		long startTimeoutMillis = startMillis + defaultStartProcessTimeSecs * 1000;
				
		gatherStatus( action );
		while( mode != VarPROCESSMODE.STARTED ) {
			Common.sleep( action , 1000 );
		    
			if( System.currentTimeMillis() > stopMillis ) {
				action.log( node.HOSTLOGIN + ": failed to start service=" + srv.SERVICENAME + " within " + starttime + " seconds" );
				return( false );
			}

			if( mode == VarPROCESSMODE.STOPPED && System.currentTimeMillis() > startTimeoutMillis ) {
				action.log( node.HOSTLOGIN + ": failed to start service=" + srv.SERVICENAME + " - process launch timeout is " + defaultStartProcessTimeSecs + " seconds" );
				return( false );
			}

			if( mode != VarPROCESSMODE.STOPPED && mode != VarPROCESSMODE.STARTING ) {
				action.log( node.HOSTLOGIN + ": failed to start service=" + srv.SERVICENAME + " - process is in unexpected state (" + cmdValue + ")" );
				return( false );
			}
			
			// check stopped
			gatherStatus( action );
		}

		action.log( node.HOSTLOGIN + " service=" + srv.SERVICENAME + " successfully started" );
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
			Common.sleep( action , 1000 );
		    
			if( System.currentTimeMillis() > stopMillis ) {
				action.log( node.HOSTLOGIN + ": failed to start generic server=" + srv.NAME + " within " + starttime + " seconds" );
				return( false );
			}

			if( mode == VarPROCESSMODE.STOPPED && System.currentTimeMillis() > startTimeoutMillis ) {
				action.log( node.HOSTLOGIN + ": failed to start generic server=" + srv.NAME + " - process launch timeout is " + defaultStartProcessTimeSecs + " seconds" );
				return( false );
			}

			if( mode != VarPROCESSMODE.STOPPED && mode != VarPROCESSMODE.STARTING ) {
				action.log( node.HOSTLOGIN + ": failed to start generic server=" + srv.NAME + " - process is in unexpected state (" + cmdValue + ")" );
				return( false );
			}
			
			// check stopped
			gatherStatus( action );
		}

		action.log( node.HOSTLOGIN + " generic server=" + srv.NAME + " successfully started" );
		return( true );
	}
	
	public boolean prepare( ActionBase action ) throws Exception {
		boolean res = false;
		if( isService( action ) )
			res = prepareService( action );
		else
		if( isGeneric( action ) )
			res = prepareGeneric( action );
		else
			action.exitUnexpectedState();
		return( res );
	}

	private boolean prepareService( ActionBase action ) throws Exception {
		ShellExecutor shell = action.getShell( node );
		
		// linux operations
		if( srv.isLinux( action ) ) {
			shell.customCritical( action , "service " + srv.SERVICENAME + " prepare" );
			shell.checkErrors( action );
			return( true );
		}
		
		// windows operations
		if( srv.isWindows( action ) ) {
			action.exitNotImplemented();
			return( false );
		}
		
		action.exitUnexpectedState();
		return( false );
	}
	
	private boolean prepareGeneric( ActionBase action ) throws Exception {
		// prepare instance
		String F_FULLBINPATH = srv.getFullBinPath( action );
		ShellExecutor shell = action.getShell( node );
		
		// linux operations
		if( srv.isLinux( action ) ) {
			shell.customCritical( action , F_FULLBINPATH , "./server.prepare.sh " + srv.NAME + " " +
					srv.ROOTPATH + " " + action.context.CTX_EXTRAARGS + " > /dev/null" );
			shell.checkErrors( action );
			return( true );
		}
		
		// windows operations
		if( srv.isWindows( action ) ) {
			shell.customCritical( action , F_FULLBINPATH , "server.prepare.cmd " + srv.NAME + " " +
					srv.ROOTPATH + " " + action.context.CTX_EXTRAARGS );
			shell.checkErrors( action );
			return( false );
		}
		
		action.exitUnexpectedState();
		return( false );
	}

}
