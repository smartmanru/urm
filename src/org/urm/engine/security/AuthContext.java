package org.urm.engine.security;

import org.urm.action.ActionBase;
import org.urm.common.Common;
import org.urm.engine.AuthService;
import org.urm.meta.loader.EngineObject;
import org.urm.meta.loader.Types.EnumAuthType;

public class AuthContext extends EngineObject {

	AuthService auth;
	
	public String METHOD = "";
	public String USER = "";
	public String PASSWORDONLINE = "";
	public String PASSWORDSAVE = "";
	public String PUBLICKEY = "";
	public String PRIVATEKEY = "";
	
	public static String METHOD_ANONYMOUS = "anonymous"; 
	public static String METHOD_COMMONPASSWORD = "common"; 
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
	
	public boolean isCurrentUser() {
		if( METHOD.equals( METHOD_USER ) )
			return( true );
		return( false );
	}
	
	public boolean isAnonymous() {
		if( METHOD.equals( METHOD_ANONYMOUS ) )
			return( true );
		return( false );
	}
	
	public boolean isCommon() {
		if( METHOD.equals( METHOD_COMMONPASSWORD ) )
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
		r.METHOD = METHOD;
		r.USER = USER;
		r.PASSWORDONLINE = PASSWORDONLINE;
		r.PASSWORDSAVE = PASSWORDSAVE;
		r.PUBLICKEY = PUBLICKEY;
		r.PRIVATEKEY = PRIVATEKEY;
		return( r );
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
		
		AuthUser user = action.session.getUser();
		return( user.NAME );
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
		METHOD = AuthContext.METHOD_COMMONPASSWORD;
		PASSWORDSAVE = password;
	}
	
	public void setResourceKeys( String publicKey , String privateKey ) {
		METHOD = AuthContext.METHOD_SSHKEY;
		PUBLICKEY = publicKey;
		PRIVATEKEY = privateKey;
	}

	public void setUserPasswordMD5( String passwordMD5 ) throws Exception {
		METHOD = AuthContext.METHOD_USER;
		PASSWORDSAVE = passwordMD5;
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
