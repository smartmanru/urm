package org.urm.meta.engine;

import org.urm.action.ActionBase;
import org.urm.common.Common;
import org.urm.common.PropertySet;
import org.urm.meta.ServerObject;

public class ServerAuthContext extends ServerObject {

	ServerAuth auth;
	public PropertySet properties;
	
	public String METHOD = "";
	public String USER = "";
	public String PASSWORDONLINE = "";
	public String PASSWORDSAVE = "";
	public String PUBLICKEY = "";
	public String PRIVATEKEY = "";
	
	public static String METHOD_ANONYMOUS = "anonymous"; 
	public static String METHOD_COMMON = "common"; 
	public static String METHOD_USER = "user";
	public static String METHOD_SSHKEY = "sshkey";
	
	public ServerAuthContext( ServerAuth auth ) {
		super( auth );
		this.auth = auth;
	}
	
	public void createInitialAdministrator() throws Exception {
		USER = "admin";
		setUserPassword( "123" );
		createProperties();
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
	
	public boolean isSshKey() {
		if( METHOD.equals( METHOD_SSHKEY ) )
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
		properties.setOriginalStringProperty( "method" , METHOD );
		properties.setOriginalStringProperty( "user" , USER );
		properties.setOriginalStringProperty( "password" , PASSWORDSAVE );
		properties.setOriginalStringProperty( "publickey" , PUBLICKEY );
		properties.setOriginalStringProperty( "privatekey" , PRIVATEKEY );
		properties.finishRawProperties();
	}
	
	private void scatterSystemProperties() throws Exception {
		METHOD = properties.findPropertyAny( "method" );
		USER = properties.findPropertyAny( "user" );
		PASSWORDSAVE = properties.findPropertyAny( "password" );
		PUBLICKEY = properties.findPropertyAny( "publickey" );
		PRIVATEKEY = properties.findPropertyAny( "privatekey" );
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

	public void setOnlinePassword( String password ) {
		PASSWORDONLINE = password;
	}
	
	public void setResourcePassword( String password ) {
		METHOD = ServerAuthContext.METHOD_COMMON;
		PASSWORDSAVE = password;
	}
	
	public void setResourceKeys( String publicKey , String privateKey ) {
		PUBLICKEY = publicKey;
		PRIVATEKEY = privateKey;
	}
	
	public void setUserPassword( String password ) throws Exception {
		METHOD = ServerAuthContext.METHOD_USER;
		PASSWORDSAVE = Common.getMD5( password );
	}
	
}
