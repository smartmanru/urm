package org.urm.engine.shell;

import java.net.Inet4Address;
import java.net.InetAddress;

import org.urm.action.ActionBase;
import org.urm.common.Common;
import org.urm.common.RunContext;
import org.urm.db.core.DBEnums.DBEnumOSType;
import org.urm.engine.security.AuthResource;
import org.urm.meta.engine.Datacenter;
import org.urm.meta.engine.HostAccount;
import org.urm.meta.engine.NetworkHost;

public class Account {

	public boolean local = false;
	public boolean current = false;
	
	public String USER;
	public String HOST;
	public String IP;
	public int PORT;
	public DBEnumOSType osType;
	public Integer AUTHRESOURCE_ID;

	private Datacenter dc;
	
	public Account( RunContext execrc ) {
		local = true;
		current = true;
		
		USER = "current";
		HOST = "localhost";
		PORT = 0;
		IP = "";
		
		try {
			osType = DBEnumOSType.getValue( execrc.osType );
		}
		catch( Throwable e ) {
			osType = DBEnumOSType.UNKNOWN;
		}
	}
	
	private Account( String user , String host , DBEnumOSType osType ) {
		this.USER = user;
		this.HOST = host;
		this.PORT = 22;
		this.local = true;
		this.osType = osType;
		IP = "";
	}
	
	private Account( String user , String host , int port , DBEnumOSType osType ) {
		this.USER = user;
		this.HOST = host;
		this.PORT = port;
		this.osType = osType;
		IP = "";
	}
	
	public static Account getLocalAccount( String user , String host , DBEnumOSType osType ) {
		return( new Account( user , host , osType ) );
	}
	
	public boolean isLocal() {
		return( local );
	}
	
	public boolean isWindows() {
		return( osType == DBEnumOSType.WINDOWS );
	}
	
	public boolean isLinux() {
		return( osType == DBEnumOSType.LINUX );
	}
	
	public static Account getDatacenterAccount( Datacenter dc , String user , String host , int port , DBEnumOSType osType , HostAccount hostAccount ) throws Exception {
		if( dc == null )
			Common.exit0( _Error.MissingDatacenter0 , "Unable to access account, missing datacenter" );
		
		if( host.isEmpty() || user.isEmpty() )
			Common.exit0( _Error.MissingAccountDetails0 , "account details are not provided" );

		Account engineAccount = dc.infra.engine.serverAction.context.account;
		
		boolean local = false;
		if( host.equals( "local" ) || host.equals( "localhost" ) ||
			( host.equals( engineAccount.HOST ) && user.equals( engineAccount.USER ) ) )
			local = true;
			
		Account account = new Account( user , host , port , osType );
		account.local = local;
		account.dc = dc;
		
		// find account
		if( !local ) {
			if( hostAccount == null ) {
				hostAccount = dc.getAccountByFinal( user + "@" + host );
				if( hostAccount.host.OS_TYPE != osType ) {
					String p1 = Common.getEnumLower( hostAccount.host.OS_TYPE );
					String p2 = Common.getEnumLower( osType );
					Common.exit2( _Error.MismatchedOsType2 , "Mismatched OS type: " + p1 + " != " + p2 , p1 , p2 );
				}
			}
			
			account.IP = hostAccount.host.IP;
			account.AUTHRESOURCE_ID = hostAccount.RESOURCE_ID;
		}
		
		return( account );
	}
	
	public static Account getHostAccount( HostAccount hostAccount ) throws Exception {
		Datacenter dc = hostAccount.host.network.datacenter;
		String user = hostAccount.NAME;
		String host = hostAccount.host.NAME;
		int port = hostAccount.host.PORT;
		DBEnumOSType osType = hostAccount.host.OS_TYPE;
		
		return( getDatacenterAccount( dc , user , host , port , osType , hostAccount ) );
	}

	public static Account getDatacenterAccount( Datacenter dc , String user , String host , int port , DBEnumOSType osType ) throws Exception {		
		return( getDatacenterAccount( dc , user , host , port , osType , null ) );
	}
	
	public static Account getResourceAccount( AuthResource resource , String user , String host , int port , DBEnumOSType osType ) throws Exception {
		if( host.isEmpty() || user.isEmpty() )
			Common.exit0( _Error.MissingAccountDetails0 , "account details are not provided" );
		
		Account account = new Account( user , host , port , osType );
		account.AUTHRESOURCE_ID = AuthResource.getId( resource );
		
		Account engineAccount = resource.resources.engine.serverAction.context.account;
		
		if( host.equals( "local" ) || host.equals( "localhost" ) ||
			( account.HOST.equals( engineAccount.HOST ) && account.USER.equals( engineAccount.USER ) ) )
			account.local = true;
		else
			account.local = false;
		
		return( account );
	}
	
	public static Account getDatacenterAccount( Datacenter dc , String hostLogin , int port , DBEnumOSType osType ) throws Exception {
		String user = Common.getPartBeforeFirst( hostLogin , "@" );
		String host = Common.getPartAfterLast( hostLogin , "@" );
		return( getDatacenterAccount( dc , user , host , port , osType ) );
	}

	public static Account getResourceAccount( AuthResource resource , String hostLogin , int port , DBEnumOSType osType ) throws Exception {
		if( hostLogin.isEmpty() )
			Common.exit0( _Error.MissingAccountDetails0 , "account details are not provided" );
		
		if( hostLogin.equals( "local" ) ) {
			Account engineAccount = resource.resources.engine.serverAction.context.account;
			return( engineAccount );
		}
			
		String user = Common.getPartBeforeFirst( hostLogin , "@" );
		String host = Common.getPartAfterLast( hostLogin , "@" );
		
		return( getResourceAccount( resource , user , host , port , osType ) );
	}
	
	public static Account getDatacenterAccount( Datacenter dc , String hostLogin , DBEnumOSType osType ) throws Exception {
		if( dc == null )
			Common.exit0( _Error.MissingDatacenter0 , "Unable to access account, missing datacenter" );
		
		if( hostLogin.isEmpty() )
			Common.exit0( _Error.MissingAccountDetails0 , "account details are not provided" );
		
		if( hostLogin.equals( "local" ) ) {
			Account engineAccount = dc.infra.engine.serverAction.context.account;
			return( engineAccount );
		}
			
		String user = Common.getPartBeforeFirst( hostLogin , "@" );
		String host = Common.getPartAfterLast( hostLogin , "@" );
		
		// find account
		HostAccount account = dc.getAccountByFinal( hostLogin );
		if( account.host.OS_TYPE != osType ) {
			String p1 = Common.getEnumLower( account.host.OS_TYPE );
			String p2 = Common.getEnumLower( osType );
			Common.exit2( _Error.MismatchedOsType2 , "Mismatched OS type: " + p1 + " != " + p2 , p1 , p2 );
		}
		
		return( getDatacenterAccount( dc , user , host , account.host.PORT , osType ) );
	}

	public static Account getDatacenterAccount( Datacenter dc , String hostLogin ) {
		String user = Common.getPartBeforeFirst( hostLogin , "@" );
		String host = Common.getPartAfterLast( hostLogin , "@" );
		Account account = new Account( user , host , 0 , DBEnumOSType.UNKNOWN );
		account.dc = dc;
		return( account );
	}

	public static Account getDatacenterAccount( Datacenter dc , String user , String host ) {
		Account account = new Account( user , host , 0 , DBEnumOSType.UNKNOWN );
		account.dc = dc;
		return( account );
	}

	public static Account getHostAccount( NetworkHost host ) {
		Account account = new Account( host.network.datacenter.infra.engine.execrc );
		account.setHost( host );
		return( account );
	}
	
	public static boolean isCorrectNetworkMask( String mask ) {
		if( mask == null || mask.isEmpty() )
			return( false );
		
		String[] parts = Common.split( mask , "/" );
		if( parts.length != 2 )
			return( false );
		
		if( !checkNumber( parts[1] , 0 , 32 ) )
			return( false );
		
		if( !isCorrectIP( parts[0] ) )
			return( false );
		
		return( true );
	}

	public static boolean isCorrectAccount( String hostLogin ) {
		if( hostLogin == null || hostLogin.isEmpty() )
			return( false );
		
		String[] parts = Common.split( hostLogin , "@" );
		if( parts.length != 2 )
			return( false );
		
		String[] parts2 = Common.split( parts[1] , ":" );
		if( parts2.length != 1 && parts2.length != 2 )
			return( false );
		
		if( !parts[0].matches( "[.0-9_A-Za-z]+" ) )
			return( false );
		
		if( parts2[0].substring( 0 , 1 ).matches( "[0-9]" ) ) {
			if( !isCorrectIP( parts2[0] ) )
				return( false );
		}
		else {
			if( !parts2[0].matches( "[.0-9_A-Za-z]+" ) )
				return( false );
		}
		return( true );
	}

	public static boolean isCorrectIP( String IP ) {
		String[] parts = Common.splitDotted( IP );
		if( parts.length != 4 )
			return( false );
		if( checkNumber( parts[0] , 0 , 255 ) && checkNumber( parts[1] , 0 , 255 ) && checkNumber( parts[2] , 0 , 255 ) && checkNumber( parts[3] , 0 , 255 ) )
			return( true );
		return( false );
	}
	
	public static boolean checkNumber( String value , int minv , int maxv ) {
		try {
			int res = Integer.parseInt( value );
			if( res < minv || res > maxv )
				return( false );
		}
		catch( Throwable e ) {
			return( false );
		}
		return( true );
	}
	
	public String getFullName() {
		String name = USER + "@" + HOST; 
		if( PORT != 22 )
			name += ":" + PORT;
		return( name );
	}
	
	public String getPrintName() {
		return( getFullName() );
	}

	public String getHostLogin() {
		return( USER + "@" + HOST );
	}
	
	public String getSshAddr() {
		String addr = getHostLogin();
		if( PORT != 22 )
			addr = "-p " + PORT + " " + addr;
		return( addr );
	}

	public Account getRootAccount() throws Exception {
		return( getDatacenterAccount( dc , "root" , HOST , PORT , osType ) );
	}
	
	public Account getUserAccount( String user ) throws Exception {
		return( getDatacenterAccount( dc , user , HOST , PORT , osType ) );
	}

	public String getOSPath( String path ) {
		if( isLinux() )
			return( Common.getLinuxPath( path ) );
		return( Common.getWinPath( path ) );
	}

	public boolean isHostName() {
		String first = HOST.substring( 0 , 1 );
		if( first.matches( "[.0-9]+" ) )
			return( false );
		return( true );
	}

	public String getIP() {
		if( !IP.isEmpty() )
			return( IP );
		
		if( !isHostName() )
			return( HOST );
		
		try {
			InetAddress address = Inet4Address.getByName( HOST );
			IP = address.getHostAddress();
			if( IP == null )
				IP = "";
			return( IP );
		}
		catch( Throwable e ) {
		}
		return( "" );
	}

	public void setHost( NetworkHost host ) {
		dc = host.network.datacenter; 
		if( isHostName() ) {
			HOST = host.NAME;
			IP = host.IP;
		}
		else {
			if( !host.IP.isEmpty() )
				HOST = IP = host.IP;
			else
				HOST = IP = host.NAME;
		}
		
		PORT = host.PORT;
	}

	public AuthResource getResource( ActionBase action ) throws Exception {
		if( AUTHRESOURCE_ID == null ) {
			String hostLogin = getHostLogin();
			if( dc == null )
				action.exit0( _Error.MissingDatacenter0 , "Unable to access resource, missing datacenter" );
			
			HostAccount hostAccount = dc.getAccountByFinal( hostLogin );
			if( hostAccount.RESOURCE_ID == null )
				action.exit1( _Error.MissingAuthKey1 , "Missing auth resource to login to " + hostLogin , hostLogin );
			
			AUTHRESOURCE_ID = hostAccount.RESOURCE_ID;
		}
		
		AuthResource res = action.getResource( AUTHRESOURCE_ID );
		return( res );
	}
	
	public boolean isNativeScp() {
		if( isLinux() )
			return( true );
		return( false );
	}

	public boolean isAdmin() {
		if( osType == DBEnumOSType.LINUX && USER.equals( "root" ) )
			return( true );
		return( false );
	}
	
}
