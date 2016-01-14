package ru.egov.urm.shell;

import ru.egov.urm.Common;
import ru.egov.urm.run.ActionBase;

public class Account {

	public boolean local;
	public String HOSTLOGIN;
	public String USER;
	public String HOST;
	
	public Account( String user , String host , boolean local ) {
		this.local = local;
		this.USER = user;
		this.HOST = host;
		this.HOSTLOGIN = user + "@" + host;
	}
	
	private Account( String user , String host ) {
		this.USER = user;
		this.HOST = host;
		this.HOSTLOGIN = user + "@" + host;
	}
	
	public static Account getAccount( ActionBase action , String user , String host ) {
		Account account = new Account( user , host ); 
		if( account.HOSTLOGIN.equals( "local" ) || 
			account.HOSTLOGIN.equals( action.context.account.HOSTLOGIN ) )
			account.local = true;
		
		account.local = false;
		return( account );
	}
	
	public static Account getAccount( ActionBase action , String hostLogin ) {
		if( hostLogin.isEmpty() || hostLogin.equals( "local" ) )
			return( action.context.account );
			
		String user = Common.getPartBeforeFirst( hostLogin , "@" );
		String host = Common.getPartAfterLast( hostLogin , "@" );
		return( getAccount( action , user , host ) );
	}
	
	public Account getRootAccount( ActionBase action ) throws Exception {
		return( getAccount( action , "root" , HOST ) );
	}
	
	public Account getUserAccount( ActionBase action , String user ) throws Exception {
		return( getAccount( action , user , HOST ) );
	}
	
}
