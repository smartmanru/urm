package org.urm.meta.engine;

import org.urm.action.ActionBase;
import org.urm.common.Common;
import org.urm.engine.AuthService;
import org.urm.engine.properties.PropertySet;
import org.urm.meta.EngineObject;
import org.urm.meta.Types.EnumAuthType;

public class AuthContext extends EngineObject {

	AuthService auth;
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
	
	public AuthContext( AuthService auth ) {
		super( auth );
		this.auth = auth;
	}
	
	@Override
	public String getName() {
		return( "server-auth-context" );
	}
	
	public void createInitialAdministrator() throws Exception {
		USER = "admin";
		setUserPassword( "123" );
		createProperties();
	}
	
	public void createLdap( String name ) {
		this.USER = name;
	}
	
	public EnumAuthType getAccessType() {
		if( isAnonymous() )
			return( EnumAuthType.ANONYMOUS );
		if( isCommon() ) {
			if( USER.isEmpty() )
				return( EnumAuthType.PASSWORD );
			return( EnumAuthType.CREDENTIALS );
		}
		if( isSshKey() )
			return( EnumAuthType.KEYS );
		return( EnumAuthType.UNKNOWN );
	}
	
	public void setAnonymous() {
		METHOD = METHOD_ANONYMOUS;
		USER = "";
	}
	
	public void setCurrentUser() {
		METHOD = METHOD_USER;
		USER = "";
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
	
	public AuthContext copy() throws Exception {
		AuthContext r = new AuthContext( auth );
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
			return( "--non-interactive" );
		
		return( "--non-interactive --username " + getUser( action ) + " --password " + getPassword( action ) );
	}
	
	public String getUser( ActionBase action ) {
		if( isAnonymous() )
			return( "" );
		
		if( isCommon() )
			return( USER );
		
		AuthContext login = action.session.getLoginAuth();
		return( login.USER );
	}
	
	public String getPassword( ActionBase action ) {
		if( isAnonymous() )
			return( "" );
		
		if( isCommon() )
			return( PASSWORDSAVE );
		
		AuthContext login = action.session.getLoginAuth();
		return( login.PASSWORDONLINE );
	}

	public void setOnlinePassword( String password ) {
		PASSWORDONLINE = password;
	}
	
	public void setResourcePassword( String password ) {
		METHOD = AuthContext.METHOD_COMMON;
		PASSWORDSAVE = password;
	}
	
	public void setResourceKeys( String publicKey , String privateKey ) {
		PUBLICKEY = publicKey;
		PRIVATEKEY = privateKey;
	}
	
	public void setUserPassword( String password ) throws Exception {
		METHOD = AuthContext.METHOD_USER;
		PASSWORDSAVE = Common.getMD5( password );
	}

	public void setData( AuthContext acdata ) {
		this.METHOD = acdata.METHOD;
		this.USER = acdata.USER;
		this.PASSWORDSAVE = acdata.PASSWORDSAVE;
		this.PUBLICKEY = acdata.PUBLICKEY;
		this.PRIVATEKEY = acdata.PRIVATEKEY;
	}
	
}
