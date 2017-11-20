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
import org.urm.common.RunContext;
import org.urm.db.core.DBEnums.*;
import org.urm.engine.Engine;
import org.urm.engine.EngineSession;
import org.urm.engine.EngineTransaction;
import org.urm.engine.SessionSecurity;
import org.urm.engine._Error;
import org.urm.engine.action.ActionInit;
import org.urm.engine.properties.PropertySet;
import org.urm.meta.EngineData;
import org.urm.meta.EngineObject;
import org.urm.meta.product.Meta;
import org.urm.meta.Types.*;
import org.urm.meta.product.MetaEnv;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class EngineAuth extends EngineObject {

	public enum SecurityAction {
		ACTION_SECURED ,
		ACTION_CONFIGURE ,
		ACTION_CODEBASE ,
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
	
	Engine engine;

	public static String AUTH_GROUP_RESOURCE = "resource"; 
	public static String AUTH_GROUP_USER = "user"; 
	
	public static String MASTER_ADMIN = "admin";
	
	Map<String,AuthUser> localUsers;
	Map<String,AuthGroup> groups;
	AuthLdap ldapSettings;
	
	public EngineAuth( Engine engine ) {
		super( null );
		this.engine = engine;
		localUsers = new HashMap<String,AuthUser>();
		groups = new HashMap<String,AuthGroup>();
		ldapSettings = new AuthLdap( this ); 
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
			AuthContext ac = new AuthContext( this );
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
			AuthUser user = new AuthUser( this );
			user.loadLocalUser( node );
			addLocalUser( user );
		}
	}

	private void addLocalUser( AuthUser user ) {
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
			AuthGroup group = new AuthGroup( this );
			group.loadGroup( node );
			addGroup( group );
		}
	}

	private void addGroup( AuthGroup group ) {
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
			AuthUser user = localUsers.get( id );
			Element node = Common.xmlCreateElement( doc , usersElement , "user" );
			user.save( doc , node );
		}
		
		Element groupsElement = Common.xmlCreateElement( doc , root , "groups" );
		for( String id : Common.getSortedKeys( groups ) ) {
			AuthGroup user = groups.get( id );
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
	
	public AuthContext loadAuthData( String authKey ) throws Exception {
		PropertySet props = new PropertySet( "authfile" , null );
		String filePath = getAuthFile( authKey );
		
		File file = new File( engine.execrc.getLocalPath( filePath ) );
		if( file.isFile() )
			props.loadFromPropertyFile( filePath , engine.execrc , false );
		props.finishRawProperties();
		
		AuthContext ac = new AuthContext( this );
		ac.load( props );
		return( ac );
	}

	public void saveAuthData( String authKey , AuthContext ac ) throws Exception {
		String filePath = getAuthFile( authKey );
		ac.properties.saveToPropertyFile( filePath , engine.execrc , false , "auth file" );
	}

	public EngineSession connect( String username , String password , RunContext clientrc ) throws Exception {
		AuthUser user = getUser( username );
		if( user == null )
			return( null );
		
		AuthContext ac = null;
		
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
		
		EngineSession session = engine.createClientSession( security , clientrc );
		return( session );
	}

	public SessionSecurity createUserSecurity( String username ) throws Exception {
		AuthUser user = getUser( username );
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

	public AuthUser getLocalUserData( String username ) {
		return( localUsers.get( username ) );
	}
	
	public AuthLdap getLdapSettings() {
		return( ldapSettings );
	}
	
	public void setLdapData( ActionBase action , AuthLdap ldap ) throws Exception {
		ldapSettings = ldap;
		save( action );
	}
	
	public AuthUser getLdapUserData( String username ) {
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

	public AuthUser getUser( String username ) throws Exception {
		AuthUser user = getLocalUserData( username );
		if( user == null )
			user = getLdapUserData( username );
		return( user );
	}

	public AuthGroup createGroup( ActionBase action , String name ) throws Exception {
		AuthGroup group = new AuthGroup( this );
		group.create( action , name );
		addGroup( group );
		return( group );
	}

	public void renameGroup( ActionBase action , AuthGroup group , String name ) throws Exception {
		groups.remove( group.NAME );
		group.rename( action , name );
		addGroup( group );
	}

	public void deleteGroup( ActionBase action , AuthGroup group ) throws Exception {
		groups.remove( group.NAME );
		for( String user : group.getUsers( null ) )
			engine.updatePermissions( action , user );
	}

	public String[] getGroupNames() {
		return( Common.getSortedKeys( groups ) );
	}

	public AuthGroup getGroup( String groupName ) {
		return( groups.get( groupName ) );
	}

	public AuthUser createLocalUser( ActionBase action , String name , String email , String full , boolean admin ) throws Exception {
		AuthUser user = new AuthUser( this );
		user.create( action , true , name , email , full , admin );
		addLocalUser( user );
		return( user );
	}
	
	public void setUserData( ActionBase action , AuthUser user , String email , String full , boolean admin ) throws Exception {
		user.setData( action , email , full , admin );
	}

	public void deleteUser( ActionBase action , AuthUser user ) throws Exception {
		localUsers.remove( user.NAME );
		for( AuthGroup group : groups.values() )
			group.deleteUser( action , user );
		engine.updatePermissions( action , user.NAME );
	}

	public void setUserPassword( ActionBase action , AuthUser user , String password ) throws Exception {
		// create initial admin user
		String authKey = getAuthKey( AUTH_GROUP_USER , user.NAME );
		AuthContext ac = loadAuthData( authKey );
		ac.setUserPassword( password );
		ac.createProperties();
		saveAuthData( authKey , ac );
	}

	public AuthGroup[] getUserGroups( AuthUser user ) {
		List<AuthGroup> list = new LinkedList<AuthGroup>();
		for( AuthGroup group : groups.values() ) {
			if( group.hasUser( user ) )
				list.add( group );
		}
		return( list.toArray( new AuthGroup[0] ) );
	}

	public boolean checkAccessServerAction( ActionBase action , SecurityAction sa , boolean readOnly ) {
		if( action == null )
			return( false );
		
		SessionSecurity security = action.actionInit.session.getSecurity();
		if( security.isAdmin() )
			return( true );
		
		if( sa == SecurityAction.ACTION_SECURED || sa == SecurityAction.ACTION_ADMIN )
			return( false );

		AuthRoleSet roles = security.getBaseRoles();
		
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

	public boolean checkAccessProductAction( ActionBase action , SecurityAction sa , Product product , boolean readOnly ) {
		return( checkAccessProductAction( action , sa , product.NAME , readOnly ) );
	}
	
	public boolean checkAccessProductAction( ActionBase action , SecurityAction sa , Meta meta , boolean readOnly ) {
		return( checkAccessProductAction( action , sa , meta.name , readOnly ) );
	}
	
	public boolean checkAccessProductAction( ActionBase action , SecurityAction sa , Meta meta , DBEnumBuildModeType mode , boolean readOnly ) {
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
	
	public boolean checkAccessProductAction( ActionBase action , SecurityAction sa , String productName , DBEnumBuildModeType mode , boolean readOnly ) {
		return( checkAccessProductAction( action , sa , productName , null , mode , readOnly ) );
	}
	
	public boolean checkAccessProductAction( ActionBase action , SecurityAction sa , Meta meta , MetaEnv env , boolean readOnly ) {
		return( checkAccessProductAction( action , sa , meta.name , env , null , readOnly ) );
	}
	
	public boolean checkAccessProductAction( ActionBase action , SecurityAction sa , MetaEnv env , boolean readOnly ) {
		return( checkAccessProductAction( action , sa , env.meta.name , env , null , readOnly ) );
	}
	
	public boolean checkAccessProductAction( ActionBase action , SecurityAction sa , String productName , MetaEnv env , DBEnumBuildModeType mode , boolean readOnly ) {
		SessionSecurity security = action.actionInit.session.getSecurity();
		if( security.isAdmin() )
			return( true );
		
		AuthRoleSet roles = security.getProductRoles( productName );
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
		
		if( sa == SecurityAction.ACTION_CODEBASE ) {
			if( readOnly ) {
				if( roles.secDev || roles.secRel || roles.secTest || roles.secOpr )
					return( true );
			}
			else {
				if( roles.secDev && ( mode == DBEnumBuildModeType.DEVTRUNK || mode == DBEnumBuildModeType.DEVBRANCH ) )
					return( true );
				if( roles.secRel && ( mode == null || mode == DBEnumBuildModeType.UNKNOWN || mode == DBEnumBuildModeType.TRUNK || mode == DBEnumBuildModeType.BRANCH || mode == DBEnumBuildModeType.MAJORBRANCH ) )
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

	public boolean checkAccessNetworkAction( ActionBase action , SecurityAction sa , Network network ) {
		return( checkAccessNetworkAction( action , sa , network.ID , false , false ) );
	}
	
	public boolean checkAccessNetworkAction( ActionBase action , SecurityAction sa , Network network , boolean configure , boolean allocate ) {
		return( checkAccessNetworkAction( action , sa , network.ID , configure , allocate ) );
	}
	
	public boolean checkAccessNetworkAction( ActionBase action , SecurityAction sa , String networkName , boolean configure , boolean allocate ) {
		SessionSecurity security = action.actionInit.session.getSecurity();
		if( security.isAdmin() )
			return( true );
		
		AuthRoleSet roles = security.getNetworkRoles( networkName );
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
	
	public void addGroupUsers( ActionBase action , AuthGroup group , SourceType source , AuthUser[] users ) throws Exception {
		for( AuthUser user : users ) {
			group.addUser( action , source , user );
			engine.updatePermissions( action , user.NAME );
		}
		save( action );
	}
	
	public void removeGroupUsers( ActionBase action , AuthGroup group , String[] users ) throws Exception {
		for( String user : users ) {
			group.removeUser( action , user );
			engine.updatePermissions( action , user );
		}
		save( action );
	}

	public void setGroupPermissions( ActionBase action , AuthGroup group , AuthRoleSet roles , boolean allProd , String[] products , boolean allNet , String[] networks , SpecialRights[] special) throws Exception {
		group.setGroupPermissions( action , roles , allProd , products , allNet , networks , special );
		for( String user : group.getUsers( null ) )
			engine.updatePermissions( action , user );
		save( action );
	}
	
	public synchronized void deleteProduct( EngineTransaction transaction , Product product ) throws Exception {
		ActionBase action = transaction.getAction();
		boolean authChanged = false;
		for( AuthGroup group : groups.values() ) {
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

	public synchronized void deleteDatacenter( EngineTransaction transaction , Datacenter datacenter ) throws Exception {
		for( String networkName : datacenter.getNetworkNames() ) {
			Network network = datacenter.findNetwork( networkName );
			deleteNetwork( transaction , network );
		}
	}
	
	public synchronized void deleteNetwork( EngineTransaction transaction , Network network ) throws Exception {
		ActionBase action = transaction.getAction();
		boolean authChanged = false;
		for( AuthGroup group : groups.values() ) {
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
			AuthUser user = getUser( username );
			if( user == null ) {
				engine.trace( "unsuccessful login: unknown user=" + username );
				return( false );
			}
			
			String authKey = getAuthKey( AUTH_GROUP_USER , username );
			AuthContext ac = loadAuthData( authKey );
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

	public AuthResource getResource( String name ) throws Exception {
		EngineData data = engine.getData();
		EngineResources resources = data.getResources();
		AuthResource res = resources.getResource( name );
		return( res );
	}
	
}
