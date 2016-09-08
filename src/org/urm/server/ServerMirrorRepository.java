package org.urm.server;

import org.urm.common.PropertySet;
import org.urm.server.action.ActionBase;
import org.urm.server.vcs.GenericVCS;
import org.urm.server.vcs.GitMirrorStorage;
import org.urm.server.vcs.GitVCS;
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
	public static String TYPE_PROJECT = "project";
	public static String TYPE_PRODUCT = "product";

	public ServerMirrorRepository( ServerMirror mirror ) {
		this.mirror = mirror;
		
		loaded = false;
		loadFailed = false;
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

	public boolean createServerMirror( ActionBase action , String resource , String reponame , String reporoot ) throws Exception {
		GenericVCS vcs = GenericVCS.getVCS( action , resource , false );
		if( vcs.res.isSvn() ) {
			if( !checkTargetEmpty( action , vcs , reponame , reporoot ) )
				return( false );
			if( !vcs.isValidRepositoryMasterPath( reponame , "/" ) )
				return( false );
		}
		else
		if( vcs.res.isGit() ) {
			createNewLocalGitMirror( action , resource , reponame , reporoot , false );
		}
		
		return( true );
	}
	
	private boolean createNewLocalGitMirror( ActionBase action , String resource , String reponame , String reporoot , boolean bare ) throws Exception {
		GitVCS vcs = GenericVCS.getGitDirect( action , resource );
		GitMirrorStorage storage = vcs.createStorage( NAME , reponame , reporoot , bare );
		if( !storage.isEmpty( action ) )
			return( false );

		storage.createReadMe( action , NAME );
		return( true );
	}
	
	public boolean checkTargetEmpty( ActionBase action , GenericVCS vcs , String reponame , String reporoot ) throws Exception {
		String[] items = vcs.listMasterItems( reponame , reporoot );
		if( items.length == 0 )
			return( true );
		return( false );
	}

	public void publishRepository( ServerTransaction transaction , String resource , String reponame , String reporoot , String repobranch ) throws Exception {
		if( isServer() )
			publishServer( transaction , resource , reponame , reporoot );
		
		RESOURCE = resource;
		RESOURCE_REPO = reponame;
		RESOURCE_ROOT = reporoot;
		BRANCH = "";
		createProperties();
		mirror.registry.loader.saveMirrors( transaction );
	}
	
	public void publishServer( ServerTransaction transaction , String resource , String reponame , String reporoot ) throws Exception {
		// reject already published
		// server: test target, remove mirror work/repo, create mirror work/repo, publish target
		if( !createServerMirror( transaction.getAction() , resource , reponame , reporoot ) )
			transaction.exit( "unable to publish repository" );
	}
	
	public void dropMirror( ServerTransaction transaction ) throws Exception {
		if( isServer() )
			dropServerMirror( transaction );
		
		RESOURCE = "";
		RESOURCE_REPO = "";
		RESOURCE_ROOT = "";
		BRANCH = "";
		createProperties();
		mirror.registry.loader.saveMirrors( transaction );
	}
	
	public void dropServerMirror( ServerTransaction transaction ) throws Exception {
		GenericVCS vcs = GenericVCS.getVCS( transaction.getAction() , RESOURCE , false );
		if( vcs.res.isGit() ) {
			GitVCS git = ( GitVCS )vcs;
			git.removeStorage( NAME , false );
		}
	}

	public void pushMirror( ServerTransaction transaction ) throws Exception {
		if( isServer() )
			pushServerMirror( transaction );
	}

	public void pushServerMirror( ServerTransaction transaction ) throws Exception {
		GenericVCS vcs = GenericVCS.getVCS( transaction.getAction() , RESOURCE , false );
		if( vcs.res.isGit() ) {
			GitVCS git = ( GitVCS )vcs;
			git.pushStorage( NAME , false );
		}
	}
	
	public void refreshMirror( ServerTransaction transaction ) throws Exception {
		if( isServer() )
			refreshServerMirror( transaction );
	}

	public void refreshServerMirror( ServerTransaction transaction ) throws Exception {
		GenericVCS vcs = GenericVCS.getVCS( transaction.getAction() , RESOURCE , false );
		if( vcs.res.isGit() ) {
			GitVCS git = ( GitVCS )vcs;
			git.refreshStorage( NAME , false );
		}
	}
	
}
