package org.urm.server;

import org.urm.common.PropertySet;
import org.urm.server.action.ActionBase;

public class ServerAuthContext {

	ServerAuth auth;
	PropertySet properties;
	
	boolean adminContext;
	
	public String METHOD;
	public String USER;
	public String PASSWORDONLINE;
	public String PASSWORDSAVE;
	
	public static String METHOD_ANONYMOUS = "anonymous"; 
	public static String METHOD_COMMON = "common"; 
	public static String METHOD_USER = "user"; 
	
	public ServerAuthContext( ServerAuth auth ) {
		this.auth = auth;
	}
	
	public boolean isAdminContext() {
		return( adminContext );
	}
	
	public boolean isAnonymous() {
		if( METHOD.equals( METHOD_ANONYMOUS ) )
			return( true );
		return( false );
	}
	
	public boolean isCommon() {
		if( METHOD.equals( METHOD_COMMON ) )
			return( true );
		return( false );
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
		properties.setStringProperty( "password" , PASSWORDSAVE );
		properties.setBooleanProperty( "admin" , adminContext );
		properties.finishRawProperties();
	}
	
	private void scatterSystemProperties() throws Exception {
		METHOD = properties.getRequiredPropertyAny( "method" );
		USER = properties.getPropertyAny( "user" );
		PASSWORDSAVE = properties.getPropertyAny( "password" );
		adminContext = properties.getBooleanProperty( "admin" , false );
	}
	
	public String getSvnAuth( ActionBase action ) {
		if( isAnonymous() )
			return( "" );
		
		return( "--username " + getUser( action ) + " --password " + getPassword( action ) );
	}
	
	public String getUser( ActionBase action ) {
		if( isAnonymous() )
			return( "" );
		
		if( isCommon() )
			return( USER );
		
		ServerAuthContext login = action.session.getLoginAuth();
		return( login.USER );
	}
	
	public String getPassword( ActionBase action ) {
		if( isAnonymous() )
			return( "" );
		
		if( isCommon() )
			return( PASSWORDSAVE );
		
		ServerAuthContext login = action.session.getLoginAuth();
		return( login.PASSWORDONLINE );
	}

	public void setResourcePassword( String password ) {
		PASSWORDSAVE = password;
	}
	
}
