package ru.egov.urm.shell;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import ru.egov.urm.action.ActionBase;
import ru.egov.urm.storage.Folder;
import ru.egov.urm.storage.LocalFolder;

public class ShellExecutorPool {

	public String rootPath;
	
	Map<String,ShellExecutor> pool = new HashMap<String,ShellExecutor>();
	List<ShellExecutor> listRemote = new LinkedList<ShellExecutor>();
	List<ShellExecutor> listDedicated = new LinkedList<ShellExecutor>();

	public ShellExecutor master;
	public Account account;
	public Folder tmpFolder;
	
	public ShellExecutorPool( String rootPath ) {
		this.rootPath = rootPath;
	}
	
	public void create( ActionBase action ) throws Exception {
		tmpFolder = new LocalFolder( action.artefactory , action.meta.product.CONFIG_REDISTPATH );
		account = action.context.account;
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
			String REDISTPATH = action.meta.product.CONFIG_REDISTPATH;
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
		listDedicated.add( shell );
		return( shell );
	}
	
	public void kill( ActionBase action ) throws Exception {
		try {
			for( ShellExecutor session : listRemote ) {
				try {
					session.kill( action );
				}
				catch( Throwable e ) {
					if( action.context.CTX_TRACEINTERNAL )
						action.trace( "exception when killing session=" + session.name + " (" + e.getMessage() + ")" );
				}
			}
			
			for( int k = listDedicated.size() - 1; k >= 0; k-- ) {
				ShellExecutor session = listDedicated.get( k ); 
				try {
					session.kill( action );
				}
				catch( Throwable e ) {
					if( action.context.CTX_TRACEINTERNAL )
						action.trace( "exception when killing session=" + session.name + " (" + e.getMessage() + ")" );
				}
			}
		}
		catch( Throwable e ) {
			// silently ignore
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
