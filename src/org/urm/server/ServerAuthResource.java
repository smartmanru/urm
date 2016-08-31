package org.urm.server;

import org.urm.common.PropertySet;
import org.urm.server.action.ActionBase;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class ServerAuthResource {

	public ServerResources resources;

	private boolean loaded;
	public boolean loadFailed;

	PropertySet properties;
	
	public String NAME;
	public String TYPE;
	public String BASEURL;
	public String DESC;
	public String AUTHKEY;
	
	public String METHOD;
	public String USER;
	public String PASSWORD;

	public static String METHOD_ANONYMOUS = "anonymous"; 
	public static String METHOD_COMMON = "common"; 
	public static String METHOD_USER = "user"; 
	
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
		r.METHOD = METHOD;
		r.USER = USER;
		r.PASSWORD = PASSWORD;
		return( r );
	}
	
	public void load( Node node ) throws Exception {
		if( loaded )
			return;

		loaded = true;
		properties = new PropertySet( "resource" , null );
		properties.loadRawFromNodeElements( node );
		
		scatterSystemProperties();
		properties.finishRawProperties();
	}
	
	public void save( Document doc , Element root ) throws Exception {
		properties.saveAsElements( doc , root );
	}
	
	private void scatterSystemProperties() throws Exception {
		NAME = properties.getSystemRequiredStringProperty( "name" );
		TYPE = properties.getSystemRequiredStringProperty( "type" );
		BASEURL = properties.getSystemRequiredStringProperty( "baseurl" );
		DESC = properties.getSystemRequiredStringProperty( "desc" );
		AUTHKEY = properties.getSystemStringProperty( "authkey" , "" );
	}

	public void createProperties() throws Exception {
		properties = new PropertySet( "resource" , null );
		properties.setStringProperty( "name" , NAME );
		properties.setStringProperty( "type" , TYPE );
		properties.setStringProperty( "baseurl" , BASEURL );
		properties.setStringProperty( "desc" , DESC );
		
		AUTHKEY = "resource." + NAME;
		properties.setStringProperty( "authkey" , AUTHKEY );
	}

	public PropertySet getAuthProps() throws Exception {
		PropertySet authProps = new PropertySet( "authkey" , null );
		authProps.setStringProperty( "method" , METHOD );
		authProps.setStringProperty( "user" , USER );
		authProps.setStringProperty( "password" , PASSWORD );
		return( authProps );
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
			transaction.exit( "mismatched resource name on change new name=" + src.NAME );
		
		TYPE = src.TYPE;
		BASEURL = src.BASEURL;
		DESC = src.DESC;
		AUTHKEY = src.AUTHKEY;
		METHOD = src.METHOD;
		USER = src.USER;
		PASSWORD = src.PASSWORD;
		createProperties();
	}
	
	public void loadAuth( ActionBase action ) throws Exception {
	}
	
}
