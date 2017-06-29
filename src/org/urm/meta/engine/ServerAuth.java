package org.urm.meta.engine;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.naming.directory.SearchResult;

import org.urm.action.ActionBase;
import org.urm.action.ActionCore;
import org.urm.client.ClientAuth;
import org.urm.common.Common;
import org.urm.common.ConfReader;
import org.urm.common.PropertySet;
import org.urm.common.RunContext;
import org.urm.engine.ServerEngine;
import org.urm.engine.ServerSession;
import org.urm.engine.ServerTransaction;
import org.urm.engine.SessionSecurity;
import org.urm.engine._Error;
import org.urm.engine.action.ActionInit;
import org.urm.meta.ServerObject;
import org.urm.meta.product.Meta;
import org.urm.meta.Types.*;
import org.urm.meta.product.MetaEnv;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class ServerAuth extends ServerObject {

	public enum SecurityAction {
		ACTION_SECURED ,
		ACTION_CONFIGURE ,
		ACTION_BUILD ,
		ACTION_RELEASE ,
		ACTION_DEPLOY ,
		ACTION_MONITOR ,
		ACTION_XDOC ,
		ACTION_ADMIN ,
		ACTION_EXECUTE
	};
	
	public enum SourceType {
		SOURCE_LOCAL ,
		SOURCE_LDAP
	};
	
	public enum SpecialRights {
		SPECIAL_BASEADM ,
		SPECIAL_BASEITEMS
	};
	
	ServerEngine engine;

	public static String AUTH_GROUP_RESOURCE = "resource"; 
	public static String AUTH_GROUP_USER = "user"; 
	
	public static String MASTER_ADMIN = "admin";
	
	Map<String,ServerAuthUser> localUsers;
	Map<String,ServerAuthGroup> groups;
	ServerAuthLdap ldapSettings;
	
	public ServerAuth( ServerEngine engine ) {
		super( null );
		this.engine = engine;
		localUsers = new HashMap<String,ServerAuthUser>();
		groups = new HashMap<String,ServerAuthGroup>();
		ldapSettings = new ServerAuthLdap( this ); 
	}
	
	@Override
	public String getName() {
		return( "server-auth" );
	}
	
	public void init() throws Exception {
		// read users
		String authFile = getAuthFile();
		load( authFile , engine.execrc );

		if( !localUsers.containsKey( MASTER_ADMIN ) )
			Common.exit0( _Error.MissingAdminUser0 , "Missing master administrator user (" + MASTER_ADMIN + ")" );
			
		// create initial admin user
		String authKey = getAuthKey( AUTH_GROUP_USER , MASTER_ADMIN );
		String authPath = getAuthFile( authKey );
		
		File authDir = new File( getAuthDir() );
		if( !authDir.isDirectory() )
			authDir.mkdir();
		
		File authUserFile = new File( authPath );
		if( !authUserFile.isFile() ) {
			ServerAuthContext ac = new ServerAuthContext( this );
			ac.createInitialAdministrator();
			saveAuthData( authKey , ac );
		}
	}

	public void start( ActionInit action ) throws Exception {
		ldapSettings.start( action );
	}
	
	public void stop( ActionInit action ) throws Exception {
		ldapSettings.stop( action );
	}
	
	private String getAuthFile() {
		String path = Common.getPath( engine.execrc.installPath , "etc" );
		String authFile = Common.getPath( path , "auth.xml" );
		return( authFile );
	}
	
	public void load( String userFile , RunContext execrc ) throws Exception {
		Document doc = ConfReader.readXmlFile( execrc , userFile );
		Node root = doc.getDocumentElement();
		readLocalUsers( root );
		readGroups( root );
		
		Node ldap = ConfReader.xmlGetFirstChild( root , "ldap" );
		if( ldap != null )
			ldapSettings.load( ldap );
	}
	
	private void readLocalUsers( Node root ) throws Exception {
		localUsers.clear();
		Node userList = ConfReader.xmlGetFirstChild( root , "localusers" );
		if( userList == null )
			return;
		
		Node[] list = ConfReader.xmlGetChildren( userList , "user" );
		if( list == null )
			return;
		
		for( Node node : list ) {
			ServerAuthUser user = new ServerAuthUser( this );
			user.loadLocalUser( node );
			addLocalUser( user );
		}
	}

	private void addLocalUser( ServerAuthUser user ) {
		localUsers.put( user.NAME , user );
	}
	
	private void readGroups( Node root ) throws Exception {
		groups.clear();
		Node groupList = ConfReader.xmlGetFirstChild( root , "groups" );
		if( groupList == null )
			return;
		
		Node[] list = ConfReader.xmlGetChildren( groupList , "group" );
		if( list == null )
			return;
		
		for( Node node : list ) {
			ServerAuthGroup group = new ServerAuthGroup( this );
			group.loadGroup( node );
			addGroup( group );
		}
	}

	private void addGroup( ServerAuthGroup group ) {
		groups.put( group.NAME , group );
	}
	
	public void save( ActionBase action ) throws Exception {
		String authFile = getAuthFile();
		save( action , authFile , engine.execrc );
	}
	
	private void save( ActionCore action , String path , RunContext execrc ) throws Exception {
		Document doc = Common.xmlCreateDoc( "auth" );
		Element root = doc.getDocumentElement();
		
		Element usersElement = Common.xmlCreateElement( doc , root , "localusers" );
		for( String id : Common.getSortedKeys( localUsers ) ) {
			ServerAuthUser user = localUsers.get( id );
			Element node = Common.xmlCreateElement( doc , usersElement , "user" );
			user.save( doc , node );
		}
		
		Element groupsElement = Common.xmlCreateElement( doc , root , "groups" );
		for( String id : Common.getSortedKeys( groups ) ) {
			ServerAuthGroup user = groups.get( id );
			Element node = Common.xmlCreateElement( doc , groupsElement , "group" );
			user.save( doc , node );
		}
		
		Element ldapElement = Common.xmlCreateElement( doc , root , "ldap" );
		ldapSettings.save( doc , ldapElement );
		
		Common.xmlSaveDoc( doc , path );
	}

	private String getAuthDir() {
		String authPath = engine.execrc.authPath;
		if( authPath.isEmpty() )
			authPath = Common.getPath( engine.execrc.userHome, ".auth" );
		return( authPath );
	}
	
	private String getAuthFile( String authKey ) {
		String authPath = getAuthDir();
		String filePath = Common.getPath( authPath , authKey + ".properties" );
		return( filePath );
	}
	
	public String getAuthKey( String group , String name ) {
		return( group + "-" + name );
	}
	
	public ServerAuthContext loadAuthData( String authKey ) throws Exception {
		PropertySet props = new PropertySet( "authfile" , null );
		String filePath = getAuthFile( authKey );
		
		File file = new File( engine.execrc.getLocalPath( filePath ) );
		if( file.isFile() )
			props.loadFromPropertyFile( filePath , engine.execrc , false );
		props.finishRawProperties();
		
		ServerAuthContext ac = new ServerAuthContext( this );
		ac.load( props );
		return( ac );
	}

	public void saveAuthData( String authKey , ServerAuthContext ac ) throws Exception {
		String filePath = getAuthFile( authKey );
		ac.properties.saveToPropertyFile( filePath , engine.execrc , false , "auth file" );
	}

	public ServerSession connect( String username , String password , RunContext clientrc ) throws Exception {
		ServerAuthUser user = getUser( username );
		if( user == null )
			return( null );
		
		ServerAuthContext ac = null;
		
		if( user.local ) {
			String authKey = getAuthKey( AUTH_GROUP_USER , username );
			ac = loadAuthData( authKey );
				
			String passwordMD5 = Common.getMD5( password );
			if( password == null || !passwordMD5.equals( ac.PASSWORDSAVE ) )
				return( null );
		}
		else {
			SearchResult res = ldapSettings.verifyLogin( username , password );
			if( res == null )
				return( null );
			
			ac = ldapSettings.getAuthContext( res );
		}

		ac.setOnlinePassword( password );
		
		SessionSecurity security = new SessionSecurity( this );
		security.setUser( user );
		security.setContext( ac );
		security.setPermissions();
		
		ServerSession session = engine.createClientSession( security , clientrc );
		return( session );
	}

	public SessionSecurity createUserSecurity( String username ) throws Exception {
		ServerAuthUser user = getUser( username );
		if( user == null )
			return( null );

		SessionSecurity security = new SessionSecurity( this );
		security.setPermissions();
		security.setUser( user );
		security.setPermissions();
		return( security );
	}
	
	public SessionSecurity createServerSecurity() throws Exception {
		SessionSecurity security = new SessionSecurity( this );
		security.setServer();
		security.setPermissions();
		return( security );
	}
	
	public String[] getLocalUserList() {
		return( Common.getSortedKeys( localUsers ) );
	}
	
	public String[] getLdapUserList() {
		try {
			return( ldapSettings.getUserList() );
		}
		catch( Throwable e ) {
			return( new String[0] );
		}
	}

	public ServerAuthUser getLocalUserData( String username ) {
		return( localUsers.get( username ) );
	}
	
	public ServerAuthLdap getLdapSettings() {
		return( ldapSettings );
	}
	
	public void setLdapData( ActionBase action , ServerAuthLdap ldap ) throws Exception {
		ldapSettings = ldap;
		save( action );
	}
	
	public ServerAuthUser getLdapUserData( String username ) {
		ActionBase action = engine.serverAction;
		if( action == null )
			return( null );

		try {
			return( ldapSettings.getLdapUserData( action , username ) );
		}
		catch( Throwable e ) {
			action.log( "find user in LDAP" , e );
		}
		return( null );
	}

	public ServerAuthUser getUser( String username ) throws Exception {
		ServerAuthUser user = getLocalUserData( username );
		if( user == null )
			user = getLdapUserData( username );
		return( user );
	}

	public ServerAuthGroup createGroup( ActionBase action , String name ) throws Exception {
		ServerAuthGroup group = new ServerAuthGroup( this );
		group.create( action , name );
		addGroup( group );
		return( group );
	}

	public void renameGroup( ActionBase action , ServerAuthGroup group , String name ) throws Exception {
		groups.remove( group.NAME );
		group.rename( action , name );
		addGroup( group );
	}

	public void deleteGroup( ActionBase action , ServerAuthGroup group ) throws Exception {
		groups.remove( group.NAME );
		for( String user : group.getUsers( null ) )
			engine.updatePermissions( action , user );
	}

	public String[] getGroupNames() {
		return( Common.getSortedKeys( groups ) );
	}

	public ServerAuthGroup getGroup( String groupName ) {
		return( groups.get( groupName ) );
	}

	public ServerAuthUser createLocalUser( ActionBase action , String name , String email , String full , boolean admin ) throws Exception {
		ServerAuthUser user = new ServerAuthUser( this );
		user.create( action , true , name , email , full , admin );
		addLocalUser( user );
		return( user );
	}
	
	public void setUserData( ActionBase action , ServerAuthUser user , String email , String full , boolean admin ) throws Exception {
		user.setData( action , email , full , admin );
	}

	public void deleteUser( ActionBase action , ServerAuthUser user ) throws Exception {
		localUsers.remove( user.NAME );
		for( ServerAuthGroup group : groups.values() )
			group.deleteUser( action , user );
		engine.updatePermissions( action , user.NAME );
	}

	public void setUserPassword( ActionBase action , ServerAuthUser user , String password ) throws Exception {
		// create initial admin user
		String authKey = getAuthKey( AUTH_GROUP_USER , user.NAME );
		ServerAuthContext ac = loadAuthData( authKey );
		ac.setUserPassword( password );
		ac.createProperties();
		saveAuthData( authKey , ac );
	}

	public ServerAuthGroup[] getUserGroups( ServerAuthUser user ) {
		List<ServerAuthGroup> list = new LinkedList<ServerAuthGroup>();
		for( ServerAuthGroup group : groups.values() ) {
			if( group.hasUser( user ) )
				list.add( group );
		}
		return( list.toArray( new ServerAuthGroup[0] ) );
	}

	public boolean checkAccessServerAction( ActionBase action , SecurityAction sa , boolean readOnly ) {
		if( action == null )
			return( false );
		
		SessionSecurity security = action.actionInit.session.getSecurity();
		if( security.isAdmin() )
			return( true );
		
		if( sa == SecurityAction.ACTION_SECURED || sa == SecurityAction.ACTION_ADMIN )
			return( false );

		ServerAuthRoleSet roles = security.getBaseRoles();
		
		if( sa == SecurityAction.ACTION_MONITOR || sa == SecurityAction.ACTION_EXECUTE ) {
			if( roles.isAny() )
				return( true );
			return( false );
		}
		
		if( sa == SecurityAction.ACTION_RELEASE ) {
			if( roles.secOpr || roles.secRel || roles.secTest )
				return( true );
		}
		
		if( sa == SecurityAction.ACTION_CONFIGURE || sa == SecurityAction.ACTION_XDOC ) {
			if( readOnly ) {
				if( roles.isAny() )
					return( true );
			}
			return( false );
		}
				
		return( false );
	}

	public boolean checkAccessProductAction( ActionBase action , SecurityAction sa , ServerProduct product , boolean readOnly ) {
		return( checkAccessProductAction( action , sa , product.NAME , readOnly ) );
	}
	
	public boolean checkAccessProductAction( ActionBase action , SecurityAction sa , Meta meta , boolean readOnly ) {
		return( checkAccessProductAction( action , sa , meta.name , readOnly ) );
	}
	
	public boolean checkAccessProductAction( ActionBase action , SecurityAction sa , Meta meta , VarBUILDMODE mode , boolean readOnly ) {
		return( checkAccessProductAction( action , sa , meta.name , null , mode , readOnly ) );
	}
	
	public boolean checkAccessProductAction( ActionBase action , SecurityAction sa , String productName , boolean readOnly ) {
		return( checkAccessProductAction( action , sa , productName , null , action.context.buildMode , readOnly ) );
	}
	
	public boolean checkAccessProductAction( ActionBase action , SecurityAction sa , String productName , String envName , boolean readOnly ) {
		try {
			Meta meta = action.getProductMetadata( productName );
			if( meta == null )
				return( false );
			MetaEnv env = meta.findEnv( envName );
			return( checkAccessProductAction( action , sa , productName , env , null , readOnly ) );
		}
		catch( Throwable e ) {
			action.log( "checkAccessProductAction" , e );
		}
		return( false );
	}
	
	public boolean checkAccessProductAction( ActionBase action , SecurityAction sa , String productName , VarBUILDMODE mode , boolean readOnly ) {
		return( checkAccessProductAction( action , sa , productName , null , mode , readOnly ) );
	}
	
	public boolean checkAccessProductAction( ActionBase action , SecurityAction sa , Meta meta , MetaEnv env , boolean readOnly ) {
		return( checkAccessProductAction( action , sa , meta.name , env , null , readOnly ) );
	}
	
	public boolean checkAccessProductAction( ActionBase action , SecurityAction sa , MetaEnv env , boolean readOnly ) {
		return( checkAccessProductAction( action , sa , env.meta.name , env , null , readOnly ) );
	}
	
	public boolean checkAccessProductAction( ActionBase action , SecurityAction sa , String productName , MetaEnv env , VarBUILDMODE mode , boolean readOnly ) {
		SessionSecurity security = action.actionInit.session.getSecurity();
		if( security.isAdmin() )
			return( true );
		
		ServerAuthRoleSet roles = security.getProductRoles( productName );
		VarENVTYPE envtype = ( env == null )? VarENVTYPE.UNKNOWN : env.envType;
		
		if( sa == SecurityAction.ACTION_SECURED ) {
			if( env == null ) {
				if( roles.secRel )
					return( true );
			}
			else {
				if( ( roles.secDev && envtype == VarENVTYPE.DEVELOPMENT ) || 
					( roles.secRel && envtype == VarENVTYPE.UAT ) || 
					( roles.secTest && envtype == VarENVTYPE.DEVELOPMENT ) || 
					( roles.secTest && envtype == VarENVTYPE.UAT ) || 
					( roles.secOpr && envtype == VarENVTYPE.PRODUCTION ) )
					return( true );
			}
			return( false );
		}
		
		if( sa == SecurityAction.ACTION_CONFIGURE ) {
			if( env == null ) {
				if( readOnly ) {
					if( roles.secDev || roles.secRel || roles.secTest || roles.secOpr )
						return( true );
				}
				else {
					if( roles.secRel )
						return( true );
				}
			}
			else {
				if( readOnly ) {
					if( roles.secDev || roles.secRel || roles.secTest || roles.secOpr )
						return( true );
				}
				else {
					if( ( roles.secDev && envtype == VarENVTYPE.DEVELOPMENT ) || 
						( roles.secRel && envtype == VarENVTYPE.UAT ) || 
						( roles.secOpr && envtype == VarENVTYPE.PRODUCTION ) )
						return( true );
				}
			}
			return( false );
		}
		
		if( sa == SecurityAction.ACTION_BUILD ) {
			if( readOnly ) {
				if( roles.secDev || roles.secRel || roles.secTest || roles.secOpr )
					return( true );
			}
			else {
				if( roles.secDev && ( mode == VarBUILDMODE.DEVTRUNK || mode == VarBUILDMODE.DEVBRANCH ) )
					return( true );
				if( roles.secRel && ( mode == null || mode == VarBUILDMODE.UNKNOWN || mode == VarBUILDMODE.TRUNK || mode == VarBUILDMODE.BRANCH || mode == VarBUILDMODE.MAJORBRANCH ) )
					return( true );
			}
			return( false );
		}
		
		if( sa == SecurityAction.ACTION_RELEASE ) {
			if( roles.secRel || ( readOnly && ( roles.secDev || roles.secTest || roles.secOpr ) ) )
				return( true );
			return( false );
		}
		
		if( sa == SecurityAction.ACTION_DEPLOY || sa == SecurityAction.ACTION_MONITOR || sa == SecurityAction.ACTION_XDOC ) {
			if( readOnly ) {
				if( roles.secDev || roles.secRel || roles.secTest || roles.secOpr )
					return( true );
			}
			else {
				if( ( roles.secDev && envtype == VarENVTYPE.DEVELOPMENT ) || 
					( roles.secRel && envtype == VarENVTYPE.UAT ) || 
					( roles.secTest && envtype == VarENVTYPE.DEVELOPMENT ) || 
					( roles.secTest && envtype == VarENVTYPE.UAT ) || 
					( roles.secOpr && envtype == VarENVTYPE.PRODUCTION ) )
					return( true );
			}
		}
		
		if( sa == SecurityAction.ACTION_ADMIN ) {
			return( false );
		}
				
		return( false );
	}

	public boolean checkAccessNetworkAction( ActionBase action , SecurityAction sa , ServerNetwork network ) {
		return( checkAccessNetworkAction( action , sa , network.ID , false , false ) );
	}
	
	public boolean checkAccessNetworkAction( ActionBase action , SecurityAction sa , ServerNetwork network , boolean configure , boolean allocate ) {
		return( checkAccessNetworkAction( action , sa , network.ID , configure , allocate ) );
	}
	
	public boolean checkAccessNetworkAction( ActionBase action , SecurityAction sa , String networkName , boolean configure , boolean allocate ) {
		SessionSecurity security = action.actionInit.session.getSecurity();
		if( security.isAdmin() )
			return( true );
		
		ServerAuthRoleSet roles = security.getNetworkRoles( networkName );
		if( configure ) {
			if( roles.secInfra )
				return( true );
			return( false );
		}
		
		if( allocate ) {
			if( roles.secDev || roles.secRel || roles.secOpr )
				return( true );
			return( false );
		}

		if( roles.isAny() )
			return( true );
		return( false );
	}
	
	public boolean checkAccessSpecial( ActionBase action , SpecialRights sr ) {
		SessionSecurity security = action.actionInit.session.getSecurity();
		if( security.isAdmin() )
			return( true );
		
		return( security.checkSpecial( sr ) );
	}
	
	public void addGroupUsers( ActionBase action , ServerAuthGroup group , SourceType source , ServerAuthUser[] users ) throws Exception {
		for( ServerAuthUser user : users ) {
			group.addUser( action , source , user );
			engine.updatePermissions( action , user.NAME );
		}
		save( action );
	}
	
	public void removeGroupUsers( ActionBase action , ServerAuthGroup group , String[] users ) throws Exception {
		for( String user : users ) {
			group.removeUser( action , user );
			engine.updatePermissions( action , user );
		}
		save( action );
	}

	public void setGroupPermissions( ActionBase action , ServerAuthGroup group , ServerAuthRoleSet roles , boolean allProd , String[] products , boolean allNet , String[] networks , SpecialRights[] special) throws Exception {
		group.setGroupPermissions( action , roles , allProd , products , allNet , networks , special );
		for( String user : group.getUsers( null ) )
			engine.updatePermissions( action , user );
		save( action );
	}
	
	public synchronized void deleteProduct( ServerTransaction transaction , ServerProduct product ) throws Exception {
		ActionBase action = transaction.getAction();
		boolean authChanged = false;
		for( ServerAuthGroup group : groups.values() ) {
			if( group.hasProduct( product.NAME ) ) {
				authChanged = true;
				group.removeProduct( action , product.NAME );
				for( String user : group.getUsers( null ) )
					engine.updatePermissions( action , user );
			}
		}
		
		if( authChanged )
			save( action );
	}

	public synchronized void deleteDatacenter( ServerTransaction transaction , ServerDatacenter datacenter ) throws Exception {
		for( String networkName : datacenter.getNetworkNames() ) {
			ServerNetwork network = datacenter.findNetwork( networkName );
			deleteNetwork( transaction , network );
		}
	}
	
	public synchronized void deleteNetwork( ServerTransaction transaction , ServerNetwork network ) throws Exception {
		ActionBase action = transaction.getAction();
		boolean authChanged = false;
		for( ServerAuthGroup group : groups.values() ) {
			if( group.hasNetwork( network.ID ) ) {
				authChanged = true;
				group.removeNetwork( action , network.ID );
				for( String user : group.getUsers( null ) )
					engine.updatePermissions( action , user );
			}
		}
		
		if( authChanged )
			save( action );
	}

	public boolean checkLogin( String username , String password ) {
		try {
			ServerAuthUser user = getUser( username );
			if( user == null ) {
				engine.trace( "unsuccessful login: unknown user=" + username );
				return( false );
			}
			
			String authKey = getAuthKey( AUTH_GROUP_USER , username );
			ServerAuthContext ac = loadAuthData( authKey );
			if( !ac.PUBLICKEY.isEmpty() ) {
		        String checkMessage = ClientAuth.getCheckMessage( username );
				if( ClientAuth.verifySigned( checkMessage , password , ac.PUBLICKEY ) ) {
					engine.trace( "successful login using key: user=" + username );
					return( true );
				}
				
				engine.trace( "unsuccessful login using key: user=" + username );
				return( false );
			}
			
			if( !ac.PASSWORDSAVE.isEmpty() ) {
				String md5 = Common.getMD5( password );
				if( ac.PASSWORDSAVE.equals( md5 ) ) {
					ac.setOnlinePassword( password );
					engine.trace( "successful login using password: user=" + username );
					return( true );
				}
				
				engine.trace( "unsuccessful login using password: user=" + username );
				return( false );
			}
		}
		catch( Throwable e ) {
			engine.log( "unsuccessful login: user=" + username , e );
			return( false );
		}
		
		engine.trace( "unknown authentification type: user=" + username );
		return( false );
	}

	public ServerAuthResource getResource( String name ) throws Exception {
		ServerResources resources = engine.getResources();
		ServerAuthResource res = resources.getResource( name );
		return( res );
	}
	
}
