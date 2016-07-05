package org.urm.server.shell;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.urm.common.Common;
import org.urm.common.jmx.ServerCommandCall;
import org.urm.server.ServerEngine;
import org.urm.server.action.ActionBase;
import org.urm.server.storage.Folder;

public class ShellExecutorPool implements Runnable {

	public ServerEngine engine;
	public String rootPath;
	
	Map<String,Object> staged = new HashMap<String,Object>();
	Map<String,ShellExecutor> pool = new HashMap<String,ShellExecutor>();
	Map<ActionBase,Map<String,ShellExecutor>> actionSessions = new HashMap<ActionBase,Map<String,ShellExecutor>>();
	List<ShellExecutor> pending = new LinkedList<ShellExecutor>();

	public ShellExecutor master;
	public Account account;
	public Folder tmpFolder;
	
	private Thread thread;
	private boolean started = false;
	private boolean stop = false;
	
	private long tsHouseKeepTime = 0;
	private static long SHELL_SILENT_MAX = 60000;
	
	public ShellExecutorPool( ServerEngine engine ) {
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
				if( checkOldShell( shell ) ) {
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
				if( checkOldShell( shell ) ) {
					pool.remove( shell.name );
					killShell( shell );
				}
				else
					engine.serverAction.trace( "stay in pool name=" + shell.name );
			}
		}
	}

	private boolean checkOldShell( ShellExecutor shell ) {
		long finished = shell.tsLastFinished;
		if( finished > 0 && finished + SHELL_SILENT_MAX < tsHouseKeepTime )
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
	
	private void killShell( ShellExecutor shell ) {
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
		
		String name = ( account.local )? "local::" + scope : "remote::" + scope + "::" + account.HOSTLOGIN; 

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
			// from free pool
			shell = pool.get( name );
			if( shell != null )
				return( shell );

			// owned by action
			Map<String,ShellExecutor> map;
			synchronized( this ) {
				map = getActionMap( action );
				shell = map.get( name );
				if( shell != null )
					return( shell );
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
				map.put( name , shell );
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
	
	private Map<String,ShellExecutor> getActionMap( ActionBase action ) {
		Map<String,ShellExecutor> map = actionSessions.get( action );
		if( map == null ) {
			map = new HashMap<String,ShellExecutor>();
			actionSessions.put( action , map );
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
			Map<String,ShellExecutor> map = getActionMap( action );
			shell = map.get( name );
			if( shell != null ) {
				action.setShell( shell );
				return( shell );
			}
			
			shell = createLocalShell( action , name );
			map.put( name , shell );
		}
		
		return( shell );
	}

	public void releaseShell( ActionBase action , ShellExecutor shell ) {
		Map<String,ShellExecutor> map;
		synchronized( this ) {
			map = actionSessions.get( action );
			if( map == null )
				return;
		}
		
		releaseShell( action , shell , map );
	}
	
	public void releaseShell( ActionBase action , ShellExecutor shell , Map<String,ShellExecutor> map ) {
		// put remote sessions to pool or to pending list, kill locals
		if( !shell.account.local ) {
			synchronized( this ) {
				if( pool.get( shell.name ) == null ) {
					pool.put( shell.name , shell );
					engine.serverAction.trace( "return action session to pool name=" + shell.name );
				}
				else {
					pending.add( shell );
					engine.serverAction.trace( "put action session to pending name=" + shell.name );
				}
			}
		}
		else {
			synchronized( this ) {
				killShell( shell );
				map.remove( shell.name );
			}
		}
	}

	public void releaseActionPool( ActionBase action ) {
		Map<String,ShellExecutor> map;
		ShellExecutor[] sessions;
		synchronized( this ) {
			map = actionSessions.get( action );
			if( map == null )
				return;
			
			actionSessions.remove( action );
			sessions = map.values().toArray( new ShellExecutor[0] );
		}
			
		for( int k = sessions.length - 1; k >= 0; k-- ) {
			ShellExecutor shell = sessions[ k ]; 
			releaseShell( action , shell , map );
		}
	}

	public void runInteractiveSsh( ActionBase action , Account account , String KEY ) throws Exception {
		if( action.context.call != null ) {
			runRemoteInteractiveSsh( action , account , KEY );
			return;
		}
		
		String cmd = "ssh " + account.HOSTLOGIN;
		if( !KEY.isEmpty() )
			cmd += " -i " + KEY;
		cmd += " < /dev/tty > /dev/tty 2>&1";
		
		action.trace( account.HOSTLOGIN + " execute: " + cmd );
		ProcessBuilder pb = new ProcessBuilder( "sh" , "-c" , cmd );
		Process p = pb.start();
		p.waitFor();
	}
	
	public void runRemoteInteractiveSsh( ActionBase action , Account account , String KEY ) throws Exception {
		String cmd = "ssh -T " + account.HOSTLOGIN;
		if( !KEY.isEmpty() )
			cmd += " -i " + KEY;
		
		action.trace( account.HOSTLOGIN + " execute: " + cmd );
		ShellExecutor executor = createDedicatedLocalShell( action , "" + action.session.sessionId );
		
		ServerCommandCall call = action.context.call;
		call.createCommunication( executor );
		
		executor.custom( action , cmd , action.context.logLevelLimit );
	}

}
