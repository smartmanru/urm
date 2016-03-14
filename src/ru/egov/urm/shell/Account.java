package ru.egov.urm.shell;

import ru.egov.urm.Common;
import ru.egov.urm.meta.Metadata.VarOSTYPE;
import ru.egov.urm.run.ActionBase;

public class Account {

	public boolean local;
	public String HOSTLOGIN;
	public String USER;
	public String HOST;
	public VarOSTYPE OSTYPE;
	
	public Account( String user , String host , boolean local , VarOSTYPE OSTYPE ) {
		this.USER = user;
		this.HOST = host;
		this.HOSTLOGIN = user + "@" + host;
		this.local = local;
		this.OSTYPE = OSTYPE;
	}
	
	private Account( String user , String host , VarOSTYPE OSTYPE ) {
		this.USER = user;
		this.HOST = host;
		this.HOSTLOGIN = user + "@" + host;
		this.OSTYPE = OSTYPE;
	}
	
	public boolean isWindows() {
		return( OSTYPE == VarOSTYPE.WINDOWS );
	}
	
	public static Account getAccount( ActionBase action , String user , String host , VarOSTYPE OSTYPE ) throws Exception {
		if( host.isEmpty() || user.isEmpty() )
			action.exit( "account details are not provided" );
		
		Account account = new Account( user , host , OSTYPE ); 
		if( account.HOSTLOGIN.equals( "local" ) || 
			account.HOSTLOGIN.equals( action.context.account.HOSTLOGIN ) )
			account.local = true;
		else
			account.local = false;
		
		return( account );
	}
	
	public static Account getAccount( ActionBase action , String hostLogin , VarOSTYPE OSTYPE ) throws Exception {
		if( hostLogin.isEmpty() )
			action.exit( "account details are not provided" );
		
		if( hostLogin.equals( "local" ) )
			return( action.context.account );
			
		String user = Common.getPartBeforeFirst( hostLogin , "@" );
		String host = Common.getPartAfterLast( hostLogin , "@" );
		return( getAccount( action , user , host , OSTYPE ) );
	}
	
	public Account getRootAccount( ActionBase action ) throws Exception {
		return( getAccount( action , "root" , HOST , OSTYPE ) );
	}
	
	public Account getUserAccount( ActionBase action , String user ) throws Exception {
		return( getAccount( action , user , HOST , OSTYPE ) );
	}
	
}
