package org.urm.common;

import org.urm.server.meta.Metadata.VarOSTYPE;

public class RunContext {

	private String OSTYPE;
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
	
	public boolean isWindows() {
		return( OSTYPE.equals( "WINDOWS" ) );		
	}
	
	public boolean isLinux() {
		return( OSTYPE.equals( "LINUX" ) );		
	}
	
}
