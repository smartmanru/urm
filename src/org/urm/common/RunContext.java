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
	public boolean offlineMode;
	
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
	
	public RunContext() {
	}

	public static RunContext clone( RunContext parent ) throws Exception {
		return( parent.copy() );
	}

	public RunContext copy() throws Exception {
		RunContext rc = new RunContext();
		rc.mainMode = mainMode;
		rc.standaloneMode = standaloneMode;
		rc.offlineMode = offlineMode;
		
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
			offlineMode = true;
		}
		else if( mode.equals( "server" ) ) {
			mainMode = false;
			standaloneMode = false;
			offlineMode = ( serverHostPort.isEmpty() )? true : false;
		}
		else
		if( mode.equals( "main" ) ) {
			mainMode = true;
			standaloneMode = false;
			offlineMode = true;
		}
		else
			throw new ExitException( "unexpected mode=" + mode );
			
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
		return( osType == VarOSTYPE.WINDOWS );		
	}
	
	public boolean isLinux() {
		return( osType == VarOSTYPE.LINUX );		
	}
	
}
