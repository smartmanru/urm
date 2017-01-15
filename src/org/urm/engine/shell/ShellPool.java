package org.urm.engine.shell;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.urm.action.ActionBase;
import org.urm.engine.ServerEngine;
import org.urm.engine.storage.Folder;
import org.urm.meta.engine.ServerAuthResource;
import org.urm.meta.engine.ServerContext;

public class ShellPool {

	public ServerEngine engine;
	public String rootPath;
	
	Map<String,Object> staged = new HashMap<String,Object>();
	Map<String,ShellExecutor> pool = new HashMap<String,ShellExecutor>();
	Map<ActionBase,ActionShells> actionSessions = new HashMap<ActionBase,ActionShells>();
	List<ShellExecutor> pending = new LinkedList<ShellExecutor>();

	private ShellCoreJNI osapi = null;
	
	public ShellExecutor master;
	public Account masterAccount;
	public Folder tmpFolder;
	
	private boolean started = false;
	private boolean stop = false;
	private boolean stopped = false;
	
	private long tsHouseKeepTime = 0;
	private static long DEFAULT_SHELL_SILENT_MAX = 60000;
	private static long DEFAULT_SHELL_UNAVAILABLE_SKIPTIME = 30000;
	private static long DEFAULT_SHELL_HOUSEKEEP_TIME = 30000;
	
	private long SHELL_SILENT_MAX;
	private long SHELL_UNAVAILABLE_SKIPTIME;
	private long SHELL_HOUSEKEEP_TIME;
	
	public int shellIndex;
	
	public class PoolCounts {
		public int poolSize;
		public int pendingSize;
		public int activeExecuteLocalCount;
		public int activeExecuteRemoteCount;
		public int activeInteractiveCount;
	}
	
	public ShellPool( ServerEngine engine ) {
		this.engine = engine;
		rootPath = engine.execrc.userHome;
		masterAccount = new Account( engine.execrc );
		shellIndex = 0;
	}
	
	public PoolCounts getPoolInfo() {
		PoolCounts c = new PoolCounts();
		synchronized( engine ) {
			c.poolSize = pool.size(); 
			c.pendingSize = pending.size();
			c.activeExecuteLocalCount = 0;
			c.activeExecuteRemoteCount = 0;
			c.activeInteractiveCount = 0;
			for( ActionShells set : actionSessions.values() ) {
				for( ShellExecutor executor : set.executors.values() ) {
					if( executor.account.local )
						c.activeExecuteLocalCount++;
					else
						c.activeExecuteRemoteCount++;
				}
				c.activeInteractiveCount += set.interactive.size();
			}
		}
		return( c );
	}
	
	public int getActiveShells() {
		synchronized( engine ) {
			return( pool.size() );
		}
	}
	
	public void runHouseKeeping( long time ) throws Exception {
		if( tsHouseKeepTime > 0 && ( time - tsHouseKeepTime ) < SHELL_HOUSEKEEP_TIME )
			return;
		
		tsHouseKeepTime = time;
		engine.trace( "run thread pool house keeping ..." );
		
		// move pending to primary
		synchronized( engine ) {
			for( int k = 0; k < pending.size(); ) {
				ShellExecutor shell = pending.get( k );
				if( !pool.containsKey( shell.name ) ) {
					pool.put( shell.name , shell );
					pending.remove( k );
					engine.trace( "return action session to pool name=" + shell.name );
				}
				else
				if( checkOldExecutorShell( shell ) ) {
					engine.trace( "kill old pending executor shell name=" + shell.name + " ..." );
					pending.remove( k );
					killShell( shell );
				}
				else {
					k++;
					engine.trace( "stay in pending name=" + shell.name );
				}
			}
		}
		
		// kill old primary 
		synchronized( engine ) {
			for( ShellExecutor shell : pool.values().toArray( new ShellExecutor[0] ) ) {
				if( checkOldExecutorShell( shell ) ) {
					engine.trace( "kill old pool executor shell name=" + shell.name + " ..." );
					pool.remove( shell.name );
					killShell( shell );
				}
				else
					engine.trace( "stay in pool name=" + shell.name );
			}
		}
		
		// kill silent action interactive shells 
		synchronized( engine ) {
			for( ActionShells map : actionSessions.values() ) {
				for( ShellInteractive shell : map.getInteractiveList() ) {
					if( checkOldInteractiveShell( shell ) ) {
						engine.trace( "kill old interactive shell name=" + shell.name + " ..." );
						map.removeInteractive( shell.name );
						killShell( shell );
					}
					else
						engine.trace( "keep interactive name=" + shell.name );
				}
			}
		}
	}

	private boolean checkOldExecutorShell( ShellExecutor shell ) {
		// check unavailable shell
		if( !shell.available ) {
			if( tsHouseKeepTime - shell.tsCreated > SHELL_UNAVAILABLE_SKIPTIME )
				return( true );
			return( false );
		}
		
		// check silent shell
		long finished = shell.tsLastFinished;
		if( finished > 0 && finished + SHELL_SILENT_MAX < tsHouseKeepTime )
			return( true );
		return( false );
	}
	
	private boolean checkOldInteractiveShell( ShellInteractive shell ) {
		long last = shell.tsLastInput;
		if( last == 0 || shell.tsLastOutput > last )
			last = shell.tsLastOutput;
		
		if( last > 0 && last + SHELL_SILENT_MAX < tsHouseKeepTime )
			return( true );
		return( false );
	}
	
	public void start( ActionBase action ) throws Exception {
		tmpFolder = action.artefactory.getTmpFolder( action );
		action.debug( "start shell pool (tmp folder=" + tmpFolder.folderPath + ") ..." );
		
		master = createDedicatedLocalShell( action , "master" );
		tmpFolder.recreateThis( action );

		// set parameters
		SHELL_SILENT_MAX = DEFAULT_SHELL_SILENT_MAX;
		SHELL_UNAVAILABLE_SKIPTIME = DEFAULT_SHELL_UNAVAILABLE_SKIPTIME;
		SHELL_HOUSEKEEP_TIME = DEFAULT_SHELL_HOUSEKEEP_TIME;
		if( !action.isStandalone() ) {
			ServerContext context = action.getServerSettings().serverContext;
			SHELL_SILENT_MAX = context.SHELL_SILENTMAX;
			SHELL_UNAVAILABLE_SKIPTIME = context.SHELL_UNAVAILABLE_SKIPTIME;
			SHELL_HOUSEKEEP_TIME = context.SHELL_HOUSEKEEP_TIME;
		}
		
		// start managing thread
		started = true;
		action.debug( "shell pool has been started" );
	}
	
	public void stop( ActionBase action ) {
		action.debug( "stop shell pool ..." );
		try {
			if( started ) {
				stop = true;

				while( true ) {
					synchronized( this ) {
						if( !stopped )
							wait();
						if( stopped )
							break;
					}
				}
			}
		}
		catch( Throwable e ) {
			if( action.context.CTX_TRACEINTERNAL )
				action.trace( "exception when killing shell=" + master.name + " (" + e.getMessage() + ")" );
		}
		action.debug( "shell pool has been stopped" );
	}
	
	private void killShell( Shell shell ) {
		try {
			shell.kill( engine.serverAction );
		}
		catch( Throwable e ) {
			if( engine.serverAction.context.CTX_TRACEINTERNAL )
				engine.trace( "exception when killing shell=" + shell.name + " (" + e.getMessage() + ")" );
		}
	}
	
	public void killAll( ActionBase serverAction ) {
		for( ActionBase actionAffected : actionSessions.keySet() )
			releaseActionPool( actionAffected );

		for( ShellExecutor shell : pending )
			killShell( shell );
		
		for( ShellExecutor shell : pool.values().toArray( new ShellExecutor[0] ) )
			killShell( shell );
		
		killShell( master );
		
		synchronized( this ) {
			notifyAll();
		}
	}

	public ShellExecutor getExecutor( ActionBase action , Account account , String scope ) throws Exception {
		if( stop )
			action.exit0( _Error.ServerShutdown0 , "server is in progress of shutdown" );
		
		String name = ( account.local )? "local::" + scope : "remote::" + scope + "::" + account.getPrintName(); 

		// get sync object
		Object sync = null;
		synchronized( engine ) {
			sync = staged.get( name );
			if( sync == null ) {
				sync = new Object();
				staged.put( name , sync );
			}
		}
		
		ShellExecutor shell = null;
		synchronized( sync ) {
			// owned by action
			ActionShells map;
			synchronized( this ) {
				map = getActionShells( action );
				shell = map.getExecutor( name );
				if( shell != null )
					return( shell );
			}

			// from free pool
			int id = 0;
			synchronized( engine ) {
				shell = pool.get( name );
				if( shell != null ) {
					if( !shell.available )
						action.exit1( _Error.NotConnectUnavailableShell0 , "do not connect to unavailable shell name=" + name , name );
						
					pool.remove( name );
					map.addExecutor( name , shell );
					engine.trace( "assign actionId=" + action.ID + " to existing session name=" + name );
					return( shell );
				}
				
				id = ++shellIndex;
			}

			// create new shell
			if( account.local )
				shell = createLocalShell( action , id , name , false );
			else
				shell = createRemoteShell( action , id , name , account , null , false );

			// start shell
			if( !shell.start( action ) ) {
				// add shell to pool to avoid many connects to unavailable resource
				shell.setUnavailable();
				pool.put( shell.name , shell );
				String accName = account.getPrintName();
				action.exit1( _Error.UnableConnectHost1 , "Unable to open shell connection account=" + accName , accName );
			}
			
			// add to action sessions (return to pool after release)
			synchronized( engine ) {
				map.addExecutor( name , shell );
				engine.trace( "assign actionId=" + action.ID + " to new session name=" + name );
			}

			// force create temporary folder on remote location
			if( !account.local )
				shell.tmpFolder.ensureExists( action );
		}
		
		return( shell );
	}

	private ShellExecutor startDedicatedLocalShell( ActionBase action , int id , String name ) throws Exception {
		ShellExecutor shell = createLocalShell( action , id , name , true );
		
		action.setShell( shell );
		if( !shell.start( action ) )
			action.exit0( _Error.UnableCreateLocalShell0 , "unable to create local shell" );
		
		return( shell );
	}

	private ShellExecutor startDedicatedRemoteShell( ActionBase action , int id , String name , Account account , ServerAuthResource auth ) throws Exception {
		ShellExecutor shell = createRemoteShell( action , id , name , account , auth , true );
		
		action.setShell( shell );
		if( !shell.start( action ) )
			action.exit0( _Error.UnableCreateRemoteShell0 , "unable to create remote shell" );
		
		return( shell );
	}

	private ShellExecutor createLocalShell( ActionBase action , int id , String name , boolean dedicated ) throws Exception {
		if( stop )
			action.exit0( _Error.ServerShutdown0 , "server is in progress of shutdown" );
		
		ShellExecutor shell = ShellExecutor.getLocalShellExecutor( action , id , name , this , rootPath , tmpFolder , dedicated );
		return( shell );
	}
	
	private ShellExecutor createRemoteShell( ActionBase action , int id , String name , Account account , ServerAuthResource auth , boolean dedicated ) throws Exception {
		if( stop )
			action.exit0( _Error.ServerShutdown0 , "server is in progress of shutdown" );
		
		ShellExecutor shell = ShellExecutor.getRemoteShellExecutor( action , id , name , this , account , auth , dedicated );
		return( shell );
	}
	
	private ActionShells getActionShells( ActionBase action ) {
		ActionShells map = actionSessions.get( action );
		if( map == null ) {
			map = new ActionShells( action );
			actionSessions.put( action , map );
			engine.trace( "register in session pool actionId=" + action.ID );
		}
		return( map );
	}
	
	public ShellExecutor createDedicatedLocalShell( ActionBase action , String stream ) throws Exception {
		if( stop )
			action.exit0( _Error.ServerShutdown0 , "server is in progress of shutdown" );
		
		String name = "local::" + stream; 
		if( stream.equals( "master" ) )
			return( startDedicatedLocalShell( action , 0 , name ) );
		
		ShellExecutor shell = null;
		synchronized( engine ) {
			ActionShells map = getActionShells( action );
			shell = map.getExecutor( name );
			if( shell != null ) {
				action.setShell( shell );
				return( shell );
			}
			
			int id = ++shellIndex;
			name += ":" + id;
			shell = startDedicatedLocalShell( action , id , name );
			map.addExecutor( shell.name , shell );
		}
		
		return( shell );
	}

	public ShellExecutor createDedicatedRemoteShell( ActionBase action , String stream , Account account , ServerAuthResource authResource ) throws Exception {
		if( stop )
			action.exit0( _Error.ServerShutdown0 , "server is in progress of shutdown" );
		
		String name = "remote::" + stream; 
		ShellExecutor shell = null;
		synchronized( engine ) {
			ActionShells map = getActionShells( action );
			int id = ++shellIndex;
			name += ":" + id;
			shell = startDedicatedRemoteShell( action , id , name , account , authResource );
			map.addExecutor( shell.name , shell );
		}
		
		return( shell );
	}

	public void releaseExecutorShell( ActionBase action , ShellExecutor shell ) {
		ActionShells map;
		synchronized( this ) {
			map = actionSessions.get( action );
			if( map == null )
				return;
		}
		
		releaseExecutorShell( action , shell , map );
	}
	
	private void releaseExecutorShell( ActionBase action , ShellExecutor shell , ActionShells map ) {
		// put remote sessions to pool or to pending list, kill locals
		synchronized( engine ) {
			if( !shell.dedicated ) {
				if( pool.get( shell.name ) == null ) {
					pool.put( shell.name , shell );
					engine.trace( "return session to pool name=" + shell.name );
				}
				else {
					pending.add( shell );
					engine.trace( "put session to pending name=" + shell.name );
				}
				map.removeExecutor( shell.name );
			}
			else {
				killShell( shell );
				map.removeExecutor( shell.name );
			}
		}
	}

	private void releaseInteractiveShell( ActionBase action , ShellInteractive shell , ActionShells map ) {
		// put remote sessions to pool or to pending list, kill locals
		synchronized( engine ) {
			killShell( shell );
			map.removeInteractive( shell.name );
		}
	}

	public void removeInteractive( ActionBase action , ShellInteractive shell ) {
		synchronized( engine ) {
			ActionShells map = actionSessions.get( action );
			if( map == null )
				return;
			
			engine.trace( "unregister in session pool actionId=" + action.ID + ", shell=" + shell.name );
			map.removeInteractive( shell.name );
		}
	}
		
	public void releaseActionPool( ActionBase action ) {
		ActionShells map;
		ShellExecutor[] executors;
		ShellInteractive[] interactive;
		synchronized( this ) {
			map = actionSessions.get( action );
			if( map == null )
				return;
			
			executors = map.getExecutorList();
			interactive = map.getInteractiveList();
		}
			
		for( int k = executors.length - 1; k >= 0; k-- ) {
			ShellExecutor shell = executors[ k ]; 
			releaseExecutorShell( action , shell , map );
		}
		
		for( int k = interactive.length - 1; k >= 0; k-- ) {
			ShellInteractive shell = interactive[ k ]; 
			releaseInteractiveShell( action , shell , map );
		}
		
		synchronized( engine ) {
			engine.trace( "unregister in session pool actionId=" + action.ID );
			actionSessions.remove( action );
		}
	}

	public ShellInteractive createInteractiveShell( ActionBase action , Account account , ServerAuthResource auth ) throws Exception {
		int id = 0;
		String name = "remote::" + account.getPrintName() + "::" + action.ID;
		synchronized( engine ) {
			id = ++shellIndex;
		}
		
		ShellInteractive shell = ShellInteractive.getShell( action , id , name , this , account , auth );
		
		// add to action map
		synchronized( engine ) {
			ActionShells map = getActionShells( action );
			map.addInteractive( shell );
		}
		
		return( shell );
	}
	
	public synchronized ShellCoreJNI getOSAPI() throws Exception {
		if( osapi == null )
			osapi = new ShellCoreJNI();
		return( osapi );
	}

}
