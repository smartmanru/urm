package org.urm.engine.shell;

import java.net.Inet4Address;
import java.net.InetAddress;

import org.urm.action.ActionBase;
import org.urm.common.Common;
import org.urm.common.RunContext;
import org.urm.common.RunContext.VarOSTYPE;
import org.urm.meta.engine.ServerDatacenter;
import org.urm.meta.engine.ServerHostAccount;
import org.urm.meta.engine.ServerInfrastructure;
import org.urm.meta.engine.ServerNetworkHost;

public class Account {

	public boolean local = false;
	public boolean current = false;
	
	public String DC;
	public String USER;
	public String HOST;
	public String IP;
	public int PORT;
	public VarOSTYPE osType;

	public Account( RunContext execrc ) {
		local = true;
		current = true;
		
		USER = "current";
		HOST = "localhost";
		PORT = 0;
		IP = "";
		
		osType = execrc.osType;
	}
	
	private Account( String user , String host , VarOSTYPE osType ) {
		this.USER = user;
		this.HOST = host;
		this.PORT = 22;
		this.local = true;
		this.osType = osType;
		IP = "";
	}
	
	private Account( String datacenter , String user , String host , int port , VarOSTYPE osType ) {
		this.DC = datacenter;
		this.USER = user;
		this.HOST = host;
		this.PORT = port;
		this.osType = osType;
		IP = "";
	}
	
	public static Account getLocalAccount( String user , String host , VarOSTYPE osType ) {
		return( new Account( user , host , osType ) );
	}
	
	public boolean isLocal() {
		return( local );
	}
	
	public boolean isWindows() {
		return( osType == VarOSTYPE.WINDOWS );
	}
	
	public boolean isLinux() {
		return( osType == VarOSTYPE.LINUX );
	}
	
	public static Account getAccount( ActionBase action , String datacenter , String user , String host , int port , VarOSTYPE osType ) throws Exception {
		if( host.isEmpty() || user.isEmpty() )
			action.exit0( _Error.MissingAccountDetails0 , "account details are not provided" );
		
		Account account = new Account( datacenter , user , host , port , osType ); 
		if( action.isLocalRun() ||
			host.equals( "local" ) || host.equals( "localhost" ) ||
			( account.HOST.equals( action.context.account.HOST ) && account.USER.equals( action.context.account.USER ) ) )
			account.local = true;
		else
			account.local = false;
		
		return( account );
	}
	
	public static Account getAccount( ActionBase action , String datacenter , String hostLogin , VarOSTYPE osType ) throws Exception {
		if( hostLogin.isEmpty() )
			action.exit0( _Error.MissingAccountDetails0 , "account details are not provided" );
		
		if( hostLogin.equals( "local" ) )
			return( action.context.account );
			
		String user = Common.getPartBeforeFirst( hostLogin , "@" );
		String host = Common.getPartAfterLast( hostLogin , "@" );
		
		// find account
		ServerInfrastructure infra = action.getServerInfrastructure();
		ServerDatacenter dc = infra.findDatacenter( datacenter );
		if( dc == null )
			action.exit1( _Error.UnknownDatacenter1 , "Unknown datacenter=" + datacenter , datacenter );
		
		ServerHostAccount account = dc.getFinalAccount( action , hostLogin );
		if( account.host.osType != osType ) {
			String p1 = Common.getEnumLower( account.host.osType );
			String p2 = Common.getEnumLower( osType );
			action.exit2( _Error.MismatchedOsType2 , "Mismatched OS type: " + p1 + " != " + p2 , p1 , p2 );
		}
		
		return( getAccount( action , datacenter , user , host , account.host.PORT , osType ) );
	}

	public static Account getAnyAccount( String datacenter , String hostLogin ) {
		String user = Common.getPartBeforeFirst( hostLogin , "@" );
		String host = Common.getPartAfterLast( hostLogin , "@" );
		return( new Account( datacenter ,  user , host , 0 , VarOSTYPE.UNKNOWN ) );
	}

	public static Account getAnyAccount( String datacenter , String user , String host ) {
		return( new Account( datacenter , user , host , 0 , VarOSTYPE.UNKNOWN ) );
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
		return( getAccount( action , DC , "root" , HOST , PORT , osType ) );
	}
	
	public Account getUserAccount( ActionBase action , String user ) throws Exception {
		return( getAccount( action , DC , user , HOST , PORT , osType ) );
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

	public void setHost( ActionBase action , ServerNetworkHost host ) throws Exception {
		if( isHostName() ) {
			HOST = host.ID;
			IP = host.IP;
		}
		else {
			if( !host.IP.isEmpty() )
				HOST = IP = host.IP;
			else
				HOST = IP = host.ID;
		}
		
		PORT = host.PORT;
	}
	
}
