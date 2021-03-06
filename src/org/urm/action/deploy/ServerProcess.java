package org.urm.action.deploy;

import org.urm.action.ActionBase;
import org.urm.common.Common;
import org.urm.engine.shell.Shell;
import org.urm.engine.shell.ShellExecutor;
import org.urm.engine.status.ScopeState;
import org.urm.engine.status.ScopeState.FACTVALUE;
import org.urm.meta.engine.HostAccount;
import org.urm.meta.env.MetaEnvServer;
import org.urm.meta.env.MetaEnvServerNode;
import org.urm.meta.loader.Types.*;

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
	
	public EnumProcessMode mode;
	public String pids;
	public String cmdValue;
	
	public static int defaultStartProcessTimeSecs = 10;
	public static int defaultStartServerTimeSecs = 60;
	public static int defaultStopServerTimeSecs = 60;
	
	public ServerProcess( MetaEnvServer srv , MetaEnvServerNode node , ScopeState state ) {
		this.srv = srv;
		this.node = node;
		this.state = state;
		this.mode = EnumProcessMode.UNKNOWN;
		this.pids = "ignore";
	}

	public boolean isGeneric( ActionBase action ) throws Exception {
		return( srv.isAccessGeneric() );
	}

	public boolean isService( ActionBase action ) throws Exception {
		return( srv.isAccessService() );
	}

	public boolean isDocker( ActionBase action ) throws Exception {
		return( srv.isAccessDocker() );
	}

	public boolean isPacemaker( ActionBase action ) throws Exception {
		return( srv.isAccessPacemaker() );
	}

	public boolean gatherStatus( ActionBase action ) throws Exception {
		HostAccount hostAccount = node.getHostAccount();
		action.debug( hostAccount.getFinalAccount() + ": check status srv=" + srv.NAME + " ..." );
		
		mode = EnumProcessMode.UNKNOWN;
		boolean res = false;
		if( isService( action ) )
			res = gatherServiceStatus( action );
		else
		if( isPacemaker( action ) )
			res = gatherPacemakerStatus( action );
		else
		if( isDocker( action ) )
			res = gatherDockerStatus( action );
		else
		if( isGeneric( action ) )
			res = gatherGenericStatus( action );
		else
			action.exitUnexpectedState();
		
		state.addFact( Facts.PROCESSSTATE , FACTVALUE.PROCESSMODE , mode.name() );
		return( res );
	}

	public boolean isStarted( ActionBase action ) throws Exception {
		HostAccount hostAccount = node.getHostAccount();
		if( mode == EnumProcessMode.UNKNOWN )
			action.exit1( _Error.UnknownHostState1 , "state is unknown for node=" + hostAccount.getFinalAccount() , hostAccount.getFinalAccount() );
		
		if( mode == EnumProcessMode.STARTED )
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
		HostAccount hostAccount = node.getHostAccount();
		try {
			ShellExecutor shell = action.getShell( node );
			return( shell );
		}
		catch( Throwable e ) {
			mode = EnumProcessMode.UNREACHABLE;
			action.error( hostAccount.getFinalAccount() + ": account is unreachable" );
			return( null );
		}
	}
	
	private boolean gatherPacemakerStatus( ActionBase action ) throws Exception {
		if( !srv.isLinux() )
			action.exitNotImplemented();
		
		ShellExecutor shell = getShell( action );
		if( shell == null )
			return( false );
		
		try {
			cmdValue = shell.customGetValue( action , "crm_resource -W -r " + srv.SYSNAME + " 2>&1 | grep `hostname`" , Shell.WAIT_DEFAULT );
			String check = cmdValue.toUpperCase();
			if( isStoppedStatus( action , check ) ) {
				mode = EnumProcessMode.STOPPED;
				return( true );
			}
			
			if( isStartedStatus( action , check ) ) {
				mode = EnumProcessMode.STARTED;
				return( true );
			}
			
			if( check.indexOf( "not found" ) >= 0 )
				action.error( "unknown pacemaker resource: " + srv.SYSNAME );
			
			mode = EnumProcessMode.ERRORS;
		}
		finally {
			shell.release( action );
		}
		
		return( true );
	}
	
	private boolean gatherDockerStatus( ActionBase action ) throws Exception {
		if( !srv.isLinux() )
			action.exitNotImplemented();
		
		ShellExecutor shell = getShell( action );
		if( shell == null )
			return( false );
		
		try {
			cmdValue = shell.customGetValue( action , "docker inspect " + srv.SYSNAME + " | grep Status" , Shell.WAIT_DEFAULT );
			cmdValue = cmdValue.trim();
			if( !cmdValue.startsWith( Common.getQuoted( "Status" ) + ":" ) ) {
				mode = EnumProcessMode.ERRORS;
				action.error( "unknown docker resource: " + srv.SYSNAME );
				return( true );
			}
			
			String check = Common.getListItem( cmdValue , ":" , 1 );
			check = Common.getListItem( check , "\"" , 1 );
			check = check.toUpperCase();
			
			if( isStoppedStatus( action , check ) ) {
				mode = EnumProcessMode.STOPPED;
				return( true );
			}
			
			if( isStartedStatus( action , check ) ) {
				mode = EnumProcessMode.STARTED;
				return( true );
			}
			
			if( check.indexOf( "not found" ) >= 0 )
				action.error( "unknown docker resource: " + srv.SYSNAME );
			
			mode = EnumProcessMode.ERRORS;
		}
		finally {
			shell.release( action );
		}
		
		return( true );
	}
	
	private boolean gatherServiceStatus( ActionBase action ) throws Exception {
		ShellExecutor shell = getShell( action );
		if( shell == null )
			return( false );
		
		// linux operations
		try {
			if( srv.isLinux() ) {
				cmdValue = shell.customGetValue( action , "service " + srv.SYSNAME + " status 2>&1" , Shell.WAIT_DEFAULT );
				
				String check = cmdValue.toUpperCase();
				if( isStoppedStatus( action , check ) ) {
					mode = EnumProcessMode.STOPPED;
					state.addFact( mode );
					return( true );
				}
				
				if( isStartedStatus( action , check ) ) {
					mode = EnumProcessMode.STARTED;
					state.addFact( mode );
					return( true );
				}
		
				if( isStartingStatus( action , check ) ) {
					mode = EnumProcessMode.STARTING;
					state.addFact( mode );
					return( true );
				}
				
				mode = EnumProcessMode.ERRORS;
				state.addFact( mode );
				return( true );
			}
			
			// windows operations
			if( srv.isWindows() ) {
				action.exitNotImplemented();
				return( false );
			}
			
			action.exitUnexpectedState();
		}
		finally {
			shell.release( action );
		}
		
		return( false );
	}

	private boolean gatherGenericStatus( ActionBase action ) throws Exception {
		mode = EnumProcessMode.UNKNOWN;
		
		if( !srv.NOPIDS ) {
			if( !getPids( action ) )
				return( false );
			
			if( pids.isEmpty() ) {
				if( mode == EnumProcessMode.UNKNOWN )
					mode = EnumProcessMode.STOPPED;
				return( true );
			}
		}

		// check process status
		ShellExecutor shell = getShell( action );
		if( shell == null )
			return( false );
		
		try {
			if( srv.isLinux() )
				cmdValue = shell.customGetValue( action , srv.getFullBinPath() , "./server.status.sh " + srv.NAME + " " + action.context.CTX_EXTRAARGS , Shell.WAIT_DEFAULT );
			else
			if( srv.isWindows() )
				cmdValue = shell.customGetValue( action , srv.getFullBinPath() , "call server.status.cmd " + srv.NAME + " " + action.context.CTX_EXTRAARGS , Shell.WAIT_DEFAULT );
			else
				action.exitUnexpectedState();
		}
		finally {
			shell.release( action );
		}

		String check = cmdValue.toUpperCase();
		if( isStartingStatus( action , check ) ) {
			mode = EnumProcessMode.STARTING;
			return( true );
		}
		
		if( srv.NOPIDS ) {
			if( isStoppedStatus( action , check  ) ) {
				mode = EnumProcessMode.STOPPED;
				return( true );
			}
		}
		
		if( isStartedStatus( action , check ) ) {
			mode = EnumProcessMode.STARTED;
			return( true );
		}
		
		mode = EnumProcessMode.ERRORS;
		return( true );
	}

	private boolean getPids( ActionBase action ) throws Exception {
		pids = "";
		
		// find program process
		ShellExecutor shell = getShell( action );
		if( shell == null )
			return( false );
		
		try {
			// linux operations
			if( srv.isLinux() ) {
				String value = shell.customGetValue( action , "pgrep -f \"Dprogram.name=" + srv.NAME + " \"" , Shell.WAIT_DEFAULT );
				if( !value.isEmpty() )
					pids = value.replace( '\n' ,  ' ' );
				return( true );
			}
			
			// windows operations
			if( srv.isWindows() ) {
				action.exitNotImplemented();
				return( false );
			}
			
			action.exitUnexpectedState();
		}
		finally {
			shell.release( action );
		}
		
		return( false );
	}

	public boolean stop( ActionBase action ) throws Exception {
		HostAccount hostAccount = node.getHostAccount();
		action.debug( hostAccount.getFinalAccount() + ": stop srv=" + srv.NAME + " ..." );
		
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
		if( !gatherStatus( action ) )
			return( false );

		HostAccount hostAccount = node.getHostAccount();
		if( mode == EnumProcessMode.STOPPED ) {
			action.debug( hostAccount.getFinalAccount() + ": pacemaker resource=" + srv.SYSNAME + " already stopped" );
			state.addFact( Facts.PROCESSACTION , FACTVALUE.PROCESSACTION , ProcessAction.ALREADYSTOPPED.name() );
			return( true );
		}

		ShellExecutor shell = action.getShell( node );
		try {
			shell.customCritical( action , "crm_resource -r " + srv.SYSNAME + " --host `hostname` --ban --quiet" , Shell.WAIT_DEFAULT );
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
		if( !gatherStatus( action ) )
			return( false );

		HostAccount hostAccount = node.getHostAccount();
		if( mode == EnumProcessMode.STOPPED ) {
			action.debug( hostAccount.getFinalAccount() + ": docker resource=" + srv.SYSNAME + " already stopped" );
			state.addFact( Facts.PROCESSACTION , FACTVALUE.PROCESSACTION , ProcessAction.ALREADYSTOPPED.name() );
			return( true );
		}

		ShellExecutor shell = action.getShell( node );
		try {
			shell.customCritical( action , "docker stop " + srv.SYSNAME , Shell.WAIT_DEFAULT );
			state.addFact( Facts.PROCESSACTION , FACTVALUE.PROCESSACTION , ProcessAction.PROCESSSTOP.name() );
			return( true );
		}
		finally {
			shell.release( action );
		}
	}
	
	private boolean stopService( ActionBase action ) throws Exception {
		// check status
		if( !gatherStatus( action ) )
			return( false );

		HostAccount hostAccount = node.getHostAccount();
		if( mode == EnumProcessMode.STOPPED ) {
			action.debug( hostAccount.getFinalAccount() + ": service=" + srv.SYSNAME + " already stopped" );
			state.addFact( Facts.PROCESSACTION , FACTVALUE.PROCESSACTION , ProcessAction.ALREADYSTOPPED.name() );
			return( true );
		}

		ShellExecutor shell = action.getShell( node );
		try {
			// linux operations
			if( srv.isLinux() ) {
				shell.customCritical( action , "service " + srv.SYSNAME + " stop > /dev/null 2>&1" , Shell.WAIT_DEFAULT );
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
		HostAccount hostAccount = node.getHostAccount();
		
		// check status
		if( srv.NOPIDS ) {
			if( !gatherStatus( action ) )
				return( false );
			
			if( mode == EnumProcessMode.STOPPED ) {
				action.debug( hostAccount.getFinalAccount() + ": server already stopped" );
				state.addFact( Facts.PROCESSACTION , FACTVALUE.PROCESSACTION , ProcessAction.ALREADYSTOPPED.name() );
				return( true );
			}
		}
		else {
			if( !getPids( action ) )
				return( false );
			
			if( pids.isEmpty() ) {
				action.debug( hostAccount.getFinalAccount() + ": server already stopped" );
				state.addFact( Facts.PROCESSACTION , FACTVALUE.PROCESSACTION , ProcessAction.ALREADYSTOPPED.name() );
				return( true );
			}
		}

		// stop kindly
		String F_FULLBINPATH = srv.getFullBinPath();
		ShellExecutor shell = action.getShell( node );
		try {
			// linux operations
			if( srv.isLinux() ) {
				shell.customCritical( action , F_FULLBINPATH , "./server.stop.sh " + srv.NAME + " " +
						Common.getQuoted( pids ) + " " + action.context.CTX_EXTRAARGS + " > /dev/null" , Shell.WAIT_DEFAULT );
				state.addFact( Facts.PROCESSACTION , FACTVALUE.PROCESSACTION , ProcessAction.PROCESSSTOP.name() );
				shell.checkErrors( action );
				return( true );
			}
			
			// windows operations
			if( srv.isWindows() ) {
				shell.customCritical( action , F_FULLBINPATH , "call server.stop.cmd " + srv.NAME + " " +
						Common.getQuoted( pids ) + " " + action.context.CTX_EXTRAARGS , Shell.WAIT_DEFAULT );
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
		HostAccount hostAccount = node.getHostAccount();
		action.debug( hostAccount.getFinalAccount() + ": wait stopped srv=" + srv.NAME + " ..." );
		
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
			
		HostAccount hostAccount = node.getHostAccount();
		action.debug( hostAccount.getFinalAccount() + ": wait for stop generic server=" + srv.NAME + " ..." );
	
		int stoptime = srv.STOPTIME;
		if( stoptime == 0 )
			stoptime = defaultStopServerTimeSecs;
		long stopMillis = startMillis + stoptime * 1000;
		
		mode = EnumProcessMode.UNKNOWN;
		if( !getPids( action ) )
			return( false );
		
		while( true ) {
			if( pids.isEmpty() )
				break;
			
		    synchronized( this ) {
		    	Thread.sleep( 1000 );
		    }
		    
			if( System.currentTimeMillis() > stopMillis ) {
				action.error( hostAccount.getFinalAccount() + ": failed to stop generic server=" + srv.NAME + " within " + stoptime + " seconds. Killing ..." );
				state.addFact( Facts.PROCESSACTION , FACTVALUE.PROCESSACTION , ProcessAction.UNABLETOSTOP.name() );
				
				// enforced stop
				killServer( action );
				getPids( action );
				
				if( pids.isEmpty() ) {
					action.info( hostAccount.getFinalAccount() + ": server successfully killed" );
					state.addFact( Facts.PROCESSACTION , FACTVALUE.PROCESSACTION , ProcessAction.KILLED.name() );
					return( true );
				}
				
				action.info( hostAccount.getFinalAccount() + ": generic server=" + srv.NAME + " - unable to kill" );
				state.addFact( Facts.PROCESSACTION , FACTVALUE.PROCESSACTION , ProcessAction.UNABLETOKILL.name() );
				return( false );
			}
			
			// check stopped
			getPids( action );
		}
	
		action.info( hostAccount.getFinalAccount() + ": generic server=" + srv.NAME + " successfully stopped" );
		state.addFact( Facts.PROCESSACTION , FACTVALUE.PROCESSACTION , ProcessAction.STOPPED.name() );
		return( true );
	}

	public boolean waitStoppedAny( ActionBase action , long startMillis , String title ) throws Exception {
		// wait for stop for a while
		HostAccount hostAccount = node.getHostAccount();
		action.debug( hostAccount.getFinalAccount() + ": wait for " + title + " ..." );
		
		int stoptime = srv.STOPTIME;
		if( stoptime == 0 )
			stoptime = defaultStopServerTimeSecs;
		long stopMillis = startMillis + stoptime * 1000;
		
		while( mode != EnumProcessMode.STOPPED ) {
			if( System.currentTimeMillis() > stopMillis ) {
				action.error( hostAccount.getFinalAccount() + ": failed to stop " + title + " within " + stoptime + " seconds" );
				state.addFact( Facts.PROCESSACTION , FACTVALUE.PROCESSACTION , ProcessAction.UNABLETOSTOP.name() );
				return( false );
			}
						
			// check stopped
			if( !gatherStatus( action ) )
				return( false );
			
		    synchronized( this ) {
		    	Thread.sleep( 1000 );
		    }
		}

		action.info( hostAccount.getFinalAccount() + " " + title + " successfully stopped" );
		state.addFact( Facts.PROCESSACTION , FACTVALUE.PROCESSACTION , ProcessAction.STOPPED.name() );
		return( true );
	}
	
	private void killServer( ActionBase action ) throws Exception {
		ShellExecutor shell = action.getShell( node );
		try {
			// linux operations
			if( srv.isLinux() ) {
				shell.customCritical( action , "kill -9 " + pids , Shell.WAIT_DEFAULT );
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
		HostAccount hostAccount = node.getHostAccount();
		action.debug( hostAccount.getFinalAccount() + ": start srv=" + srv.NAME + " ..." );
		
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
		if( !gatherStatus( action ) )
			return( false );

		HostAccount hostAccount = node.getHostAccount();
		if( mode == EnumProcessMode.STARTED ) {
			action.debug( hostAccount.getFinalAccount() + ": pacemaker resource=" + srv.SYSNAME + " already started" );
			state.addFact( Facts.PROCESSACTION , FACTVALUE.PROCESSACTION , ProcessAction.ALREADYSTARTED.name() );
			return( true );
		}

		if( mode != EnumProcessMode.STOPPED ) {
			action.error( hostAccount.getFinalAccount() + ": pacemaker resource=" + srv.SYSNAME + " is in unexpected state" );
			state.addFact( Facts.PROCESSACTION , FACTVALUE.PROCESSACTION , ProcessAction.UNABLETOSTART.name() );
			return( false );
		}

		ShellExecutor shell = action.getShell( node );
		try {
			shell.customCritical( action , "crm_resource -r " + srv.SYSNAME + " --host `hostname` --clear --quiet" , Shell.WAIT_DEFAULT );
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
		if( !gatherStatus( action ) )
			return( false );

		HostAccount hostAccount = node.getHostAccount();
		if( mode == EnumProcessMode.STARTED ) {
			action.debug( hostAccount.getFinalAccount() + ": docker resource=" + srv.SYSNAME + " already started" );
			state.addFact( Facts.PROCESSACTION , FACTVALUE.PROCESSACTION , ProcessAction.ALREADYSTARTED.name() );
			return( true );
		}

		if( mode != EnumProcessMode.STOPPED ) {
			action.error( hostAccount.getFinalAccount() + ": docker resource=" + srv.SYSNAME + " is in unexpected state" );
			state.addFact( Facts.PROCESSACTION , FACTVALUE.PROCESSACTION , ProcessAction.ALREADYSTARTED.name() );
			return( false );
		}

		ShellExecutor shell = action.getShell( node );
		try {
			shell.customCritical( action , "docker start " + srv.SYSNAME , Shell.WAIT_DEFAULT );
			state.addFact( Facts.PROCESSACTION , FACTVALUE.PROCESSACTION , ProcessAction.PROCESSSTART.name() );
			return( true );
		}
		finally {
			shell.release( action );
		}
	}
	
	private boolean startService( ActionBase action ) throws Exception {
		// check status
		if( !gatherStatus( action ) )
			return( false );

		HostAccount hostAccount = node.getHostAccount();
		if( mode == EnumProcessMode.STARTED ) {
			action.debug( hostAccount.getFinalAccount() + ": service=" + srv.SYSNAME + " already started" );
			state.addFact( Facts.PROCESSACTION , FACTVALUE.PROCESSACTION , ProcessAction.ALREADYSTARTED.name() );
			return( true );
		}

		if( mode != EnumProcessMode.STOPPED ) {
			action.error( hostAccount.getFinalAccount() + ": service=" + srv.SYSNAME + " is in unexpected state" );
			state.addFact( Facts.PROCESSACTION , FACTVALUE.PROCESSACTION , ProcessAction.UNABLETOSTART.name() );
			return( false );
		}

		ShellExecutor shell = action.getShell( node );
		try {
			// linux operations
			if( srv.isLinux() ) {
				shell.customCritical( action , "service " + srv.SYSNAME + " start > /dev/null 2>&1" , Shell.WAIT_DEFAULT );
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
		if( !gatherStatus( action ) )
			return( false );

		HostAccount hostAccount = node.getHostAccount();
		if( mode == EnumProcessMode.STARTED ) {
			action.debug( hostAccount.getFinalAccount() + ": server=" + srv.NAME + " already started (pids=" + pids + ")" );
			state.addFact( Facts.PROCESSACTION , FACTVALUE.PROCESSACTION , ProcessAction.ALREADYSTARTED.name() );
			return( true );
		}

		if( mode != EnumProcessMode.STOPPED ) {
			action.error( hostAccount.getFinalAccount() + ": server=" + srv.NAME + " is in unexpected state (pids=" + pids + ")" );
			state.addFact( Facts.PROCESSACTION , FACTVALUE.PROCESSACTION , ProcessAction.UNABLETOSTART.name() );
			return( false );
		}
		
		// proceed with startup
		String F_FULLBINPATH = srv.getFullBinPath();
		ShellExecutor shell = action.getShell( node );
		try {
			// linux operations
			if( srv.isLinux() ) {
				shell.customCritical( action , F_FULLBINPATH , "./server.start.sh " + srv.NAME + " " +
					action.context.CTX_EXTRAARGS + " > /dev/null" , Shell.WAIT_DEFAULT );
				shell.checkErrors( action );
				state.addFact( Facts.PROCESSACTION , FACTVALUE.PROCESSACTION , ProcessAction.PROCESSSTART.name() );
				return( true );
			}
			
			// windows operations
			if( srv.isWindows() ) {
				shell.customCritical( action , F_FULLBINPATH , "call server.start.cmd " + srv.NAME + " " +
					action.context.CTX_EXTRAARGS , Shell.WAIT_DEFAULT );
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
		HostAccount hostAccount = node.getHostAccount();
		action.debug( hostAccount.getFinalAccount() + ": wait started srv=" + srv.NAME + " ..." );
		
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
		HostAccount hostAccount = node.getHostAccount();
		action.debug( hostAccount.getFinalAccount() + ": wait for start " + title + " ..." );
		
		int starttime = srv.STARTTIME;
		if( starttime == 0 )
			starttime = defaultStartServerTimeSecs;
		long stopMillis = startMillis + starttime * 1000;
		long startTimeoutMillis = startMillis + defaultStartProcessTimeSecs * 1000;
				
		if( !gatherStatus( action ) )
			return( false );
		
		while( mode != EnumProcessMode.STARTED ) {
			Common.sleep( 1000 );
		    
			if( System.currentTimeMillis() > stopMillis ) {
				action.error( hostAccount.getFinalAccount() + ": failed to start " + title + " within " + starttime + " seconds" );
				state.addFact( Facts.PROCESSACTION , FACTVALUE.PROCESSACTION , ProcessAction.UNABLETOSTART.name() );
				return( false );
			}

			if( mode == EnumProcessMode.STOPPED && System.currentTimeMillis() > startTimeoutMillis ) {
				action.info( hostAccount.getFinalAccount() + ": failed to start " + title + " - process launch timeout is " + defaultStartProcessTimeSecs + " seconds" );
				state.addFact( Facts.PROCESSACTION , FACTVALUE.PROCESSACTION , ProcessAction.UNABLETOSTART.name() );
				return( false );
			}

			if( mode != EnumProcessMode.STOPPED && mode != EnumProcessMode.STARTING ) {
				action.info( hostAccount.getFinalAccount() + ": failed to start " + title + " - process is in unexpected state (" + cmdValue + ")" );
				state.addFact( Facts.PROCESSACTION , FACTVALUE.PROCESSACTION , ProcessAction.UNABLETOSTART.name() );
				return( false );
			}
			
			// check stopped
			if( !gatherStatus( action ) )
				return( false );
		}

		action.info( hostAccount.getFinalAccount() + " " + title + " successfully started" );
		return( true );
	}
	
	public boolean prepare( ActionBase action ) throws Exception {
		action.info( "prepare server ..." );
		
		boolean res = false;
		
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
				shell.customCritical( action , "service " + srv.SYSNAME + " prepare" , Shell.WAIT_LONG );
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
		String F_FULLBINPATH = srv.getFullBinPath();
		ShellExecutor shell = action.getShell( node );
		try {
			// linux operations
			if( srv.isLinux() ) {
				shell.customCritical( action , F_FULLBINPATH , "./server.prepare.sh " + srv.NAME + " " +
						srv.ROOTPATH + " " + action.context.CTX_EXTRAARGS + " > /dev/null" , Shell.WAIT_LONG );
				shell.checkErrors( action );
				return( true );
			}
			
			// windows operations
			if( srv.isWindows() ) {
				String wpath = Common.getWinPath( srv.ROOTPATH ); 
				shell.customCritical( action , F_FULLBINPATH , "call server.prepare.cmd " + srv.NAME + " " +
						wpath + " " + action.context.CTX_EXTRAARGS , Shell.WAIT_LONG );
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
