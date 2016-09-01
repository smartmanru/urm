package org.urm.server;

import org.urm.common.PropertySet;

public class ServerAuthContext {

	ServerAuth auth;
	PropertySet properties;
	
	boolean adminContext;
	
	public String METHOD;
	public String USER;
	public String PASSWORD;
	
	public static String METHOD_ANONYMOUS = "anonymous"; 
	public static String METHOD_COMMON = "common"; 
	public static String METHOD_USER = "user"; 
	
	public ServerAuthContext( ServerAuth auth ) {
		this.auth = auth;
	}
	
	public boolean isAdminContext() {
		return( adminContext );
	}
	
	public ServerAuthContext copy() throws Exception {
		ServerAuthContext r = new ServerAuthContext( auth );
		if( properties != null ) {
			r.properties = properties.copy( properties.parent );
			r.scatterSystemProperties();
		}
		return( r );
	}
	
	public void load( PropertySet properties ) throws Exception {
		this.properties = properties;
		scatterSystemProperties();
	}

	public void createProperties() throws Exception {
		properties = new PropertySet( "authctx" , null );
		properties.setStringProperty( "method" , METHOD );
		properties.setStringProperty( "user" , USER );
		properties.setStringProperty( "password" , PASSWORD );
		properties.setBooleanProperty( "admin" , adminContext );
		properties.finishRawProperties();
	}
	
	private void scatterSystemProperties() throws Exception {
		METHOD = properties.getRequiredPropertyAny( "method" );
		USER = properties.getPropertyAny( "user" );
		PASSWORD = properties.getPropertyAny( "password" );
		adminContext = properties.getBooleanProperty( "admin" , false );
	}
	
	public String getSvnAuth() {
		return( "--username " + USER + " --password " + PASSWORD );
	}
	
}
