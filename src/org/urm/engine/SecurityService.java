package org.urm.engine;

import org.urm.action.ActionBase;
import org.urm.common.Common;
import org.urm.common.action.CommandMethodMeta.SecurityAction;
import org.urm.engine.data.EngineResources;
import org.urm.engine.security.AuthContext;
import org.urm.engine.security.AuthResource;
import org.urm.engine.security.AuthUser;
import org.urm.engine.security.CryptoContainer;
import org.urm.engine.security.EngineSecurity;
import org.urm.engine.security.SecureData;
import org.urm.meta.loader.EngineObject;
import org.urm.meta.system.AppProduct;

public class SecurityService extends EngineObject {

	public static String MASTER_CONTAINER = "master";
	
	Engine engine;

	private EngineSecurity security;
	private CryptoContainer master;
	
	public SecurityService( Engine engine ) {
		super( null );
		this.engine = engine;
		this.security = new EngineSecurity( this );
	}
	
	@Override
	public String getName() {
		return( "server-security" );
	}

	public void init() throws Exception {
	}
	
	public void start( ActionBase action , String password ) throws Exception {
		security.closeAll();
		master = security.openContainer( action , MASTER_CONTAINER , password );
	}

	public synchronized void stop( ActionBase action ) throws Exception {
		security.closeAll();
	}

	public void createContainer( ActionBase action , AuthResource rc ) throws Exception {
		if( security.findContainer( rc.NAME ) != null )
			Common.exitUnexpected();
		
		if( CryptoContainer.checkExists( action , rc.NAME ) )
			security.openContainer( action , rc.NAME , rc.ac.PASSWORDSAVE );
		else
			security.createContainer( action , rc.NAME , rc.ac.PASSWORDSAVE );
	}
	
	public void openContainer( ActionBase action , AuthResource rc ) throws Exception {
		if( security.findContainer( rc.NAME ) != null )
			return;
		
		security.openContainer( action , rc.NAME , rc.ac.PASSWORDSAVE );
	}
	
	public void saveContainer( ActionBase action , AuthResource rc ) throws Exception {
		if( security.findContainer( rc.NAME ) == null )
			Common.exitUnexpected();
		
		security.saveContainer( action , rc.NAME , rc.ac.PASSWORDSAVE );
	}
	
	public boolean verifyContainer( ActionBase action , AuthResource rc ) throws Exception {
		try {
			CryptoContainer crypto = new CryptoContainer( null , rc.NAME );
			crypto.open( action , rc.ac.PASSWORDSAVE );
			return( true );
		}
		catch( Throwable e ) {
			action.log( "very container" , e );
		}
		return( false );
	}
	
	public String getProductContainerName( ActionBase action , AppProduct product ) throws Exception {
		String key = SecureData.getProductContainerName( product );
		return( master.getKey( action , key ) );
	}
	
	public boolean checkUser( ActionBase action , AuthUser user , AuthContext ac , String password ) throws Exception {
		String passwordMD5 = Common.getMD5( password );
		if( password == null || !passwordMD5.equals( ac.PASSWORDSAVE ) ) {
			action.trace( "failed login user=" + user.NAME );
			return( false );
		}
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
		if( method.isEmpty() ) {
			ac.setAnonymous();
			return;
		}
		
		if( method.equals( AuthContext.METHOD_ANONYMOUS ) ) {
			ac.setAnonymous();
		}
		else
		if( method.equals( AuthContext.METHOD_COMMONPASSWORD ) ) {
			key = SecureData.getResourceUserKey( res );
			String user = master.getKey( action , key );
			ac.setResourceUser( user );
			key = SecureData.getResourcePasswordKey( res );
			String password = master.getKey( action , key );
			ac.setResourcePassword( password );
		}
		else
		if( method.equals( AuthContext.METHOD_SSHKEY ) ) {
			key = SecureData.getResourceUserKey( res );
			String user = master.getKey( action , key );
			ac.setResourceUser( user );
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
		String method = ac.METHOD;
		master.setKey( action , key , ac.METHOD );
		
		if( method == null || method.isEmpty() )
			method = AuthContext.METHOD_ANONYMOUS;
		
		if( method.equals( AuthContext.METHOD_ANONYMOUS ) ) {
		}
		else
		if( method.equals( AuthContext.METHOD_COMMONPASSWORD ) ) {
			key = SecureData.getResourceUserKey( res );
			master.setKey( action , key , ac.USER );
			key = SecureData.getResourcePasswordKey( res );
			master.setKey( action , key , ac.PASSWORDSAVE );
		}
		else
		if( method.equals( AuthContext.METHOD_SSHKEY ) ) {
			key = SecureData.getResourceUserKey( res );
			master.setKey( action , key , ac.USER );
			key = SecureData.getResourceSshPublicKey( res );
			master.setKey( action , key , ac.PUBLICKEY );
			key = SecureData.getResourceSshPrivateKey( res );
			master.setKey( action , key , ac.PRIVATEKEY );
		}
		else
			Common.exitUnexpected();
		
		master.save( action , null );
	}
	
	public void setProductContainer( ActionBase action , AppProduct product , String containerName , String password ) throws Exception {
		AuthService auth = engine.getAuth();
		auth.verifyAccessProductAction( action , SecurityAction.ACTION_SECURED , product , "" , false );
		
		DataService data = engine.getData();
		EngineResources resources = data.getResources();
		AuthResource res = resources.getResource( containerName );
		res.checkPassword( action , password );
		
		openContainer( action , res );
		String key = SecureData.getProductContainerName( product );
		master.setKey( action , key , containerName );
		master.save( action , null );
	}

	public void clearProductContainer( ActionBase action , AppProduct product ) throws Exception {
		AuthService auth = engine.getAuth();
		auth.verifyAccessProductAction( action , SecurityAction.ACTION_SECURED , product , "" , false );
		
		DataService data = engine.getData();
		EngineResources resources = data.getResources();
		String containerName = getProductContainerName( action , product );
		if( containerName.isEmpty() )
			return;
		
		AuthResource res = resources.getResource( containerName );
		
		openContainer( action , res );
		CryptoContainer container = security.findContainer( res.NAME );
		String key = SecureData.getProductFolder( product );
		container.clearKeySet( action , key );
		container.save( action , null );
		
		key = SecureData.getProductFolder( product );
		master.clearKeySet( action , key );
		master.save( action , null );
	}
	
}
