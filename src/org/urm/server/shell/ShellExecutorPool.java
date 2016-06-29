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
	Map<ActionBase,List<ShellExecutor>> mapDedicated = new HashMap<ActionBase,List<ShellExecutor>>();

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
		ShellExecutor shell = pool.get( name );
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
		
		return( shell );
	}

	public ShellExecutor createDedicatedLocalShell( ActionBase action , String name ) throws Exception {
		ShellExecutor shell = ShellExecutor.getLocalShellExecutor( action , "local::" + name , this , rootPath , tmpFolder );
		action.setShell( shell );
		
		shell.start( action );
		if( !name.equals( "master" ) ) {
			synchronized( this ) {
				List<ShellExecutor> list = mapDedicated.get( action );
				if( list == null ) {
					list = new LinkedList<ShellExecutor>();
					mapDedicated.put( action , list );
				}
				list.add( shell );
			}
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
		List<ShellExecutor> list = mapDedicated.get( action ); 
		for( int k = list.size() - 1; k >= 0; k-- ) {
			ShellExecutor session = list.get( k );  
			try {
				session.kill( action );
			}
			catch( Throwable e ) {
				if( action.context.CTX_TRACEINTERNAL )
					action.trace( "exception when killing shell=" + session.name + " (" + e.getMessage() + ")" );
			}
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
