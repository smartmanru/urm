package org.urm.server.shell;

import org.urm.common.Common;
import org.urm.server.action.ActionBase;
import org.urm.server.meta.Metadata.VarOSTYPE;

public class Account {

	public boolean local;
	public String HOSTLOGIN;
	public String USER;
	public String HOST;
	public VarOSTYPE osType;
	
	private Account( String user , String host , boolean local , VarOSTYPE osType ) {
		this.USER = user;
		this.HOST = host;
		this.HOSTLOGIN = user + "@" + host;
		this.local = local;
		this.osType = osType;
	}
	
	public static Account getLocalAccount( String user , String host , VarOSTYPE osType ) {
		return( new Account( user , host , true , osType ) );
	}
	
	private Account( String user , String host , VarOSTYPE osType ) {
		this.USER = user;
		this.HOST = host;
		this.HOSTLOGIN = user + "@" + host;
		this.osType = osType;
	}
	
	public boolean isWindows() {
		return( osType == VarOSTYPE.WINDOWS );
	}
	
	public boolean isLinux() {
		return( osType == VarOSTYPE.LINUX );
	}
	
	public static Account getAccount( ActionBase action , String user , String host , VarOSTYPE osType ) throws Exception {
		if( host.isEmpty() || user.isEmpty() )
			action.exit( "account details are not provided" );
		
		Account account = new Account( user , host , osType ); 
		if( action.isLocal() ||
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
		return( getAccount( action , user , host , osType ) );
	}
	
	public Account getRootAccount( ActionBase action ) throws Exception {
		return( getAccount( action , "root" , HOST , osType ) );
	}
	
	public Account getUserAccount( ActionBase action , String user ) throws Exception {
		return( getAccount( action , user , HOST , osType ) );
	}
	
}
