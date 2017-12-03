package org.urm.engine.shell;

import java.net.Inet4Address;
import java.net.InetAddress;

import org.urm.action.ActionBase;
import org.urm.common.Common;
import org.urm.common.RunContext;
import org.urm.db.core.DBEnums.DBEnumOSType;
import org.urm.meta.engine.AuthResource;
import org.urm.meta.engine.Datacenter;
import org.urm.meta.engine.HostAccount;
import org.urm.meta.engine.EngineInfrastructure;
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

	private String DATACENTER;
	
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
		DATACENTER = "";
	}
	
	private Account( String user , String host , DBEnumOSType osType ) {
		this.USER = user;
		this.HOST = host;
		this.PORT = 22;
		this.local = true;
		this.osType = osType;
		IP = "";
		DATACENTER = "";
	}
	
	private Account( String user , String host , int port , DBEnumOSType osType ) {
		this.USER = user;
		this.HOST = host;
		this.PORT = port;
		this.osType = osType;
		IP = "";
		DATACENTER = "";
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
	
	public static Account getDatacenterAccount( ActionBase action , String datacenter , String user , String host , int port , DBEnumOSType osType ) throws Exception {
		if( host.isEmpty() || user.isEmpty() )
			action.exit0( _Error.MissingAccountDetails0 , "account details are not provided" );

		boolean local = false;
		if( action.isLocalRun() ||
			host.equals( "local" ) || host.equals( "localhost" ) ||
			( host.equals( action.context.account.HOST ) && user.equals( action.context.account.USER ) ) )
			local = true;
			
		Account account = new Account( user , host , port , osType );
		account.local = local;
		account.DATACENTER = datacenter;
		
		// find account
		HostAccount hostAccount = null;
		if( !local ) {
			EngineInfrastructure infra = action.getServerInfrastructure();
			if( datacenter.isEmpty() )
				action.exit0( _Error.MissingDatacenter0 , "Unable to access account, missing datacenter" );
			
			Datacenter dc = infra.findDatacenter( datacenter );
			if( dc == null )
				action.exit1( _Error.UnknownDatacenter1 , "Unable to access account, unknown datacenter=" + datacenter , datacenter );
			
			hostAccount = dc.getFinalAccount( user + "@" + host );
			if( hostAccount.host.OS_TYPE != osType ) {
				String p1 = Common.getEnumLower( hostAccount.host.OS_TYPE );
				String p2 = Common.getEnumLower( osType );
				action.exit2( _Error.MismatchedOsType2 , "Mismatched OS type: " + p1 + " != " + p2 , p1 , p2 );
			}
			
			account.IP = hostAccount.host.IP;
			account.AUTHRESOURCE_ID = hostAccount.RESOURCE_ID;
		}
		
		return( account );
	}
	
	public static Account getResourceAccount( ActionBase action , AuthResource resource , String user , String host , int port , DBEnumOSType osType ) throws Exception {
		if( host.isEmpty() || user.isEmpty() )
			action.exit0( _Error.MissingAccountDetails0 , "account details are not provided" );
		
		Account account = new Account( user , host , port , osType );
		account.AUTHRESOURCE_ID = AuthResource.getId( resource );
		
		if( action.isLocalRun() ||
			host.equals( "local" ) || host.equals( "localhost" ) ||
			( account.HOST.equals( action.context.account.HOST ) && account.USER.equals( action.context.account.USER ) ) )
			account.local = true;
		else
			account.local = false;
		
		return( account );
	}
	
	public static Account getDatacenterAccount( ActionBase action , String datacenter , String hostLogin , int port , DBEnumOSType osType ) throws Exception {
		String user = Common.getPartBeforeFirst( hostLogin , "@" );
		String host = Common.getPartAfterLast( hostLogin , "@" );
		return( getDatacenterAccount( action , datacenter , user , host , port , osType ) );
	}

	public static Account getResourceAccount( ActionBase action , AuthResource resource , String hostLogin , int port , DBEnumOSType osType ) throws Exception {
		if( hostLogin.isEmpty() )
			action.exit0( _Error.MissingAccountDetails0 , "account details are not provided" );
		
		if( hostLogin.equals( "local" ) )
			return( action.context.account );
			
		String user = Common.getPartBeforeFirst( hostLogin , "@" );
		String host = Common.getPartAfterLast( hostLogin , "@" );
		
		return( getResourceAccount( action , resource , user , host , port , osType ) );
	}
	
	public static Account getDatacenterAccount( ActionBase action , String datacenter , String hostLogin , DBEnumOSType osType ) throws Exception {
		if( hostLogin.isEmpty() )
			action.exit0( _Error.MissingAccountDetails0 , "account details are not provided" );
		
		if( hostLogin.equals( "local" ) )
			return( action.context.account );
			
		String user = Common.getPartBeforeFirst( hostLogin , "@" );
		String host = Common.getPartAfterLast( hostLogin , "@" );
		
		// find account
		EngineInfrastructure infra = action.getServerInfrastructure();
		if( datacenter.isEmpty() )
			action.exit0( _Error.MissingDatacenter0 , "Unable to access account, missing datacenter" );
		
		Datacenter dc = infra.findDatacenter( datacenter );
		if( dc == null )
			action.exit1( _Error.UnknownDatacenter1 , "Unable to access account, unknown datacenter=" + datacenter , datacenter );
		
		HostAccount account = dc.getFinalAccount( hostLogin );
		if( account.host.OS_TYPE != osType ) {
			String p1 = Common.getEnumLower( account.host.OS_TYPE );
			String p2 = Common.getEnumLower( osType );
			action.exit2( _Error.MismatchedOsType2 , "Mismatched OS type: " + p1 + " != " + p2 , p1 , p2 );
		}
		
		return( getDatacenterAccount( action , datacenter , user , host , account.host.PORT , osType ) );
	}

	public static Account getDatacenterAccount( String datacenter , String hostLogin ) {
		String user = Common.getPartBeforeFirst( hostLogin , "@" );
		String host = Common.getPartAfterLast( hostLogin , "@" );
		Account account = new Account( user , host , 0 , DBEnumOSType.UNKNOWN );
		account.DATACENTER = datacenter;
		return( account );
	}

	public static Account getDatacenterAccount( String datacenter , String user , String host ) {
		Account account = new Account( user , host , 0 , DBEnumOSType.UNKNOWN );
		account.DATACENTER = datacenter;
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

	public Account getRootAccount( ActionBase action ) throws Exception {
		return( getDatacenterAccount( action , DATACENTER , "root" , HOST , PORT , osType ) );
	}
	
	public Account getUserAccount( ActionBase action , String user ) throws Exception {
		return( getDatacenterAccount( action , DATACENTER , user , HOST , PORT , osType ) );
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

	public void setHost( ActionBase action , NetworkHost host ) throws Exception {
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
			EngineInfrastructure infra = action.getServerInfrastructure();
			if( DATACENTER.isEmpty() )
				action.exit0( _Error.MissingDatacenter0 , "Unable to access resource, missing datacenter" );
			
			Datacenter dc = infra.findDatacenter( DATACENTER );
			if( dc == null )
				action.exit1( _Error.UnknownDatacenter1 , "Unable to access resource, unknown datacenter=" + DATACENTER , DATACENTER );
			
			HostAccount hostAccount = dc.getFinalAccount( hostLogin );
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
