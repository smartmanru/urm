package org.urm.server;

import org.urm.common.PropertySet;
import org.urm.server.action.ActionBase;
import org.urm.server.vcs.GenericVCS;
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
	public String RESOURCE_DATA;
	public String BRANCH;
	
	public static String TYPE_SERVER = "server";
	public static String TYPE_PROJECT = "project";
	public static String TYPE_PRODUCT = "product";

	public ServerMirrorRepository( ServerMirror mirror ) {
		this.mirror = mirror;
		
		loaded = false;
		loadFailed = false;
	}

	public String getFolderName() {
		return( NAME );
	}
	
	public boolean isServer() {
		return( TYPE.equals( TYPE_SERVER ) );
	}
	
	public boolean isProject() {
		return( TYPE.equals( TYPE_PROJECT ) );
	}
	
	public boolean isProduct() {
		return( TYPE.equals( TYPE_PRODUCT ) );
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
		RESOURCE_DATA = properties.getSystemStringProperty( "datapath" , "" );
		BRANCH = properties.getSystemStringProperty( "branch" , "" );
	}

	public void createProperties() throws Exception {
		properties = new PropertySet( "mirror" , null );
		properties.setStringProperty( "name" , NAME );
		properties.setStringProperty( "type" , TYPE );
		properties.setStringProperty( "resource" , RESOURCE );
		properties.setStringProperty( "repository" , RESOURCE_REPO );
		properties.setStringProperty( "rootpath" , RESOURCE_ROOT );
		properties.setStringProperty( "datapath" , RESOURCE_DATA );
		properties.setStringProperty( "branch" , BRANCH );
	}

	public void publishRepository( ServerTransaction transaction , String resource , String reponame , String reporoot , String dataroot , String repobranch ) throws Exception {
		RESOURCE = resource;
		RESOURCE_REPO = reponame;
		RESOURCE_ROOT = ( reporoot.isEmpty() )? "/" : reporoot;
		RESOURCE_DATA = ( dataroot.isEmpty() )? "/" : dataroot;
		BRANCH = "";
		
		try {
			if( isServer() )
				publishServer( transaction );
		}
		catch( Throwable e ) {
			transaction.log( "publishRepository" , e );
			RESOURCE = "";
			RESOURCE_REPO = "";
			RESOURCE_ROOT = "";
			RESOURCE_DATA = "";
			BRANCH = "";
			transaction.exit( "unable to publish repository" );
		}
		
		createProperties();
		mirror.registry.loader.saveMirrors( transaction );
	}
	
	public void publishServer( ServerTransaction transaction ) throws Exception {
		// reject already published
		// server: test target, remove mirror work/repo, create mirror work/repo, publish target
		ActionBase action = transaction.getAction();
		GenericVCS vcs = GenericVCS.getVCS( action , RESOURCE , false );
		vcs.createRemoteBranchMirror( this );
		
		if( !vcs.checkTargetEmpty( this ) )
			action.exit( "unable to publish to non-empty repository" );
	}
	
	public void dropMirror( ServerTransaction transaction ) throws Exception {
		if( isServer() )
			dropServerMirror( transaction );
		
		RESOURCE = "";
		RESOURCE_REPO = "";
		RESOURCE_ROOT = "";
		RESOURCE_DATA = "";
		BRANCH = "";
		createProperties();
		mirror.registry.loader.saveMirrors( transaction );
	}
	
	public void dropServerMirror( ServerTransaction transaction ) throws Exception {
		GenericVCS vcs = GenericVCS.getVCS( transaction.getAction() , RESOURCE , false );
		vcs.dropRemoteBranchMirror( this );
	}

	public void pushMirror( ServerTransaction transaction ) throws Exception {
		if( isServer() )
			pushServerMirror( transaction );
	}

	public void pushServerMirror( ServerTransaction transaction ) throws Exception {
		GenericVCS vcs = GenericVCS.getVCS( transaction.getAction() , RESOURCE , false );
		vcs.pushRemoteBranchMirror( this );
	}
	
	public void refreshMirror( ServerTransaction transaction ) throws Exception {
		if( isServer() )
			refreshServerMirror( transaction );
	}

	public void refreshServerMirror( ServerTransaction transaction ) throws Exception {
		GenericVCS vcs = GenericVCS.getVCS( transaction.getAction() , RESOURCE , false );
		vcs.refreshRemoteBranchMirror( this );
	}
	
}
