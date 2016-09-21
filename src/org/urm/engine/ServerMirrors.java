package org.urm.engine;

import java.util.HashMap;
import java.util.Map;

import org.urm.common.Common;
import org.urm.common.ConfReader;
import org.urm.engine.meta.MetaSourceProject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class ServerMirrors extends ServerObject {

	public ServerRegistry registry;
	public ServerEngine engine;

	Map<String,ServerMirrorRepository> repoMap;

	public ServerMirrors( ServerRegistry registry ) {
		this.registry = registry;
		this.engine = registry.loader.engine;
		
		repoMap = new HashMap<String,ServerMirrorRepository>();
	}

	public ServerMirrors copy() throws Exception {
		ServerMirrors r = new ServerMirrors( registry );
		
		for( ServerMirrorRepository repo : repoMap.values() ) {
			ServerMirrorRepository rc = repo.copy( r );
			r.repoMap.put( rc.NAME , rc );
		}
		return( r );
	}
	
	public Map<String,ServerMirrorRepository> getRepositories() {
		return( repoMap );
	}
	
	public ServerMirrorRepository getRepository( String name ) {
		return( repoMap.get( name ) );
	}
	
	private ServerMirrorRepository findRepository( String name ) {
		return( repoMap.get( name ) );
	}
	
	public ServerMirrorRepository findServerRepository() {
		return( findRepository( "core" ) );
	}

	public ServerMirrorRepository findProjectRepository( MetaSourceProject project ) {
		String name = "project-" + project.meta.storage.name + "-" + project.PROJECT;
		return( findRepository( name ) );
	}
	
	public ServerMirrorRepository findProductMetaRepository( ServerProductMeta meta ) {
		String name = "product-" + meta.name + "-conf";
		return( findRepository( name ) );
	}
	
	public ServerMirrorRepository findProductConfigurationRepository( ServerProductMeta meta ) {
		String name = "product-" + meta.name + "-meta";
		return( findRepository( name ) );
	}
	
	public void load( Node root ) throws Exception {
		if( root == null )
			return;
		
		Node[] list = ConfReader.xmlGetChildren( root , "repository" );
		if( list == null )
			return;
		
		for( Node node : list ) {
			ServerMirrorRepository repo = new ServerMirrorRepository( this );
			repo.load( node );

			repoMap.put( repo.NAME , repo );
		}
	}

	public void save( Document doc , Element root ) throws Exception {
		for( ServerMirrorRepository repo : repoMap.values() ) {
			Element resElement = Common.xmlCreateElement( doc , root , "repository" );
			repo.save( doc , resElement );
		}
	}

	public void addProductMirrors( ServerProduct mirror ) throws Exception {
		
	}
	
}
