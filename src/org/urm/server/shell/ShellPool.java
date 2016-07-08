package org.urm.server.shell;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.urm.common.Common;
import org.urm.server.ServerEngine;
import org.urm.server.action.ActionBase;
import org.urm.server.storage.Folder;

public class ShellPool implements Runnable {

	public ServerEngine engine;
	public String rootPath;
	
	Map<String,Object> staged = new HashMap<String,Object>();
	Map<String,ShellExecutor> pool = new HashMap<String,ShellExecutor>();
	Map<ActionBase,ActionShells> actionSessions = new HashMap<ActionBase,ActionShells>();
	List<ShellExecutor> pending = new LinkedList<ShellExecutor>();

	public ShellExecutor master;
	public Account account;
	public Folder tmpFolder;
	
	private Thread thread;
	private boolean started = false;
	private boolean stop = false;
	
	private long tsHouseKeepTime = 0;
	private static long SHELL_SILENT_MAX = 60000;
	
	public ShellPool( ServerEngine engine ) {
		this.engine = engine;
		rootPath = engine.execrc.userHome;
		account = new Account( engine.execrc );
	}
	
	@Override
	public void run() {
		while( !stop ) {
			try {
				Common.sleep( this , 30000 );
				runHouseKeeping();
			}
			catch( InterruptedException e ) {
			}
			catch( Throwable e ) {
				engine.serverAction.log( "thread pool house keeping error" , e );
			}
		}
	}

	private void runHouseKeeping() throws Exception {
		engine.serverAction.trace( "run thread pool house keeping ..." );
		
		// move pending to primary
		tsHouseKeepTime = System.currentTimeMillis();
		synchronized( this ) {
			for( int k = 0; k < pending.size(); ) {
				ShellExecutor shell = pending.get( k );
				if( !pool.containsKey( shell.name ) ) {
					pool.put( shell.name , shell );
					pending.remove( k );
					engine.serverAction.trace( "return action session to pool name=" + shell.name );
				}
				else
				if( checkOldExecutorShell( shell ) ) {
					pending.remove( k );
					killShell( shell );
				}
				else {
					k++;
					engine.serverAction.trace( "stay in pending name=" + shell.name );
				}
			}
		}
		
		// kill old primary 
		synchronized( this ) {
			for( ShellExecutor shell : pool.values().toArray( new ShellExecutor[0] ) ) {
				if( checkOldExecutorShell( shell ) ) {
					pool.remove( shell.name );
					killShell( shell );
				}
				else
					engine.serverAction.trace( "stay in pool name=" + shell.name );
			}
		}
		
		// kill silent action interactive shells 
		synchronized( this ) {
			for( ActionShells map : actionSessions.values() ) {
				for( ShellInteractive shell : map.getInteractiveList() ) {
					if( checkOldInteractiveShell( shell ) )
						killShell( shell );
					else
						engine.serverAction.trace( "stay in pool name=" + shell.name );
				}
			}
		}
	}

	private boolean checkOldExecutorShell( ShellExecutor shell ) {
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
		master = createDedicatedLocalShell( action , "master" );
		tmpFolder.ensureExists( action );
		
		// start managing thread
		started = true;
        thread = new Thread( null , this , "Thread Pool" );
        thread.start();
	}
	
	public void stop( ActionBase action ) {
		try {
			if( started ) {
				stop = true;
				thread.interrupt();
				
				synchronized( thread ) {
					thread.wait();
				}
			}
		}
		catch( Throwable e ) {
			if( action.context.CTX_TRACEINTERNAL )
				action.trace( "exception when killing shell=" + master.name + " (" + e.getMessage() + ")" );
		}
	}
	
	private void killShell( Shell shell ) {
		try {
			shell.kill( engine.serverAction );
		}
		catch( Throwable e ) {
			if( engine.serverAction.context.CTX_TRACEINTERNAL )
				engine.serverAction.trace( "exception when killing shell=" + shell.name + " (" + e.getMessage() + ")" );
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
		
		synchronized( thread ) {
			thread.notifyAll();
		}
	}

	public ShellExecutor getExecutor( ActionBase action , Account account , String scope ) throws Exception {
		if( stop )
			action.exit( "server is in progress of shutdown" );
		
		String name = ( account.local )? "local::" + scope : "remote::" + scope + "::" + account.getPrintName(); 

		// get sync object
		Object sync = null;
		synchronized( this ) {
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
			synchronized( this ) {
				shell = pool.get( name );
				if( shell != null ) {
					pool.remove( name );
					map.addExecutor( shell );
					engine.serverAction.trace( "assign action ID=" + action.ID + " to existing session name=" + name );
					return( shell );
				}
			}

			// create new shell
			if( account.local ) {
				shell = ShellExecutor.getLocalShellExecutor( action , name , this , rootPath , tmpFolder );
				shell.start( action );
			}
			else {
				String REDISTPATH = action.context.CTX_REDISTPATH;
				shell = ShellExecutor.getRemoteShellExecutor( action , name , this , account , REDISTPATH );
				shell.start( action );
			}

			// add to action sessions (return to pool after release)
			synchronized( this ) {
				map.addExecutor( shell );
				engine.serverAction.trace( "assign action ID=" + action.ID + " to new session name=" + name );
			}

			// force create temporary folder on remote location
			if( !account.local )
				shell.tmpFolder.ensureExists( action );
		}
		
		return( shell );
	}

	private ShellExecutor createLocalShell( ActionBase action , String name ) throws Exception {
		if( stop )
			action.exit( "server is in progress of shutdown" );
		
		ShellExecutor shell = ShellExecutor.getLocalShellExecutor( action , "local::" + name , this , rootPath , tmpFolder );
		action.setShell( shell );
		shell.start( action );
		return( shell );
	}
	
	private ActionShells getActionShells( ActionBase action ) {
		ActionShells map = actionSessions.get( action );
		if( map == null ) {
			map = new ActionShells( action );
			actionSessions.put( action , map );
			engine.serverAction.trace( "register in session pool action ID=" + action.ID );
		}
		return( map );
	}
	
	public ShellExecutor createDedicatedLocalShell( ActionBase action , String name ) throws Exception {
		if( stop )
			action.exit( "server is in progress of shutdown" );
		
		if( name.equals( "master" ) )
			return( createLocalShell( action , name ) );
		
		ShellExecutor shell = null;
		synchronized( this ) {
			ActionShells map = getActionShells( action );
			shell = map.getExecutor( name );
			if( shell != null ) {
				action.setShell( shell );
				return( shell );
			}
			
			shell = createLocalShell( action , name );
			map.addExecutor( shell );
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
		synchronized( this ) {
			if( !shell.account.local ) {
				if( pool.get( shell.name ) == null ) {
					pool.put( shell.name , shell );
					engine.serverAction.trace( "return session to pool name=" + shell.name );
				}
				else {
					pending.add( shell );
					engine.serverAction.trace( "put session to pending name=" + shell.name );
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
		synchronized( this ) {
			killShell( shell );
			map.removeInteractive( shell.name );
		}
	}

	public void removeInteractive( ActionBase action , ShellInteractive shell ) {
		synchronized( this ) {
			ActionShells map = actionSessions.get( action );
			if( map == null )
				return;
			
			engine.serverAction.trace( "unregister in session pool action ID=" + action.ID );
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
			
			engine.serverAction.trace( "unregister in session pool action ID=" + action.ID );
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
		
		synchronized( this ) {
			actionSessions.remove( action );
		}
	}

	public ShellInteractive createInteractiveShell( ActionBase action , Account account ) throws Exception {
		String name = "remote::" + account.getPrintName() + "::" + action.ID;
		ShellInteractive shell = ShellInteractive.getShell( action , name , this , account );
		
		// add to action map
		synchronized( this ) {
			ActionShells map = getActionShells( action );
			map.addInteractive( shell );
		}
		
		return( shell );
	}
	
}
