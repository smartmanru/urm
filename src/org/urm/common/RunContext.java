package org.urm.common;

public class RunContext {

	public enum VarOSTYPE {
		UNKNOWN ,
		LINUX ,
		WINDOWS
	};
	
	public boolean mainMode;
	public boolean serverMode;
	public String OSTYPE;
	public VarOSTYPE osType;

	public String userHome;
	public String productHome;
	public String buildMode;
	public String envName;
	public String dcName;
	public String hostName;
	public String userName;
	
	public RunContext() {
	}
	
	public void load() {
		String mode = getProperty( "urm.mode" );
		if( mode.isEmpty() || mode.equals( "standalone" ) ) {
			mainMode = false;
			serverMode = false;
		}
		else if( mode.equals( "server" ) ) {
			mainMode = false;
			serverMode = true;
		}
		else
		if( mode.equals( "main" ) ) {
			mainMode = true;
			serverMode = false;
		}
			
		OSTYPE = getProperty( "urm.os" ).toUpperCase();
		osType = VarOSTYPE.valueOf( Common.xmlToEnumValue( OSTYPE ) );
		buildMode = getProperty( "build.mode" ).toUpperCase();
		envName = getProperty( "env" );
		dcName = getProperty( "dc" );
		
		if( osType == VarOSTYPE.LINUX ) {
			productHome = getProperty( "product.home" );
			hostName = System.getenv( "HOSTNAME" );
			userName = System.getenv( "USER" );
	    	userHome = System.getenv( "HOME" );
		}
		else {
			productHome = Common.getLinuxPath( getProperty( "product.home" ) );
			hostName = "windows";
			userName = "user";
	    	userHome = Common.getLinuxPath( System.getenv( "HOMEPATH" ) );
		}
	}
	
	private String getProperty( String name ) {
		String value = System.getProperty( name );
		if( value == null )
			return( "" );
		return( value );
	}

	public boolean isServer() {
		return( serverMode );		
	}

	public boolean isMain() {
		return( mainMode );		
	}
	
	public boolean isWindows() {
		return( OSTYPE.equals( "WINDOWS" ) );		
	}
	
	public boolean isLinux() {
		return( OSTYPE.equals( "LINUX" ) );		
	}
	
}
