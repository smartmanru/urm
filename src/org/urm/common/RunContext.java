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
	
	private boolean mainMode;
	private boolean standaloneMode;
	private boolean clientMode;
	
	public VarOSTYPE osType;
	public String serverHostPort;

	public String userHome;
	public String installPath;
	public String workPath;
	public String authPath;
	
	public String product;
	public String buildMode;
	public String envName;
	public String dcName;
	public String hostName;
	public String userName;
	public String encoding;

	public static String PROPERTY_HOSTNAME = "hostname";
	public static String PROPERTY_USER_HOME = "userhome";
	public static String PROPERTY_OS_TYPE = "urm.os";
	public static String PROPERTY_INSTALL_PATH = "urm.installpath";
	public static String PROPERTY_WORK_PATH = "urm.workpath";
	public static String PROPERTY_AUTH_PATH = "urm.authpath";
	public static String PROPERTY_SERVER_CONFPATH = "server.conf";
	public static String PROPERTY_SERVER_MASTERPATH = "server.master";
	public static String PROPERTY_SERVER_PRODUCTSPATH = "server.products";
	
	public RunContext() {
	}

	public static RunContext copy( RunContext parent ) throws Exception {
		return( parent.copy() );
	}

	public RunContext copy() throws Exception {
		RunContext rc = new RunContext();
		rc.mainMode = mainMode;
		rc.standaloneMode = standaloneMode;
		rc.clientMode = clientMode;
		
		rc.osType = osType;
		rc.serverHostPort = serverHostPort;

		rc.userHome = userHome;
		rc.installPath = installPath;
		rc.workPath = workPath;
		rc.authPath = authPath;
		
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
		String OSTYPE = getProperty( PROPERTY_OS_TYPE ).toUpperCase();
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
			clientMode = false;
		}
		else if( mode.equals( "server" ) ) {
			mainMode = false;
			standaloneMode = false;
			clientMode = false;
		}
		else
		if( mode.equals( "main" ) ) {
			mainMode = true;
			standaloneMode = false;
			clientMode = false;
		}
		else
		if( mode.equals( "client" ) ) {
			mainMode = false;
			standaloneMode = true;
			clientMode = true;
		}
		else
			Common.exit1( _Error.UnexpectedMode1 , "unexpected mode=" + mode , mode );
			
		if( osType == VarOSTYPE.LINUX ) {
			installPath = getProperty( PROPERTY_INSTALL_PATH );
			workPath = getProperty( PROPERTY_WORK_PATH );
			authPath = getProperty( PROPERTY_AUTH_PATH );
			
			hostName = getEnvRequired( "HOSTNAME" );
			userName = getEnvRequired( "USER" );
	    	userHome = getEnvRequired( "HOME" );
		}
		else {
			installPath = Common.getLinuxPath( getProperty( PROPERTY_INSTALL_PATH ) );
			workPath = Common.getLinuxPath( getProperty( PROPERTY_WORK_PATH ) );
			authPath = Common.getLinuxPath( getProperty( PROPERTY_AUTH_PATH ) );
			
			hostName = getEnvRequired( "COMPUTERNAME" );
			userName = getEnvRequired( "USERNAME" );
	    	userHome = Common.getLinuxPath( getEnvRequired( "HOMEDRIVE" ) + getEnvRequired( "HOMEPATH" ) );
		}
	
		encoding = getProperty( "urm.encoding" );
		
		if( installPath.isEmpty() )
			Common.exit0( _Error.InstallPathNotSet0 , "install path is not set" );
	}
	
	private String getEnvRequired( String key ) throws Exception { 
		String value = System.getenv( key );
		if( value == null )
			Common.exit1( _Error.EnvironmentVariableNotSet1 , "environment variable " + key + " is not set" , key );
		if( value.isEmpty() )
			Common.exit1( _Error.EnvironmentVariableEmpty1 , "environment variable " + key + " is empty" , key );
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

	public boolean isStandalone() {
		return( standaloneMode );		
	}

	public boolean isClientMode() {
		return( clientMode );		
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

	public String getLocalPath( String path ) throws Exception {
		if( isLinux() )
			return( Common.getLinuxPath( path ) );
		if( isWindows() )
			return( Common.getWinPath( path ) );
		Common.exitUnexpected();
		return( null );
	}

	public void getProperties( PropertySet set ) throws Exception {
		set.setStringProperty( PROPERTY_OS_TYPE , Common.getEnumLower( osType ) );
		set.setPathProperty( PROPERTY_INSTALL_PATH , installPath , null );
		set.setPathProperty( PROPERTY_WORK_PATH , workPath , null );
		set.setPathProperty( PROPERTY_USER_HOME , userHome , null );
		set.setPathProperty( PROPERTY_AUTH_PATH , authPath , null );
		set.setStringProperty( PROPERTY_HOSTNAME , hostName );
		set.setPathProperty( PROPERTY_SERVER_CONFPATH , installPath + "/etc" , null );
		set.setPathProperty( PROPERTY_SERVER_MASTERPATH , installPath + "/master" , null );
		set.setPathProperty( PROPERTY_SERVER_PRODUCTSPATH , installPath + "/products" , null );
		
		set.resolveRawProperties();
	}
	
}
