package org.urm.action.deploy;

import org.urm.action.ActionBase;
import org.urm.common.Common;
import org.urm.engine.shell.ShellExecutor;
import org.urm.engine.status.ScopeState;
import org.urm.engine.status.ScopeState.FACTVALUE;
import org.urm.meta.product.MetaEnvServer;
import org.urm.meta.product.MetaEnvServerNode;
import org.urm.meta.Types.*;

public class ServerProcess {

	public enum Facts {
		PROCESSSTATE ,
		PROCESSACTION
	};
	
	public enum ProcessAction {
		UNKNOWN ,
		ALREADYSTARTED ,
		ALREADYSTOPPED ,
		PROCESSSTOP ,
		PROCESSKILL ,
		PROCESSSTART ,
		UNABLETOSTART ,
		UNABLETOSTOP ,
		KILLED ,
		UNABLETOKILL ,
		STARTED ,
		STOPPED
	};
	
	MetaEnvServer srv;
	MetaEnvServerNode node;
	ScopeState state;
	
	public VarPROCESSMODE mode;
	public String pids;
	public String cmdValue;
	
	public static int defaultStartProcessTimeSecs = 10;
	public static int defaultStartServerTimeSecs = 60;
	public static int defaultStopServerTimeSecs = 60;
	
	public ServerProcess( MetaEnvServer srv , MetaEnvServerNode node , ScopeState state ) {
		this.srv = srv;
		this.node = node;
		this.state = state;
		this.mode = VarPROCESSMODE.UNKNOWN;
		this.pids = "ignore";
	}

	public boolean isGeneric( ActionBase action ) throws Exception {
		return( srv.isGeneric() );
	}

	public boolean isService( ActionBase action ) throws Exception {
		return( srv.isService() );
	}

	public boolean isDocker( ActionBase action ) throws Exception {
		return( srv.isDocker() );
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
		if( isDocker( action ) )
			gatherDockerStatus( action );
		else
		if( isGeneric( action ) )
			gatherGenericStatus( action );
		else
			action.exitUnexpectedState();
		
		state.addFact( Facts.PROCESSSTATE , FACTVALUE.PROCESSMODE , mode.name() );
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

	private ShellExecutor getShell( ActionBase action ) throws Exception {
		try {
			ShellExecutor shell = action.getShell( node );
			return( shell );
		}
		catch( Throwable e ) {
			mode = VarPROCESSMODE.UNREACHABLE;
			return( null );
		}
	}
	
	private void gatherPacemakerStatus( ActionBase action ) throws Exception {
		if( !srv.isLinux() )
			action.exitNotImplemented();
		
		ShellExecutor shell = getShell( action );
		if( shell == null )
			return;
		
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
	
	private void gatherDockerStatus( ActionBase action ) throws Exception {
		if( !srv.isLinux() )
			action.exitNotImplemented();
		
		ShellExecutor shell = getShell( action );
		if( shell == null )
			return;
		
		try {
			cmdValue = shell.customGetValue( action , "docker inspect " + srv.SYSNAME + " | grep Status" );
			cmdValue = cmdValue.trim();
			if( !cmdValue.startsWith( Common.getQuoted( "Status" ) + ":" ) ) {
				mode = VarPROCESSMODE.ERRORS;
				action.error( "unknown docker resource: " + srv.SYSNAME );
				return;
			}
			
			String check = Common.getListItem( cmdValue , ":" , 1 );
			check = Common.getListItem( check , "\"" , 1 );
			check = check.toUpperCase();
			
			if( isStoppedStatus( action , check ) ) {
				mode = VarPROCESSMODE.STOPPED;
				return;
			}
			
			if( isStartedStatus( action , check ) ) {
				mode = VarPROCESSMODE.STARTED;
				return;
			}
			
			if( check.indexOf( "not found" ) >= 0 )
				action.error( "unknown docker resource: " + srv.SYSNAME );
			
			mode = VarPROCESSMODE.ERRORS;
		}
		finally {
			shell.release( action );
		}
	}
	
	private void gatherServiceStatus( ActionBase action ) throws Exception {
		ShellExecutor shell = getShell( action );
		if( shell == null )
			return;
		
		// linux operations
		try {
			if( srv.isLinux() ) {
				cmdValue = shell.customGetValue( action , "service " + srv.SYSNAME + " status 2>&1" );
				
				String check = cmdValue.toUpperCase();
				if( isStoppedStatus( action , check ) ) {
					mode = VarPROCESSMODE.STOPPED;
					state.addFact( mode );
					return;
				}
				
				if( isStartedStatus( action , check ) ) {
					mode = VarPROCESSMODE.STARTED;
					state.addFact( mode );
					return;
				}
		
				if( isStartingStatus( action , check ) ) {
					mode = VarPROCESSMODE.STARTING;
					state.addFact( mode );
					return;
				}
				
				mode = VarPROCESSMODE.ERRORS;
				state.addFact( mode );
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
				if( mode == VarPROCESSMODE.UNKNOWN )
					mode = VarPROCESSMODE.STOPPED;
				return;
			}
		}

		// check process status
		ShellExecutor shell = getShell( action );
		if( shell == null )
			return;
		
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
		ShellExecutor shell = getShell( action );
		if( shell == null )
			return;
		
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
		if( isDocker( action ) )
			res = stopDocker( action );
		else
		if( isGeneric( action ) )
			res = stopGeneric( action );
		else
			action.exitUnexpectedState();
		return( res );
	}

	private boolean stopPacemaker( ActionBase action ) throws Exception {
		if( !srv.isLinux() )
			action.exitNotImplemented();
			
		// check status
		gatherStatus( action );

		if( mode == VarPROCESSMODE.STOPPED ) {
			action.debug( node.HOSTLOGIN + ": pacemaker resource=" + srv.SYSNAME + " already stopped" );
			state.addFact( Facts.PROCESSACTION , FACTVALUE.PROCESSACTION , ProcessAction.ALREADYSTOPPED.name() );
			return( true );
		}

		ShellExecutor shell = action.getShell( node );
		try {
			shell.customCritical( action , "crm_resource -r " + srv.SYSNAME + " --host `hostname` --ban --quiet" );
			state.addFact( Facts.PROCESSACTION , FACTVALUE.PROCESSACTION , ProcessAction.PROCESSSTOP.name() );
			return( true );
		}
		finally {
			shell.release( action );
		}
	}
	
	private boolean stopDocker( ActionBase action ) throws Exception {
		if( !srv.isLinux() )
			action.exitNotImplemented();
			
		// check status
		gatherStatus( action );

		if( mode == VarPROCESSMODE.STOPPED ) {
			action.debug( node.HOSTLOGIN + ": docker resource=" + srv.SYSNAME + " already stopped" );
			state.addFact( Facts.PROCESSACTION , FACTVALUE.PROCESSACTION , ProcessAction.ALREADYSTOPPED.name() );
			return( true );
		}

		ShellExecutor shell = action.getShell( node );
		try {
			shell.customCritical( action , "docker stop " + srv.SYSNAME );
			state.addFact( Facts.PROCESSACTION , FACTVALUE.PROCESSACTION , ProcessAction.PROCESSSTOP.name() );
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
			state.addFact( Facts.PROCESSACTION , FACTVALUE.PROCESSACTION , ProcessAction.ALREADYSTOPPED.name() );
			return( true );
		}

		ShellExecutor shell = action.getShell( node );
		try {
			// linux operations
			if( srv.isLinux() ) {
				shell.customCritical( action , "service " + srv.SYSNAME + " stop > /dev/null 2>&1" );
				state.addFact( Facts.PROCESSACTION , FACTVALUE.PROCESSACTION , ProcessAction.PROCESSSTOP.name() );
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
				state.addFact( Facts.PROCESSACTION , FACTVALUE.PROCESSACTION , ProcessAction.ALREADYSTOPPED.name() );
				return( true );
			}
		}
		else {
			getPids( action );
			if( pids.isEmpty() ) {
				action.debug( node.HOSTLOGIN + ": server already stopped" );
				state.addFact( Facts.PROCESSACTION , FACTVALUE.PROCESSACTION , ProcessAction.ALREADYSTOPPED.name() );
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
				state.addFact( Facts.PROCESSACTION , FACTVALUE.PROCESSACTION , ProcessAction.PROCESSSTOP.name() );
				shell.checkErrors( action );
				return( true );
			}
			
			// windows operations
			if( srv.isWindows() ) {
				shell.customCritical( action , F_FULLBINPATH , "call server.stop.cmd " + srv.NAME + " " +
						Common.getQuoted( pids ) + " " + action.context.CTX_EXTRAARGS );
				state.addFact( Facts.PROCESSACTION , FACTVALUE.PROCESSACTION , ProcessAction.PROCESSSTOP.name() );
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
		if( isDocker( action ) )
			res = waitStoppedDocker( action , startMillis );
		else
		if( isGeneric( action ) )
			res = waitStoppedGeneric( action , startMillis );
		else
			action.exitUnexpectedState();
		return( res );
	}

	public boolean waitStoppedPacemaker( ActionBase action , long startMillis ) throws Exception {
		return( waitStoppedAny( action , startMillis , "pacemaker resource=" + srv.SYSNAME ) );
	}
	
	public boolean waitStoppedDocker( ActionBase action , long startMillis ) throws Exception {
		return( waitStoppedAny( action , startMillis , "docker resource=" + srv.SYSNAME ) );
	}
	
	public boolean waitStoppedService( ActionBase action , long startMillis ) throws Exception {
		return( waitStoppedAny( action , startMillis , "service=" + srv.SYSNAME ) );
	}
	
	public boolean waitStoppedGeneric( ActionBase action , long startMillis ) throws Exception {
		if( srv.NOPIDS )
			return( waitStoppedAny( action , startMillis , "generic server=" + srv.NAME ) );
			
		action.debug( node.HOSTLOGIN + ": wait for stop generic server=" + srv.NAME + " ..." );
	
		int stoptime = srv.STOPTIME;
		if( stoptime == 0 )
			stoptime = defaultStopServerTimeSecs;
		long stopMillis = startMillis + stoptime * 1000;
		
		mode = VarPROCESSMODE.UNKNOWN;
		getPids( action );
		
		while( true ) {
			if( pids.isEmpty() )
				break;
			
		    synchronized( this ) {
		    	Thread.sleep( 1000 );
		    }
		    
			if( System.currentTimeMillis() > stopMillis ) {
				action.error( node.HOSTLOGIN + ": failed to stop generic server=" + srv.NAME + " within " + stoptime + " seconds. Killing ..." );
				state.addFact( Facts.PROCESSACTION , FACTVALUE.PROCESSACTION , ProcessAction.UNABLETOSTOP.name() );
				
				// enforced stop
				killServer( action );
				getPids( action );
				
				if( pids.isEmpty() ) {
					action.info( node.HOSTLOGIN + ": server successfully killed" );
					state.addFact( Facts.PROCESSACTION , FACTVALUE.PROCESSACTION , ProcessAction.KILLED.name() );
					return( true );
				}
				
				action.info( node.HOSTLOGIN + ": generic server=" + srv.NAME + " - unable to kill" );
				state.addFact( Facts.PROCESSACTION , FACTVALUE.PROCESSACTION , ProcessAction.UNABLETOKILL.name() );
				return( false );
			}
			
			// check stopped
			getPids( action );
		}
	
		action.info( node.HOSTLOGIN + ": generic server=" + srv.NAME + " successfully stopped" );
		state.addFact( Facts.PROCESSACTION , FACTVALUE.PROCESSACTION , ProcessAction.STOPPED.name() );
		return( true );
	}

	public boolean waitStoppedAny( ActionBase action , long startMillis , String title ) throws Exception {
		// wait for stop for a while
		action.debug( node.HOSTLOGIN + ": wait for " + title + " ..." );
		
		int stoptime = srv.STOPTIME;
		if( stoptime == 0 )
			stoptime = defaultStopServerTimeSecs;
		long stopMillis = startMillis + stoptime * 1000;
		
		while( mode != VarPROCESSMODE.STOPPED ) {
			if( System.currentTimeMillis() > stopMillis ) {
				action.error( node.HOSTLOGIN + ": failed to stop " + title + " within " + stoptime + " seconds" );
				state.addFact( Facts.PROCESSACTION , FACTVALUE.PROCESSACTION , ProcessAction.UNABLETOSTOP.name() );
				return( false );
			}
						
			// check stopped
			gatherStatus( action );
		    synchronized( this ) {
		    	Thread.sleep( 1000 );
		    }
		}

		action.info( node.HOSTLOGIN + " " + title + " successfully stopped" );
		state.addFact( Facts.PROCESSACTION , FACTVALUE.PROCESSACTION , ProcessAction.STOPPED.name() );
		return( true );
	}
	
	private void killServer( ActionBase action ) throws Exception {
		ShellExecutor shell = action.getShell( node );
		try {
			// linux operations
			if( srv.isLinux() ) {
				shell.customCritical( action , "kill -9 " + pids );
				state.addFact( Facts.PROCESSACTION , FACTVALUE.PROCESSACTION , ProcessAction.PROCESSKILL.name() );
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
		if( isDocker( action ) )
			res = startDocker( action );
		else
		if( isGeneric( action ) )
			res = startGeneric( action );
		else
			action.exitUnexpectedState();
		return( res );
	}

	private boolean startPacemaker( ActionBase action ) throws Exception {
		if( !srv.isLinux() )
			action.exitNotImplemented();
			
		// check status
		gatherStatus( action );

		if( mode == VarPROCESSMODE.STARTED ) {
			action.debug( node.HOSTLOGIN + ": pacemaker resource=" + srv.SYSNAME + " already started" );
			state.addFact( Facts.PROCESSACTION , FACTVALUE.PROCESSACTION , ProcessAction.ALREADYSTARTED.name() );
			return( true );
		}

		if( mode != VarPROCESSMODE.STOPPED ) {
			action.error( node.HOSTLOGIN + ": pacemaker resource=" + srv.SYSNAME + " is in unexpected state" );
			state.addFact( Facts.PROCESSACTION , FACTVALUE.PROCESSACTION , ProcessAction.UNABLETOSTART.name() );
			return( false );
		}

		ShellExecutor shell = action.getShell( node );
		try {
			shell.customCritical( action , "crm_resource -r " + srv.SYSNAME + " --host `hostname` --clear --quiet" );
			state.addFact( Facts.PROCESSACTION , FACTVALUE.PROCESSACTION , ProcessAction.PROCESSSTART.name() );
			return( true );
		}
		finally {
			shell.release( action );
		}
	}
	
	private boolean startDocker( ActionBase action ) throws Exception {
		if( !srv.isLinux() )
			action.exitNotImplemented();
			
		// check status
		gatherStatus( action );

		if( mode == VarPROCESSMODE.STARTED ) {
			action.debug( node.HOSTLOGIN + ": docker resource=" + srv.SYSNAME + " already started" );
			state.addFact( Facts.PROCESSACTION , FACTVALUE.PROCESSACTION , ProcessAction.ALREADYSTARTED.name() );
			return( true );
		}

		if( mode != VarPROCESSMODE.STOPPED ) {
			action.error( node.HOSTLOGIN + ": docker resource=" + srv.SYSNAME + " is in unexpected state" );
			state.addFact( Facts.PROCESSACTION , FACTVALUE.PROCESSACTION , ProcessAction.ALREADYSTARTED.name() );
			return( false );
		}

		ShellExecutor shell = action.getShell( node );
		try {
			shell.customCritical( action , "docker start " + srv.SYSNAME );
			state.addFact( Facts.PROCESSACTION , FACTVALUE.PROCESSACTION , ProcessAction.PROCESSSTART.name() );
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
			state.addFact( Facts.PROCESSACTION , FACTVALUE.PROCESSACTION , ProcessAction.ALREADYSTARTED.name() );
			return( true );
		}

		if( mode != VarPROCESSMODE.STOPPED ) {
			action.error( node.HOSTLOGIN + ": service=" + srv.SYSNAME + " is in unexpected state" );
			state.addFact( Facts.PROCESSACTION , FACTVALUE.PROCESSACTION , ProcessAction.UNABLETOSTART.name() );
			return( false );
		}

		ShellExecutor shell = action.getShell( node );
		try {
			// linux operations
			if( srv.isLinux() ) {
				shell.customCritical( action , "service " + srv.SYSNAME + " start > /dev/null 2>&1" );
				state.addFact( Facts.PROCESSACTION , FACTVALUE.PROCESSACTION , ProcessAction.PROCESSSTART.name() );
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
			state.addFact( Facts.PROCESSACTION , FACTVALUE.PROCESSACTION , ProcessAction.ALREADYSTARTED.name() );
			return( true );
		}

		if( mode != VarPROCESSMODE.STOPPED ) {
			action.error( node.HOSTLOGIN + ": server=" + srv.NAME + " is in unexpected state (pids=" + pids + ")" );
			state.addFact( Facts.PROCESSACTION , FACTVALUE.PROCESSACTION , ProcessAction.UNABLETOSTART.name() );
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
				state.addFact( Facts.PROCESSACTION , FACTVALUE.PROCESSACTION , ProcessAction.PROCESSSTART.name() );
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
		if( isDocker( action ) )
			res = waitStartedDocker( action , startMillis );
		else
		if( isGeneric( action ) )
			res = waitStartedGeneric( action , startMillis );
		else
			action.exitUnexpectedState();
		return( res );
	}

	public boolean waitStartedPacemaker( ActionBase action , long startMillis ) throws Exception {
		return( waitStartedAny( action , startMillis , "pacemaker resource=" + srv.SYSNAME ) );
	}
	
	public boolean waitStartedDocker( ActionBase action , long startMillis ) throws Exception {
		return( waitStartedAny( action , startMillis , "docker resource=" + srv.SYSNAME ) );
	}
	
	public boolean waitStartedService( ActionBase action , long startMillis ) throws Exception {
		return( waitStartedAny( action , startMillis , "service=" + srv.SYSNAME ) );
	}
	
	public boolean waitStartedGeneric( ActionBase action , long startMillis ) throws Exception {
		return( waitStartedAny( action , startMillis , "generic server=" + srv.NAME ) );
	}

	public boolean waitStartedAny( ActionBase action , long startMillis , String title ) throws Exception {
		// wait for stop for a while
		action.debug( node.HOSTLOGIN + ": wait for start " + title + " ..." );
		
		int starttime = srv.STARTTIME;
		if( starttime == 0 )
			starttime = defaultStartServerTimeSecs;
		long stopMillis = startMillis + starttime * 1000;
		long startTimeoutMillis = startMillis + defaultStartProcessTimeSecs * 1000;
				
		gatherStatus( action );
		while( mode != VarPROCESSMODE.STARTED ) {
			Common.sleep( 1000 );
		    
			if( System.currentTimeMillis() > stopMillis ) {
				action.error( node.HOSTLOGIN + ": failed to start " + title + " within " + starttime + " seconds" );
				state.addFact( Facts.PROCESSACTION , FACTVALUE.PROCESSACTION , ProcessAction.UNABLETOSTART.name() );
				return( false );
			}

			if( mode == VarPROCESSMODE.STOPPED && System.currentTimeMillis() > startTimeoutMillis ) {
				action.info( node.HOSTLOGIN + ": failed to start " + title + " - process launch timeout is " + defaultStartProcessTimeSecs + " seconds" );
				state.addFact( Facts.PROCESSACTION , FACTVALUE.PROCESSACTION , ProcessAction.UNABLETOSTART.name() );
				return( false );
			}

			if( mode != VarPROCESSMODE.STOPPED && mode != VarPROCESSMODE.STARTING ) {
				action.info( node.HOSTLOGIN + ": failed to start " + title + " - process is in unexpected state (" + cmdValue + ")" );
				state.addFact( Facts.PROCESSACTION , FACTVALUE.PROCESSACTION , ProcessAction.UNABLETOSTART.name() );
				return( false );
			}
			
			// check stopped
			gatherStatus( action );
		}

		action.info( node.HOSTLOGIN + " " + title + " successfully started" );
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
		if( isDocker( action ) )
			res = prepareDocker( action );
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
	
	private boolean prepareDocker( ActionBase action ) throws Exception {
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
