package org.urm.engine;

import java.io.File;

import org.urm.common.Common;
import org.urm.common.PropertySet;
import org.urm.meta.ServerObject;

public class ServerAuth extends ServerObject {

	ServerEngine engine;

	public static String AUTH_GROUP_RESOURCE = "resource"; 
	public static String AUTH_GROUP_USER = "user"; 
	
	public ServerAuth( ServerEngine engine ) {
		super( null );
		this.engine = engine;
	}
	
	public void init() throws Exception {
		// create initial admin user
		String authKey = getAuthKey( AUTH_GROUP_USER , "admin" );
		String authPath = getAuthFile( authKey );
		
		File authDir = new File( getAuthDir() );
		if( !authDir.isDirectory() )
			authDir.mkdir();
		
		File authFile = new File( authPath );
		if( !authFile.isFile() ) {
			ServerAuthContext ac = new ServerAuthContext( this );
			ac.adminContext = true;
			ac.USER = "admin";
			ac.PASSWORDSAVE = Common.getMD5( "123" );
			ac.METHOD = ServerAuthContext.METHOD_USER;
			ac.createProperties();
			saveAuthData( authKey , ac );
		}
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
		PropertySet props = new PropertySet( "authile" , null );
		String filePath = getAuthFile( authKey );
		props.loadFromPropertyFile( filePath , engine.execrc );
		props.finishRawProperties();
		
		ServerAuthContext ac = new ServerAuthContext( this );
		ac.load( props );
		return( ac );
	}

	public void deleteGroupData( String group ) throws Exception {
	}
	
	public void saveAuthData( String authKey , ServerAuthContext ac ) throws Exception {
		String filePath = getAuthFile( authKey );
		ac.properties.saveToPropertyFile( filePath , engine.execrc );
	}

	public ServerAuthContext connect( String user , String password ) throws Exception {
		String authKey = getAuthKey( AUTH_GROUP_USER , user );
		ServerAuthContext ac = loadAuthData( authKey );
		
		String passwordMD5 = Common.getMD5( password );
		if( password == null || !passwordMD5.equals( ac.PASSWORDSAVE ) )
			return( null );

		ac.PASSWORDONLINE = password;
		return( ac );
	}
	
}
