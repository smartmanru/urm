package org.urm.engine;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.naming.directory.SearchResult;

import org.urm.action.ActionBase;
import org.urm.client.ClientAuth;
import org.urm.common.Common;
import org.urm.common.RunContext;
import org.urm.common.action.CommandMethodMeta.SecurityAction;
import org.urm.db.core.DBEnums.*;
import org.urm.engine.action.ActionInit;
import org.urm.engine.properties.ObjectProperties;
import org.urm.engine.properties.PropertySet;
import org.urm.engine.session.EngineSession;
import org.urm.engine.session.SessionSecurity;
import org.urm.meta.EngineObject;
import org.urm.meta.MatchItem;
import org.urm.meta.engine.AppProduct;
import org.urm.meta.engine.AuthContext;
import org.urm.meta.engine.AuthGroup;
import org.urm.meta.engine.AuthLdap;
import org.urm.meta.engine.AuthLdapUser;
import org.urm.meta.engine.AuthRoleSet;
import org.urm.meta.engine.AuthUser;
import org.urm.meta.engine.Network;
import org.urm.meta.engine._Error;
import org.urm.meta.env.MetaEnv;
import org.urm.meta.env.ProductEnvs;
import org.urm.meta.product.Meta;

public class AuthService extends EngineObject {

	public enum SourceType {
		SOURCE_LOCAL ,
		SOURCE_LDAP
	};
	
	public enum SpecialRights {
		SPECIAL_ADMCORE ,
		SPECIAL_BASEADM ,
		SPECIAL_BASEITEMS
	};
	
	public Engine engine;

	public static String AUTH_GROUP_RESOURCE = "resource"; 
	public static String AUTH_GROUP_USER = "user"; 
	
	public static String MASTER_ADMIN = "admin";
	
	Map<String,AuthUser> mapLocalUsers;
	Map<Integer,AuthUser> mapLocalUsersById;
	Map<String,AuthUser> mapLdapUsers;
	Map<Integer,AuthUser> mapLdapUsersById;
	Map<String,AuthGroup> groups;
	Map<Integer,AuthGroup> groupsById;
	AuthLdap ldapSettings;
	
	public AuthService( Engine engine ) {
		super( null );
		this.engine = engine;
		mapLocalUsers = new HashMap<String,AuthUser>();
		mapLocalUsersById = new HashMap<Integer,AuthUser>();
		mapLdapUsers = new HashMap<String,AuthUser>();
		mapLdapUsersById = new HashMap<Integer,AuthUser>();
		groups = new HashMap<String,AuthGroup>();
		groupsById = new HashMap<Integer,AuthGroup>();
		ldapSettings = new AuthLdap( this ); 
	}
	
	@Override
	public String getName() {
		return( "server-auth" );
	}
	
	public void init() throws Exception {
	}

	public void start( ActionInit action ) throws Exception {
		if( !mapLocalUsers.containsKey( MASTER_ADMIN ) )
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
	
	public void stop( ActionInit action ) throws Exception {
	}
	
	public void addLocalUser( AuthUser user ) {
		mapLocalUsers.put( user.NAME , user );
		mapLocalUsersById.put( user.ID , user );
	}
	
	public void removeLocalUser( AuthUser user ) {
		mapLocalUsers.remove( user.NAME );
		mapLocalUsersById.remove( user.ID );
		
		for( AuthGroup group : getGroups() ) {
			if( group.hasUser( user ) )
				group.removeUser( user.ID );
		}
	}
	
	public void updateUser( AuthUser user ) throws Exception {
		Common.changeMapKey( mapLocalUsers , user , user.NAME );
	}
	
	public void addLdapUser( AuthUser user ) {
		mapLdapUsers.put( user.NAME , user );
		mapLdapUsersById.put( user.ID , user );
	}
	
	public void addGroup( AuthGroup group ) {
		groups.put( group.NAME , group );
		groupsById.put( group.ID , group );
	}
	
	public void removeGroup( AuthGroup group ) {
		groups.remove( group.NAME );
		groupsById.remove( group.ID );
	}
	
	public void updateGroup( AuthGroup group ) throws Exception {
		Common.changeMapKey( groups , group , group.NAME );
	}
	
	public AuthGroup[] getGroups() {
		return( groups.values().toArray( new AuthGroup[0] ) );
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
		engine.trace( "save auth file: " + filePath );
		ac.createProperties();
		ac.properties.saveToPropertyFile( filePath , engine.execrc , false , "auth file" );
	}

	public EngineSession connect( String username , String password , RunContext clientrc ) throws Exception {
		AuthUser user = getUser( username );
		if( user == null )
			return( null );
		
		AuthContext ac = null;
		
		if( user.LOCAL ) {
			String authKey = getAuthKey( AUTH_GROUP_USER , username );
			ac = loadAuthData( authKey );
				
			String passwordMD5 = Common.getMD5( password );
			if( password == null || !passwordMD5.equals( ac.PASSWORDSAVE ) )
				return( null );
		}
		else {
			ldapSettings.start();
			
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
	
	public String[] getLocalUserNames() {
		return( Common.getSortedKeys( mapLocalUsers ) );
	}
	
	public String[] getLdapUserNames() {
		return( Common.getSortedKeys( mapLdapUsers ) );
	}
	
	public String[] getLdapFullUserList() {
		try {
			return( ldapSettings.getUserList() );
		}
		catch( Throwable e ) {
			return( new String[0] );
		}
	}

	public AuthUser findLocalUser( String username ) {
		return( mapLocalUsers.get( username ) );
	}
	
	public AuthUser findLocalUser( Integer userId ) {
		return( mapLocalUsersById.get( userId ) );
	}
	
	public AuthUser getLocalUser( String username ) throws Exception {
		AuthUser user = mapLocalUsers.get( username );
		if( user == null )
			Common.exit1( _Error.UnknownLocalUser1 , "Unknown local user=" + username , username );
		return( user );
	}
	
	public AuthUser getLocalUser( Integer userId ) throws Exception {
		AuthUser user = mapLocalUsersById.get( userId );
		if( user == null )
			Common.exit1( _Error.UnknownLocalUser1 , "Unknown local user=" + userId , "" + userId );
		return( user );
	}
	
	public AuthUser findLdapUser( String username ) {
		return( mapLdapUsers.get( username ) );
	}
	
	public AuthUser findLdapUser( Integer userId ) {
		return( mapLdapUsersById.get( userId ) );
	}
	
	public AuthUser getLdapUser( String username ) throws Exception {
		AuthUser user = mapLdapUsers.get( username );
		if( user == null )
			Common.exit1( _Error.UnknownLdapUser1 , "Unknown LDAP user=" + username , username );
		return( user );
	}
	
	public AuthUser getLdapUser( Integer userId ) throws Exception {
		AuthUser user = mapLdapUsersById.get( userId );
		if( user == null )
			Common.exit1( _Error.UnknownLdapUser1 , "Unknown LDAP user=" + userId , "" + userId );
		return( user );
	}
	
	public AuthLdap getAuthLdap() {
		return( ldapSettings );
	}
	
	public AuthLdapUser getLdapUserData( String username ) {
		try {
			return( ldapSettings.getLdapUserData( username ) );
		}
		catch( Throwable e ) {
			engine.log( "find user in LDAP" , e );
		}
		return( null );
	}

	public AuthUser getUser( String username ) throws Exception {
		AuthUser user = findLocalUser( username );
		if( user == null )
			user = findLdapUser( username );
		
		if( user == null )
			Common.exit1( _Error.UnknownUser1 , "Unknown user=" + username , username );
		return( user );
	}

	public AuthUser getUser( int userId ) throws Exception {
		AuthUser user = findLocalUser( userId );
		if( user == null )
			user = findLdapUser( userId );
		if( user == null )
			Common.exit1( _Error.UnknownUser1 , "Unknown user=" + userId , "" + userId );
		return( user );
	}

	public String[] getGroupNames() {
		return( Common.getSortedKeys( groups ) );
	}

	public AuthGroup findGroup( String groupName ) {
		return( groups.get( groupName ) );
	}

	public AuthGroup findGroup( int groupId ) {
		return( groupsById.get( groupId ) );
	}

	public AuthUser findUser( int userId ) {
		AuthUser user = mapLocalUsersById.get( userId );
		if( user != null )
			return( user );
		return( mapLdapUsersById.get( userId ) );
	}

	public AuthUser findUser( String name ) {
		AuthUser user = mapLocalUsers.get( name );
		if( user != null )
			return( user );
		return( mapLdapUsers.get( name ) );
	}

	public AuthUser findUser( MatchItem item ) {
		if( item == null )
			return( null );
		if( item.MATCHED )
			return( findUser( item.FKID ) );
		return( findUser( item.FKNAME ) );
	}
	
	public AuthGroup getGroup( String groupName ) throws Exception {
		AuthGroup group = groups.get( groupName );
		if( group == null )
			Common.exit1( _Error.UnknownGroup1 , "Unknown local group=" + groupName , groupName );
		return( group );
	}

	public AuthGroup getGroup( Integer groupId ) throws Exception {
		AuthGroup group = groupsById.get( groupId );
		if( group == null )
			Common.exit1( _Error.UnknownGroup1 , "Unknown local group=" + groupId , "" + groupId );
		return( group );
	}

	public void setUserPassword( AuthUser user , String password ) throws Exception {
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
		if( security.isAdminAny() )
			return( true );
		
		if( sa == SecurityAction.ACTION_SECURED || sa == SecurityAction.ACTION_ADMIN )
			return( false );

		if( security.isAdminCore() )
			return( true );
		
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

	public boolean checkAccessProductAction( ActionBase action , SecurityAction sa , AppProduct product , boolean readOnly ) {
		return( checkAccessProductAction( action , sa , product , null , DBEnumBuildModeType.UNKNOWN , readOnly ) );
	}
	
	public boolean checkAccessProductAction( ActionBase action , SecurityAction sa , Meta meta , boolean readOnly ) {
		return( checkAccessProductAction( action , sa , meta.findProduct() , null , DBEnumBuildModeType.UNKNOWN , readOnly ) );
	}
	
	public boolean checkAccessProductAction( ActionBase action , SecurityAction sa , Meta meta , DBEnumBuildModeType mode , boolean readOnly ) {
		return( checkAccessProductAction( action , sa , meta.findProduct() , null , mode , readOnly ) );
	}
	
	public boolean checkAccessProductAction( ActionBase action , SecurityAction sa , Meta meta , String envName , boolean readOnly ) {
		ProductEnvs envs = meta.getEnviroments();
		MetaEnv env = envs.findMetaEnv( envName );
		return( checkAccessProductAction( action , sa , meta.findProduct() , env , DBEnumBuildModeType.UNKNOWN , readOnly ) );
	}
	
	public boolean checkAccessProductAction( ActionBase action , SecurityAction sa , AppProduct product , String envName , boolean readOnly ) {
		try {
			Meta meta = product.getMeta( action );
			ProductEnvs envs = meta.getEnviroments();
			MetaEnv env = envs.findMetaEnv( envName );
			return( checkAccessProductAction( action , sa , product , env , DBEnumBuildModeType.UNKNOWN , readOnly ) );
		}
		catch( Throwable e ) {
			action.log( "invalid product", e );
			return( false );
		}
	}
	
	public boolean checkAccessProductAction( ActionBase action , SecurityAction sa , AppProduct product , DBEnumBuildModeType mode , boolean readOnly ) {
		return( checkAccessProductAction( action , sa , product , null , mode , readOnly ) );
	}
	
	public boolean checkAccessProductAction( ActionBase action , SecurityAction sa , Meta meta , MetaEnv env , boolean readOnly ) {
		return( checkAccessProductAction( action , sa , meta.findProduct() , env , null , readOnly ) );
	}
	
	public boolean checkAccessProductAction( ActionBase action , SecurityAction sa , MetaEnv env , boolean readOnly ) {
		return( checkAccessProductAction( action , sa , env.meta.findProduct() , env , null , readOnly ) );
	}
	
	public boolean checkAccessProductAction( ActionBase action , SecurityAction sa , AppProduct product , MetaEnv env , DBEnumBuildModeType mode , boolean readOnly ) {
		SessionSecurity security = action.actionInit.session.getSecurity();
		if( security.isAdminAny() )
			return( true );
		
		AuthRoleSet roles = security.getProductRoles( product.ID );
		DBEnumEnvType envtype = ( env == null )? DBEnumEnvType.UNKNOWN : env.ENV_TYPE;
		
		if( sa == SecurityAction.ACTION_SECURED ) {
			if( env == null ) {
				if( roles.secRel )
					return( true );
			}
			else {
				if( ( roles.secDev && envtype == DBEnumEnvType.DEVELOPMENT ) || 
					( roles.secRel && envtype == DBEnumEnvType.UAT ) || 
					( roles.secTest && envtype == DBEnumEnvType.DEVELOPMENT ) || 
					( roles.secTest && envtype == DBEnumEnvType.UAT ) || 
					( roles.secOpr && envtype == DBEnumEnvType.PRODUCTION ) )
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
					if( ( roles.secDev && envtype == DBEnumEnvType.DEVELOPMENT ) || 
						( roles.secRel && envtype == DBEnumEnvType.UAT ) || 
						( roles.secOpr && envtype == DBEnumEnvType.PRODUCTION ) )
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
				if( ( roles.secDev && envtype == DBEnumEnvType.DEVELOPMENT ) || 
					( roles.secRel && envtype == DBEnumEnvType.UAT ) || 
					( roles.secTest && envtype == DBEnumEnvType.DEVELOPMENT ) || 
					( roles.secTest && envtype == DBEnumEnvType.UAT ) || 
					( roles.secOpr && envtype == DBEnumEnvType.PRODUCTION ) )
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
	
	public boolean checkAccessNetworkAction( ActionBase action , SecurityAction sa , int networkId , boolean configure , boolean allocate ) {
		SessionSecurity security = action.actionInit.session.getSecurity();
		if( security.isAdminAny() )
			return( true );
		
		AuthRoleSet roles = security.getNetworkRoles( networkId );
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
		if( security.isAdminAny() )
			return( true );
		
		return( security.checkSpecial( sr ) );
	}
	
	public boolean doLogin( SessionSecurity security , String password ) {
		AuthUser user = security.getUser();
		String username = user.NAME;
		
		try {
			if( user.LOCAL ) {
				String authKey = getAuthKey( AUTH_GROUP_USER , username );
				AuthContext ac = loadAuthData( authKey );
				if( !ac.PUBLICKEY.isEmpty() ) {
			        String checkMessage = ClientAuth.getCheckMessage( username );
					if( ClientAuth.verifySigned( checkMessage , password , ac.PUBLICKEY ) ) {
						security.setContext( ac );
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
						security.setContext( ac );
						engine.trace( "successful login using password: user=" + username );
						return( true );
					}
					
					engine.trace( "unsuccessful login using password: user=" + username );
					return( false );
				}
			}
			else {
				ldapSettings.start();
				
				SearchResult res = ldapSettings.verifyLogin( username , password );
				if( res == null ) {
					engine.trace( "unsuccessful login using password: user=" + username );
					return( false );
				}
				
				engine.trace( "successful login using password: user=" + username );
				return( true );
			}
		}
		catch( Throwable e ) {
			engine.log( "unsuccessful login: user=" + username , e );
			return( false );
		}
		
		engine.trace( "unknown authentification type: user=" + username );
		return( false );
	}

	public void unloadAll() throws Exception {
		mapLocalUsers.clear();
		mapLocalUsersById.clear();
		mapLdapUsers.clear();
		mapLdapUsersById.clear();
		groups.clear();
		groupsById.clear();
		ldapSettings = new AuthLdap( this ); 
	}

	public void setLdapSettings( ObjectProperties props ) throws Exception {
		ldapSettings.setLdapSettings( props );
	}
	
	public ObjectProperties getLdapSettings() {
		return( ldapSettings.getLdapSettings() );
	}

	public String[] getUserNames() {
		Map<String,AuthUser> users = new HashMap<String,AuthUser>();
		users.putAll( mapLocalUsers );
		users.putAll( mapLdapUsers );
		return( Common.getSortedKeys( users ) );
	}

	public Integer getUserId( String name ) throws Exception {
		if( name == null || name.isEmpty() )
			return( null );
		AuthUser user = getUser( name );
		return( user.ID );
	}

	public MatchItem getUserMatchItem( String name ) throws Exception {
		Integer value = getUserId( name );
		return( MatchItem.create( value ) );
	}

	public String findUserName( MatchItem item ) {
		AuthUser user = findUser( item );
		if( user == null )
			return( null );
		return( user.NAME );
	}
	
	public String getUserName( MatchItem item ) throws Exception {
		if( item == null )
			return( "" );
		AuthUser user = findUser( item );
		if( user == null )
			Common.exitUnexpected();
		return( user.NAME );
	}
	
}
