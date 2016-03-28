package ru.egov.urm;

public class RunContext {

	public String OSTYPE;
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
		}
		else {
			hostName = "windows";
			userName = "user";
		}
	}
	
	private String getProperty( String name ) {
		String value = System.getProperty( name );
		if( value == null )
			return( "" );
		return( value );
	}
	
}
