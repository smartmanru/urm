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
	public boolean standaloneMode;
	
	public VarOSTYPE osType;
	public String serverHostPort;

	public String userHome;
	public String installPath;
	
	public String product;
	public String buildMode;
	public String envName;
	public String dcName;
	public String hostName;
	public String userName;
	public String encoding;
	
	public RunContext() {
	}

	public static RunContext clone( RunContext parent ) throws Exception {
		return( parent.copy() );
	}

	public RunContext copy() throws Exception {
		RunContext rc = new RunContext();
		rc.mainMode = mainMode;
		rc.standaloneMode = standaloneMode;
		
		rc.osType = osType;
		rc.serverHostPort = serverHostPort;

		rc.userHome = userHome;
		rc.installPath = installPath;
		
		rc.product = product;
		rc.buildMode = buildMode;
		rc.envName = envName;
		rc.dcName = dcName;
		rc.hostName = hostName;
		rc.userName = userName;
		rc.encoding = encoding;
		
		return( rc );
	}
	
	public void load() throws Exception {
		String OSTYPE = getProperty( "urm.os" ).toUpperCase();
		osType = VarOSTYPE.valueOf( Common.xmlToEnumValue( OSTYPE ) );
		buildMode = getProperty( "urm.build" ).toUpperCase();
		envName = getProperty( "urm.env" );
		dcName = getProperty( "urm.dc" );
		product = getProperty( "urm.product" );
		
		serverHostPort = getProperty( "urm.server" );
		String mode = getProperty( "urm.mode" );
		if( mode.isEmpty() || mode.equals( "standalone" ) ) {
			mainMode = false;
			standaloneMode = true;
		}
		else if( mode.equals( "server" ) ) {
			mainMode = false;
			standaloneMode = false;
		}
		else
		if( mode.equals( "main" ) ) {
			mainMode = true;
			standaloneMode = false;
		}
		else
			throw new ExitException( "unexpected mode=" + mode );
			
		if( osType == VarOSTYPE.LINUX ) {
			installPath = getProperty( "urm.installpath" );
			
			hostName = getEnvRequired( "HOSTNAME" );
			userName = getEnvRequired( "USER" );
	    	userHome = getEnvRequired( "HOME" );
		}
		else {
			installPath = Common.getLinuxPath( getProperty( "urm.installpath" ) );
			
			hostName = getEnvRequired( "COMPUTERNAME" );
			userName = getEnvRequired( "USERNAME" );
	    	userHome = Common.getLinuxPath( getEnvRequired( "HOMEDRIVE" ) + getEnvRequired( "HOMEPATH" ) );
		}
	
		encoding = getProperty( "urm.encoding" );
		
		if( installPath.isEmpty() )
			throw new ExitException( "install path is not set" );
	}
	
	private String getEnvRequired( String key ) throws Exception { 
		String value = System.getenv( key );
		if( value == null )
			throw new ExitException( "environment variable " + key + " is not set" );
		if( value.isEmpty() )
			throw new ExitException( "environment variable " + key + " is empty" );
		return( value );
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
		return( osType == VarOSTYPE.WINDOWS );		
	}
	
	public boolean isLinux() {
		return( osType == VarOSTYPE.LINUX );		
	}
	
}
