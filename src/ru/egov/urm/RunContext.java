package ru.egov.urm;

public class RunContext {

	public String osType;
	public String productHome;
	public String buildMode;
	public String envName;
	public String dcName;
	public String hostName;
	public String userName;
	
	public RunContext() {
	}
	
	public void load() {
		osType = getProperty( "urm.os" );
		productHome = getProperty( "product.home" ).toUpperCase();
		buildMode = getProperty( "build.mode" ).toUpperCase();
		envName = getProperty( "env" );
		dcName = getProperty( "dc" );
		
		if( osType.equals( "linux" ) ) {
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
