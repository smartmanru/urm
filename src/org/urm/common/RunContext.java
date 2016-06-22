package org.urm.common;

import java.io.Serializable;

public class RunContext implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2043100035583299557L;

	public enum VarOSTYPE {
		UNKNOWN ,
		LINUX ,
		WINDOWS
	};
	
	public boolean mainMode;
	public boolean serverMode;
	public String OSTYPE;
	public VarOSTYPE osType;
	public String serverHostPort;

	public String userHome;
	public String installPath;
	
	public String productDir;
	public String buildMode;
	public String envName;
	public String dcName;
	public String hostName;
	public String userName;
	
	public RunContext() {
	}
	
	public void load() throws Exception {
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
		buildMode = getProperty( "urm.build" ).toUpperCase();
		envName = getProperty( "urm.env" );
		dcName = getProperty( "urm.dc" );
		productDir = getProperty( "urm.product" );
		serverHostPort = getProperty( "urm.server" );
		
		if( osType == VarOSTYPE.LINUX ) {
			installPath = getProperty( "urm.installpath" );
			
			hostName = System.getenv( "HOSTNAME" );
			userName = System.getenv( "USER" );
	    	userHome = System.getenv( "HOME" );
		}
		else {
			installPath = Common.getLinuxPath( getProperty( "urm.installpath" ) );
			
			hostName = "windows";
			userName = "user";
	    	userHome = Common.getLinuxPath( System.getenv( "HOMEPATH" ) );
		}
		
		if( installPath.isEmpty() )
			throw new ExitException( "install path is not set" );
	}
	
	private String getProperty( String name ) {
		String value = System.getProperty( name );
		if( value == null )
			return( "" );
		return( value );
	}

	public boolean isRemoteMode() {
		if( serverHostPort.isEmpty() )
			return( false );
		return( true );
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
