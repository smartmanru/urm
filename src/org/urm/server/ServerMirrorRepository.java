package org.urm.server;

import org.urm.common.PropertySet;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class ServerMirrorRepository {

	ServerMirror mirror;
	
	private boolean loaded;
	public boolean loadFailed;

	PropertySet properties;
	
	public String NAME;
	public String TYPE;
	public String RESOURCE;
	public String RESOURCE_REPO;
	public String RESOURCE_ROOT;
	public String BRANCH;
	
	public static String TYPE_SERVER = "server";
	public static String TYPE_PRODUCT = "product";
	public static String TYPE_SOURCE = "source";

	public ServerMirrorRepository( ServerMirror mirror ) {
		this.mirror = mirror;
		
		loaded = false;
		loadFailed = false;
	}
	
	public ServerMirrorRepository copy( ServerMirror mirror ) throws Exception {
		ServerMirrorRepository r = new ServerMirrorRepository( mirror );
		r.properties = properties.copy( null );
		r.scatterSystemProperties();
		return( r );
	}
	
	public void load( Node root ) throws Exception {
		if( loaded )
			return;

		loaded = true;
		properties = new PropertySet( "mirror" , null );
		properties.loadRawFromNodeElements( root );
		
		scatterSystemProperties();
		properties.finishRawProperties();
	}

	public void save( Document doc , Element root ) throws Exception {
		properties.saveAsElements( doc , root );
	}
	
	private void scatterSystemProperties() throws Exception {
		NAME = properties.getSystemRequiredStringProperty( "name" );
		TYPE = properties.getSystemRequiredStringProperty( "type" );
		RESOURCE = properties.getSystemStringProperty( "resource" , "" );
		RESOURCE_REPO = properties.getSystemStringProperty( "repository" , "" );
		RESOURCE_ROOT = properties.getSystemStringProperty( "rootpath" , "" );
		BRANCH = properties.getSystemStringProperty( "branch" , "" );
	}

	public void createProperties() throws Exception {
		properties = new PropertySet( "mirror" , null );
		properties.setStringProperty( "name" , NAME );
		properties.setStringProperty( "type" , TYPE );
		properties.setStringProperty( "resource" , RESOURCE );
		properties.setStringProperty( "repository" , RESOURCE_REPO );
		properties.setStringProperty( "rootpath" , RESOURCE_ROOT );
		properties.setStringProperty( "branch" , BRANCH );
	}

	public void update( ServerTransaction transaction , ServerMirrorRepository src ) throws Exception {
		if( !NAME.equals( src.NAME ) )
			transaction.exit( "mismatched repository name on change new name=" + src.NAME );
		
		TYPE = src.TYPE;
		RESOURCE = src.RESOURCE;
		RESOURCE_ROOT = src.RESOURCE_ROOT;
		RESOURCE_REPO = src.RESOURCE_REPO;
		BRANCH = src.BRANCH;
		createProperties();
	}
	
}
