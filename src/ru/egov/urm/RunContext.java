package ru.egov.urm;

public class RunContext {

	public String OSTYPE;

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
		productHome = getProperty( "product.home" );
		buildMode = getProperty( "build.mode" ).toUpperCase();
		envName = getProperty( "env" );
		dcName = getProperty( "dc" );
		
		if( OSTYPE.equals( "LINUX" ) ) {
			hostName = System.getenv( "HOSTNAME" );
			userName = System.getenv( "USER" );
	    	userHome = System.getenv( "HOME" );
		}
		else {
			hostName = "windows";
			userName = "user";
	    	userHome = productHome;
		}
	}
	
	private String getProperty( String name ) {
		String value = System.getProperty( name );
		if( value == null )
			return( "" );
		return( value );
	}
	
}
