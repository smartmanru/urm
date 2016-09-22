package org.urm.engine;

import org.urm.common.PropertySet;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class ServerAuthResource extends ServerObject {

	public ServerResources resources;

	private boolean loaded;
	public boolean loadFailed;

	PropertySet properties;
	
	public String NAME;
	public String TYPE;
	public String BASEURL;
	public String DESC;
	public String AUTHKEY;
	public ServerAuthContext ac;
	
	public static String TYPE_SVN = "svn";
	public static String TYPE_GIT = "git";
	public static String TYPE_NEXUS = "nexus";
	
	public ServerAuthResource( ServerResources resources ) {
		this.resources = resources;
		loaded = false;
		loadFailed = false;
	}

	public ServerAuthResource copy( ServerResources resources ) throws Exception {
		ServerAuthResource r = new ServerAuthResource( resources );
		r.properties = properties.copy( null );
		r.scatterSystemProperties();
		if( ac != null )
			r.ac = ac.copy();
		return( r );
	}
	
	public void load( Node node ) throws Exception {
		if( loaded )
			return;

		loaded = true;
		properties = new PropertySet( "resource" , null );
		properties.loadFromNodeElements( node );
		
		scatterSystemProperties();
		properties.finishRawProperties();
	}
	
	public void save( Document doc , Element root ) throws Exception {
		properties.saveAsElements( doc , root );
		saveAuthData();
	}
	
	private void scatterSystemProperties() throws Exception {
		NAME = properties.getSystemRequiredStringProperty( "name" );
		TYPE = properties.getSystemRequiredStringProperty( "type" );
		BASEURL = properties.getSystemRequiredStringProperty( "baseurl" );
		DESC = properties.getSystemStringProperty( "desc" , "" );
		AUTHKEY = properties.getSystemStringProperty( "authkey" , "" );
	}

	public void createProperties() throws Exception {
		properties = new PropertySet( "resource" , null );
		properties.setOriginalStringProperty( "name" , NAME );
		properties.setOriginalStringProperty( "type" , TYPE );
		properties.setOriginalStringProperty( "baseurl" , BASEURL );
		properties.setOriginalStringProperty( "desc" , DESC );
		properties.setOriginalStringProperty( "authkey" , AUTHKEY );
	}

	public boolean isSvn() {
		if( TYPE.equals( TYPE_SVN ) )
			return( true );
		return( false );
	}
	
	public boolean isGit() {
		if( TYPE.equals( TYPE_GIT ) )
			return( true );
		return( false );
	}
	
	public boolean isNexus() {
		if( TYPE.equals( TYPE_NEXUS ) )
			return( true );
		return( false );
	}

	public void updateResource( ServerTransaction transaction , ServerAuthResource src ) throws Exception {
		if( !NAME.equals( src.NAME ) )
			transaction.exit( _Error.TransactionResourceOld1 , "mismatched resource name on change new name=" + src.NAME , new String[] { src.NAME } );
		
		TYPE = src.TYPE;
		BASEURL = src.BASEURL;
		DESC = src.DESC;
		
		ServerAuth auth = resources.engine.getAuth();
		ac = new ServerAuthContext( auth );
		ac.load( src.ac.properties );
		createProperties();
	}
	
	public void saveAuthData() throws Exception {
		ServerAuth auth = resources.engine.getAuth();
		AUTHKEY = auth.getAuthKey( ServerAuth.AUTH_GROUP_RESOURCE , NAME );
		properties.setOriginalStringProperty( "authkey" , AUTHKEY );
		
		if( ac != null )
			auth.saveAuthData( AUTHKEY , ac ); 
	}
	
	public void loadAuthData() throws Exception {
		if( ac != null )
			return;
		ServerAuth auth = resources.engine.getAuth();
		ac = auth.loadAuthData( AUTHKEY );
	}

	public void createResource() throws Exception {
		ServerAuth auth = resources.engine.getAuth();
		AUTHKEY = auth.getAuthKey( ServerAuth.AUTH_GROUP_RESOURCE , NAME );
		createProperties();
	}

}
