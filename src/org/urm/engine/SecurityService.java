package org.urm.engine;

import java.util.HashMap;
import java.util.Map;

import org.urm.action.ActionBase;
import org.urm.common.Common;
import org.urm.engine.security.AuthContext;
import org.urm.engine.security.AuthResource;
import org.urm.engine.security.AuthUser;
import org.urm.engine.security.CryptoContainer;
import org.urm.engine.security.SecureData;
import org.urm.meta.loader.EngineObject;

public class SecurityService extends EngineObject {

	public static String MASTER_CONTAINER = "master";
	
	Engine engine;

	private Map<String,CryptoContainer> data;
	private CryptoContainer master;
	
	public SecurityService( Engine engine ) {
		super( null );
		this.engine = engine;
		
		data = new HashMap<String,CryptoContainer>(); 
	}
	
	@Override
	public String getName() {
		return( "server-security" );
	}

	public void init() throws Exception {
	}
	
	public void start( ActionBase action , String password ) throws Exception {
		data.clear();
		master = open( action , MASTER_CONTAINER , password );
	}

	public synchronized void stop( ActionBase action ) throws Exception {
		data.clear();
	}
	
	public synchronized CryptoContainer create( ActionBase action , String name , String password ) throws Exception {
		if( data.get( name ) != null )
			Common.exitUnexpected();
		
		CryptoContainer crypto = new CryptoContainer( this , name );
		crypto.create( action , password );
		data.put( name , crypto );
		
		return( crypto );
	}
	
	public synchronized CryptoContainer open( ActionBase action , String name , String password ) throws Exception {
		if( data.get( name ) != null )
			Common.exitUnexpected();
		
		CryptoContainer crypto = new CryptoContainer( this , name );
		crypto.open( action , password );
		data.put( name , crypto );
		
		return( crypto );
	}

	public boolean checkUser( ActionBase action , AuthContext ac , String password ) throws Exception {
		String passwordMD5 = Common.getMD5( password );
		if( password == null || !passwordMD5.equals( ac.PASSWORDSAVE ) )
			return( false );
		return( true );
	}

	public void loadAuthUserData( ActionBase action , AuthUser user , AuthContext ac ) throws Exception {
		if( master == null )
			Common.exitUnexpected();
		
		String key = null;
		if( user.isMaster() )
			key = SecureData.getMasterPasswordKey();
		else
			key = SecureData.getUserPasswordKey( user );
		
		String passwordMD5 = master.getKey( action , key );
		ac.setUserPasswordMD5( passwordMD5 );
	}

	public void saveAuthUserData( ActionBase action , AuthUser user , AuthContext ac , String password ) throws Exception {
		if( master == null )
			Common.exitUnexpected();
		
		if( user.isMaster() ) {
			String key = SecureData.getMasterPasswordKey();
			master.setKey( action , key , ac.PASSWORDSAVE );
			master.save( action , password );
		}
		else {
			String key = SecureData.getUserPasswordKey( user );
			master.setKey( action , key , ac.PASSWORDSAVE );
			master.save( action , null );
		}
	}
	
	public void loadAuthResourceData( ActionBase action , AuthResource res , AuthContext ac ) throws Exception {
		String key = SecureData.getResourceMethodKey( res );
		String method = master.getKey( action , key );
		if( method == null ) {
			ac.setAnonymous();
			return;
		}
		
		if( method.equals( AuthContext.METHOD_COMMONPASSWORD ) ) {
			key = SecureData.getResourcePasswordKey( res );
			String password = master.getKey( action , key );
			ac.setResourcePassword( password );
		}
		else
		if( method.equals( AuthContext.METHOD_ANONYMOUS ) ) {
			ac.setAnonymous();
		}
		else
		if( method.equals( AuthContext.METHOD_USER ) ) {
			ac.setCurrentUser();
		}
		else
		if( method.equals( AuthContext.METHOD_USER ) ) {
			key = SecureData.getResourceSshPublicKey( res );
			String publicKey = master.getKey( action , key );
			key = SecureData.getResourceSshPrivateKey( res );
			String privateKey = master.getKey( action , key );
			ac.setResourceKeys( publicKey , privateKey );
		}
		else
			Common.exitUnexpected();
	}

	public void saveAuthResourceData( ActionBase action , AuthResource res , AuthContext ac ) throws Exception {
		String key = SecureData.getResourceMethodKey( res );
		String method = master.getKey( action , key );
		master.setKey( action , key , ac.METHOD );
		
		if( method.equals( AuthContext.METHOD_COMMONPASSWORD ) ) {
			key = SecureData.getResourcePasswordKey( res );
			master.setKey( action , key , ac.PASSWORDSAVE );
		}
		else
		if( method.equals( AuthContext.METHOD_ANONYMOUS ) ) {
		}
		else
		if( method.equals( AuthContext.METHOD_USER ) ) {
		}
		else
		if( method.equals( AuthContext.METHOD_USER ) ) {
			key = SecureData.getResourceSshPublicKey( res );
			master.setKey( action , key , ac.PUBLICKEY );
			key = SecureData.getResourceSshPrivateKey( res );
			master.setKey( action , key , ac.PRIVATEKEY );
		}
		else
			Common.exitUnexpected();
		
		master.save( action , null );
	}
	
}

