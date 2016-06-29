package org.urm.server.shell;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.urm.server.ServerEngine;
import org.urm.server.action.ActionBase;
import org.urm.server.storage.Folder;

public class ShellExecutorPool {

	public ServerEngine engine;
	public String rootPath;
	
	Map<String,ShellExecutor> pool = new HashMap<String,ShellExecutor>();
	List<ShellExecutor> listRemote = new LinkedList<ShellExecutor>();
	Map<ActionBase,Map<String,ShellExecutor>> mapDedicated = new HashMap<ActionBase,Map<String,ShellExecutor>>();
	Map<String,Object> staged = new HashMap<String,Object>(); 

	public ShellExecutor master;
	public Account account;
	public Folder tmpFolder;
	
	public ShellExecutorPool( ServerEngine engine ) {
		this.engine = engine;
		rootPath = engine.execrc.userHome;
		account = new Account( engine.execrc );
	}
	
	public void start( ActionBase action ) throws Exception {
		tmpFolder = action.artefactory.getTmpFolder( action );
		master = createDedicatedLocalShell( action , "master" );
		tmpFolder.ensureExists( action );
	}
	
	public ShellExecutor getExecutor( ActionBase action , Account account , String scope ) throws Exception {
		Account execAccount = account;

		String name = ( account.local )? "local::" + scope : "remote::" + scope + "::" + account.HOSTLOGIN; 

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
			shell = pool.get( name );
			if( shell != null )
				return( shell );

			if( account.local ) {
				shell = ShellExecutor.getLocalShellExecutor( action , name , this , rootPath , tmpFolder );
				shell.start( action );
			}
			else {
				String REDISTPATH = action.context.CTX_REDISTPATH;
				shell = ShellExecutor.getRemoteShellExecutor( action , name , this , execAccount , REDISTPATH );
				shell.start( action );
			}
			
			pool.put( name , shell );
			listRemote.add( shell );
			
			if( !account.local )
				shell.tmpFolder.ensureExists( action );
		}
		
		return( shell );
	}

	private ShellExecutor createLocalShell( ActionBase action , String name ) throws Exception {
		ShellExecutor shell = ShellExecutor.getLocalShellExecutor( action , "local::" + name , this , rootPath , tmpFolder );
		action.setShell( shell );
		shell.start( action );
		return( shell );
	}
	
	public ShellExecutor createDedicatedLocalShell( ActionBase action , String name ) throws Exception {
		if( name.equals( "master" ) )
			return( createLocalShell( action , name ) );
		
		ShellExecutor shell = null;
		synchronized( this ) {
			Map<String,ShellExecutor> list = mapDedicated.get( action );
			if( list == null ) {
				list = new HashMap<String,ShellExecutor>();
				mapDedicated.put( action , list );
			}
			
			shell = list.get( name );
			if( shell != null ) {
				action.setShell( shell );
				return( shell );
			}
			
			shell = createLocalShell( action , name );
			list.put( name , shell );
		}
		
		return( shell );
	}

	public void stop( ActionBase action ) {
		try {
			master.kill( action );
		}
		catch( Throwable e ) {
			if( action.context.CTX_TRACEINTERNAL )
				action.trace( "exception when killing shell=" + master.name + " (" + e.getMessage() + ")" );
		}
	}
	
	public void killAll( ActionBase action ) {
		for( ShellExecutor session : listRemote ) {
			try {
				session.kill( action );
			}
			catch( Throwable e ) {
				if( action.context.CTX_TRACEINTERNAL )
					action.trace( "exception when killing shell=" + session.name + " (" + e.getMessage() + ")" );
			}
		}
		
		for( ActionBase actionAffected : mapDedicated.keySet() )
			killDedicated( actionAffected );
	}

	public void killDedicated( ActionBase action ) {
		Map<String,ShellExecutor> list = mapDedicated.get( action );
		if( list == null )
			return;
		
		synchronized( this ) {
			ShellExecutor[] sessions = list.values().toArray( new ShellExecutor[0] );  
			for( int k = sessions.length - 1; k >= 0; k-- ) {
				ShellExecutor session = sessions[ k ]; 
				try {
					session.kill( action );
				}
				catch( Throwable e ) {
					if( action.context.CTX_TRACEINTERNAL )
						action.trace( "exception when killing shell=" + session.name + " (" + e.getMessage() + ")" );
				}
			}
			
			list.clear();
			mapDedicated.remove( action );
		}
	}

	public void runInteractiveSsh( ActionBase action , Account account , String KEY ) throws Exception {
		String cmd = "ssh " + account.HOSTLOGIN;
		if( !KEY.isEmpty() )
			cmd += " -i " + KEY;
		cmd += " < /dev/tty > /dev/tty 2>&1";
		
		action.trace( account.HOSTLOGIN + " execute: " + cmd );
		ProcessBuilder pb = new ProcessBuilder( "sh" , "-c" , cmd );
		Process p = pb.start();
		p.waitFor();
	}
	
}
