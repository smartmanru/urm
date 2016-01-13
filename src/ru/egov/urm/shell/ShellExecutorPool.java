package ru.egov.urm.shell;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import ru.egov.urm.run.ActionBase;

public class ShellExecutorPool {

	public String rootPath;
	public int timeoutDefault;
	
	Map<String,ShellExecutor> pool = new HashMap<String,ShellExecutor>();
	List<ShellExecutor> listRemote = new LinkedList<ShellExecutor>();
	List<ShellExecutor> listDedicated = new LinkedList<ShellExecutor>();

	public ShellExecutor master;
	
	public ShellExecutorPool( String rootPath , int timeoutDefault ) {
		this.rootPath = rootPath;
		this.timeoutDefault = timeoutDefault;
	}
	
	public void create( ActionBase action ) throws Exception {
		master = createDedicatedLocalShell( action , "master" );
	}
	
	public ShellExecutor getExecutor( ActionBase action , String hostLogin , String scope ) throws Exception {
		String execHostLogin = hostLogin;

		boolean local = false;
		if( execHostLogin == null || execHostLogin.isEmpty() || execHostLogin.equals( "local" ) || execHostLogin.equals( action.context.hostLogin ) )
			local = true;
		
		String name = ( local )? "local::" + scope : "remote::" + scope + "::" + hostLogin; 
		ShellExecutor shell = pool.get( name );
		if( shell != null )
			return( shell );
		
		if( local ) {
			shell = new LocalShellExecutor( name , this , rootPath );
			shell.start( action );
		}
		else {
			String REDISTPATH = action.meta.product.CONFIG_REDISTPATH;
			shell = new RemoteShellExecutor( name , this , execHostLogin , REDISTPATH );
			shell.start( action );
		}
		
		pool.put( name , shell );
		listRemote.add( shell );
		
		return( shell );
	}

	public ShellExecutor createDedicatedLocalShell( ActionBase action , String name ) throws Exception {
		ShellExecutor shell = new LocalShellExecutor( "local::" + name , this , rootPath );
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
					if( action.options.OPT_TRACE )
						System.out.println( "exception when killing session=" + session.name + " (" + e.getMessage() + ")" );
				}
			}
			
			for( int k = listDedicated.size() - 1; k >= 0; k-- ) {
				ShellExecutor session = listDedicated.get( k ); 
				try {
					session.kill( action );
				}
				catch( Throwable e ) {
					if( action.options.OPT_TRACE )
						System.out.println( "exception when killing session=" + session.name + " (" + e.getMessage() + ")" );
				}
			}
		}
		catch( Throwable e ) {
			// silently ignore
		}
	}

	public void runInteractiveSsh( ActionBase action , String hostLogin , String KEY ) throws Exception {
		String cmd = "ssh " + hostLogin;
		if( !KEY.isEmpty() )
			cmd += " -i " + KEY;
		cmd += " < /dev/tty > /dev/tty 2>&1";
		
		action.trace( hostLogin + " execute: " + cmd );
		ProcessBuilder pb = new ProcessBuilder( "sh" , "-c" , cmd );
		pb.redirectErrorStream(true);
		Process p = pb.start();
		
		BufferedReader stdInput = new BufferedReader( new InputStreamReader( p.getInputStream() ) );
		String s;
		while (( s = stdInput.readLine()) != null) {
		        System.out.println(s);
		}
		p.waitFor();
	}
	
}
