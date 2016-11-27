package org.urm.engine;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.urm.action.ActionBase;
import org.urm.action.ActionCore;
import org.urm.common.Common;
import org.urm.common.ConfReader;
import org.urm.common.PropertySet;
import org.urm.common.RunContext;
import org.urm.meta.ServerObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class ServerAuth extends ServerObject {

	ServerEngine engine;

	public static String AUTH_GROUP_RESOURCE = "resource"; 
	public static String AUTH_GROUP_USER = "user"; 
	
	public static String MASTER_ADMIN = "admin";
	
	Map<String,ServerAuthUser> localUsers;
	Map<String,ServerAuthGroup> groups;
	
	public ServerAuth( ServerEngine engine ) {
		super( null );
		this.engine = engine;
		localUsers = new HashMap<String,ServerAuthUser>();
		groups = new HashMap<String,ServerAuthGroup>(); 
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
	
	public ServerAuthContext loadAuthData( ActionBase action , String authKey ) throws Exception {
		PropertySet props = new PropertySet( "authfile" , null );
		String filePath = getAuthFile( authKey );
		
		if( action.shell.checkFileExists( action , filePath ) )
			props.loadFromPropertyFile( filePath , engine.execrc , false );
		props.finishRawProperties();
		
		ServerAuthContext ac = new ServerAuthContext( this );
		ac.load( props );
		return( ac );
	}

	public void deleteGroupData( String group ) throws Exception {
	}
	
	public void saveAuthData( String authKey , ServerAuthContext ac ) throws Exception {
		String filePath = getAuthFile( authKey );
		ac.properties.saveToPropertyFile( filePath , engine.execrc , false );
	}

	public ServerAuthUser connect( String username , String password ) throws Exception {
		ServerAuthUser user = getUser( username );
		if( user == null )
			return( null );
		
		ServerAuthContext ac = user.getContext();
		if( ac == null ) {
			String authKey = getAuthKey( AUTH_GROUP_USER , username );
			ac = loadAuthData( engine.serverAction , authKey );
			user.setContext( ac );
		}
			
		String passwordMD5 = Common.getMD5( password );
		if( password == null || !passwordMD5.equals( ac.PASSWORDSAVE ) )
			return( null );
	
		ac.PASSWORDONLINE = password;
		return( user );
	}

	public String[] getLocalUserList() {
		return( Common.getSortedKeys( localUsers ) );
	}
	
	public String[] getLdapUserList() {
		return( new String[0] );
	}

	public ServerAuthUser getLocalUserData( String username ) {
		return( localUsers.get( username ) );
	}
	
	public ServerAuthUser getLdapUserData( String username ) {
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
	}

	public String[] getGroupNames() {
		return( Common.getSortedKeys( groups ) );
	}

	public ServerAuthGroup getGroup( String groupName ) {
		return( groups.get( groupName ) );
	}
	
}
