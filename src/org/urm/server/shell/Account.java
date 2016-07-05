package org.urm.server.shell;

import org.urm.common.Common;
import org.urm.common.RunContext;
import org.urm.common.RunContext.VarOSTYPE;
import org.urm.server.action.ActionBase;

public class Account {

	public boolean local = false;
	public boolean current = false;
	
	public String HOSTLOGIN;
	public String USER;
	public String HOST;
	public int PORT;
	public VarOSTYPE osType;

	public Account( RunContext execrc ) {
		local = true;
		current = true;
		
		HOSTLOGIN = "local";
		USER = "current";
		HOST = "localhost";
		PORT = 0;
		
		osType = execrc.osType;
	}
	
	private Account( String user , String host , boolean local , VarOSTYPE osType ) {
		this.USER = user;
		this.HOST = host;
		this.PORT = 22;
		this.HOSTLOGIN = user + "@" + host + ":" + PORT;
		this.local = local;
		this.osType = osType;
	}
	
	public static Account getLocalAccount( String user , String host , VarOSTYPE osType ) {
		return( new Account( user , host , true , osType ) );
	}
	
	private Account( String user , String host , int port , VarOSTYPE osType ) {
		this.USER = user;
		this.HOST = host;
		this.PORT = port;
		this.HOSTLOGIN = user + "@" + host + ":" + port;
		this.osType = osType;
	}
	
	public boolean isWindows() {
		return( osType == VarOSTYPE.WINDOWS );
	}
	
	public boolean isLinux() {
		return( osType == VarOSTYPE.LINUX );
	}
	
	public static Account getAccount( ActionBase action , String user , String host , int port , VarOSTYPE osType ) throws Exception {
		if( host.isEmpty() || user.isEmpty() )
			action.exit( "account details are not provided" );
		
		Account account = new Account( user , host , port , osType ); 
		if( action.isLocalRun() ||
			host.equals( "local" ) || host.equals( "localhost" ) ||
			account.HOSTLOGIN.equals( action.context.account.HOSTLOGIN ) )
			account.local = true;
		else
			account.local = false;
		
		return( account );
	}
	
	public static Account getAccount( ActionBase action , String hostLogin , VarOSTYPE osType ) throws Exception {
		if( hostLogin.isEmpty() )
			action.exit( "account details are not provided" );
		
		if( hostLogin.equals( "local" ) )
			return( action.context.account );
			
		String user = Common.getPartBeforeFirst( hostLogin , "@" );
		String host = Common.getPartAfterLast( hostLogin , "@" );
		int port = 22;
		if( host.indexOf( ':' ) > 0 ) {
			port = Integer.parseInt( Common.getPartAfterFirst( host , ":" ) );
			host = Common.getPartBeforeFirst( host , ":" );
		}
			
		return( getAccount( action , user , host , port , osType ) );
	}
	
	public Account getRootAccount( ActionBase action ) throws Exception {
		return( getAccount( action , "root" , HOST , PORT , osType ) );
	}
	
	public Account getUserAccount( ActionBase action , String user ) throws Exception {
		return( getAccount( action , user , HOST , PORT , osType ) );
	}
	
}
