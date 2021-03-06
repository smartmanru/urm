package org.urm.engine;

import org.urm.action.ActionBase;
import org.urm.common.Common;
import org.urm.common.action.CommandMethodMeta.SecurityAction;
import org.urm.engine.data.EngineResources;
import org.urm.engine.properties.EntityVar;
import org.urm.engine.security.AuthContext;
import org.urm.engine.security.AuthResource;
import org.urm.engine.security.AuthUser;
import org.urm.engine.security.CryptoContainer;
import org.urm.engine.security.EngineSecurity;
import org.urm.engine.security.SecureData;
import org.urm.meta.env.MetaEnv;
import org.urm.meta.env.MetaEnvSegment;
import org.urm.meta.env.MetaEnvServer;
import org.urm.meta.env.MetaEnvServerNode;
import org.urm.meta.loader.EngineObject;
import org.urm.meta.product.Meta;
import org.urm.meta.product.MetaDistrConfItem;
import org.urm.meta.system.AppProduct;
import org.urm.meta.system.AppSystem;

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

	public void deleteContainer( ActionBase action , AuthResource rc ) throws Exception {
		security.deleteContainer( action , rc.NAME );
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

	public void setEngineVar( ActionBase action , EntityVar var , String value ) throws Exception {
		AuthService auth = engine.getAuth();
		auth.verifyAccessServerAction( action , SecurityAction.ACTION_SECURED , false );
		
		String key = SecureData.getEngineVar( var );
		master.setKey( action , key , value );
		master.save( action , null );
	}
	
	public void setSystemVar( ActionBase action , AppSystem system , EntityVar var , String value ) throws Exception {
		AuthService auth = engine.getAuth();
		auth.verifyAccessServerAction( action , SecurityAction.ACTION_SECURED , false );
		
		String key = SecureData.getSystemVar( system , var );
		master.setKey( action , key , value );
		master.save( action , null );
	}
	
	public void setMetaVar( ActionBase action , Meta meta , EntityVar var , String value ) throws Exception {
		AppProduct product = meta.getProduct();
		CryptoContainer container = getProductContainer( action , product );
		
		String key = SecureData.getMetaVar( meta , var );
		container.setKey( action , key , value );
		container.save( action , null );
	}
	
	public void setEnvVar( ActionBase action , MetaEnv env , EntityVar var , String value ) throws Exception {
		AppProduct product = env.meta.getProduct();
		CryptoContainer container = getProductContainer( action , product );
		
		String key = SecureData.getEnvVar( env , var );
		container.setKey( action , key , value );
		container.save( action , null );
	}
	
	public void setEnvSegmentVar( ActionBase action , MetaEnvSegment sg , EntityVar var , String value ) throws Exception {
		AppProduct product = sg.meta.getProduct();
		CryptoContainer container = getProductContainer( action , product );
		
		String key = SecureData.getEnvSegmentVar( sg , var );
		container.setKey( action , key , value );
		container.save( action , null );
	}
	
	public void setEnvServerVar( ActionBase action , MetaEnvServer server , EntityVar var , String value ) throws Exception {
		AppProduct product = server.meta.getProduct();
		CryptoContainer container = getProductContainer( action , product );
		
		String key = SecureData.getEnvServerVar( server , var );
		container.setKey( action , key , value );
		container.save( action , null );
	}
	
	public void setEnvServerNodeVar( ActionBase action , MetaEnvServerNode node , EntityVar var , String value ) throws Exception {
		AppProduct product = node.meta.getProduct();
		CryptoContainer container = getProductContainer( action , product );
		
		String key = SecureData.getEnvServerNodeVar( node , var );
		container.setKey( action , key , value );
		container.save( action , null );
	}

	public String getEngineVarEffective( ActionBase action , EntityVar var ) throws Exception {
		return( getEngineVar( action , var ) );
	}
	
	public String getEngineVar( ActionBase action , EntityVar var ) throws Exception {
		AuthService auth = engine.getAuth();
		auth.verifyAccessServerAction( action , SecurityAction.ACTION_SECURED , false );
		
		String key = SecureData.getEngineVar( var );
		return( master.getKey( action , key ) );
	}

	public String getSystemVarEffective( ActionBase action , AppSystem system , EntityVar var ) throws Exception {
		String value = getSystemVar( action , system , var );
		if( !value.isEmpty() )
			return( value );
		return( getEngineVar( action , var ) );
	}
	
	public String getSystemVar( ActionBase action , AppSystem system , EntityVar var ) throws Exception {
		AuthService auth = engine.getAuth();
		auth.verifyAccessServerAction( action , SecurityAction.ACTION_SECURED , false );
		
		String key = SecureData.getSystemVar( system , var );
		return( master.getKey( action , key ) );
	}

	public String getMetaVarEffective( ActionBase action , Meta meta , EntityVar var ) throws Exception {
		return( getMetaVar( action , meta , var ) );
	}
	
	public String getMetaVar( ActionBase action , Meta meta , EntityVar var ) throws Exception {
		AppProduct product = meta.getProduct();
		CryptoContainer container = getProductContainer( action , product );
		
		String key = SecureData.getMetaVar( meta , var );
		return( container.getKey( action , key ) );
	}

	public String getEnvVarEffective( ActionBase action , MetaEnv env , EntityVar var ) throws Exception {
		String value = getEnvVar( action , env , var );
		if( !value.isEmpty() )
			return( value );
		return( getMetaVarEffective( action , env.meta , var ) );
	}
	
	public String getEnvVar( ActionBase action , MetaEnv env , EntityVar var ) throws Exception {
		AppProduct product = env.meta.getProduct();
		CryptoContainer container = getProductContainer( action , product );
		
		String key = SecureData.getEnvVar( env , var );
		return( container.getKey( action , key ) );
	}

	public String getEnvSegmentVarEffective( ActionBase action , MetaEnvSegment sg , EntityVar var ) throws Exception {
		String value = getEnvSegmentVar( action , sg , var );
		if( !value.isEmpty() )
			return( value );
		return( getEnvVarEffective( action , sg.env , var ) );
	}
	
	public String getEnvSegmentVar( ActionBase action , MetaEnvSegment sg , EntityVar var ) throws Exception {
		AppProduct product = sg.meta.getProduct();
		CryptoContainer container = getProductContainer( action , product );
		
		String key = SecureData.getEnvSegmentVar( sg , var );
		return( container.getKey( action , key ) );
	}
	
	public String getEnvServerVarEffective( ActionBase action , MetaEnvServer server , EntityVar var ) throws Exception {
		String value = getEnvServerVar( action , server , var );
		if( !value.isEmpty() )
			return( value );
		return( getEnvSegmentVarEffective( action , server.sg , var ) );
	}
	
	public String getEnvServerVar( ActionBase action , MetaEnvServer server , EntityVar var ) throws Exception {
		AppProduct product = server.meta.getProduct();
		CryptoContainer container = getProductContainer( action , product );
		
		String key = SecureData.getEnvServerVar( server , var );
		return( container.getKey( action , key ) );
	}
	
	public String getEnvServerNodeVarEffective( ActionBase action , MetaEnvServerNode node , EntityVar var ) throws Exception {
		String value = getEnvServerNodeVar( action , node , var );
		if( !value.isEmpty() )
			return( value );
		return( getEnvServerVarEffective( action , node.server , var ) );
	}
	
	public String getEnvServerNodeVar( ActionBase action , MetaEnvServerNode node , EntityVar var ) throws Exception {
		AppProduct product = node.meta.getProduct();
		CryptoContainer container = getProductContainer( action , product );
		
		String key = SecureData.getEnvServerNodeVar( node , var );
		return( container.getKey( action , key ) );
	}

	private CryptoContainer getProductContainer( ActionBase action , AppProduct product ) throws Exception {
		AuthService auth = engine.getAuth();
		auth.verifyAccessProductAction( action , SecurityAction.ACTION_SECURED , product , "" , false );
		
		String key = SecureData.getProductContainerName( product );
		String containerName = master.getKey( action , key );
		
		CryptoContainer container = security.findContainer( containerName );
		if( container != null )
			return( container );
		
		DataService data = engine.getData();
		EngineResources resources = data.getResources();
		AuthResource res = resources.getResource( containerName );
		res.loadAuthData();
		
		return( security.openContainer( action , containerName , res.ac.PASSWORDSAVE ) );
	}

	public boolean hasEnvObjectConf( ActionBase action , EngineObject owner , MetaDistrConfItem conf ) throws Exception {
		String key = "";
		AppProduct product = null;
		
		if( owner instanceof MetaEnv ) {
			MetaEnv env = ( MetaEnv )owner; 
			product = env.meta.getProduct();
			key = SecureData.getEnvConfFolder( env , conf );
		}
		else
		if( owner instanceof MetaEnvSegment ) {
			MetaEnvSegment sg = ( MetaEnvSegment )owner; 
			product = sg.meta.getProduct();
			key = SecureData.getEnvSegmentConfFolder( sg , conf );
		}
		else
		if( owner instanceof MetaEnvServer ) {
			MetaEnvServer server = ( MetaEnvServer )owner; 
			product = server.meta.getProduct();
			key = SecureData.getEnvServerConfFolder( server , conf );
		}
		else
		if( owner instanceof MetaEnvServerNode ) {
			MetaEnvServerNode node = ( MetaEnvServerNode )owner; 
			product = node.meta.getProduct();
			key = SecureData.getEnvServerNodeConfFolder( node , conf );
		}

		CryptoContainer container = getProductContainer( action , product );
		return( container.checkFolderExists( action , key ) );
	}
	
}
