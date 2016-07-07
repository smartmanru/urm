package org.urm.server.shell;

import org.urm.common.Common;
import org.urm.server.action.ActionBase;

abstract public class Shell {

	abstract public void start( ActionBase action ) throws Exception;
	abstract public void kill( ActionBase action ) throws Exception;
	
	public String name;
	public ShellPool pool;
	public Account account;

	public Shell( String name , ShellPool pool , Account account ) {
		this.name = name;
		this.pool = pool;
		this.account = account;
	}
	
	public synchronized String getOSPath( ActionBase action , String path ) throws Exception {
		if( account.isWindows() )
			return( Common.getWinPath( path ) );
		return( path );
	}
	
	public boolean isWindows() {
		return( account.isWindows() );
	}

	public boolean isLinux() {
		return( account.isLinux() );
	}

	public String getOSDevNull() {
		if( isLinux() )
			return( "/dev/null" );
		if( isWindows() )
			return( "nul" );
		return( null );
	}
	
}
