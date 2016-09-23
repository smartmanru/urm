package org.urm.engine;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.urm.action.ActionBase;
import org.urm.common.Common;
import org.urm.common.ConfReader;
import org.urm.engine.meta.MetaSourceProject;
import org.urm.engine.storage.LocalFolder;
import org.urm.engine.storage.UrmStorage;
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
			r.addRepository( rc );
		}
		return( r );
	}
	
	public void addRepository( ServerMirrorRepository repo ) {
		repoMap.put( repo.NAME , repo );
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
		String name = "project-" + project.meta.name + "-" + project.PROJECT;
		return( findRepository( name ) );
	}
	
	public ServerMirrorRepository findProductMetaRepository( ServerProductMeta meta ) {
		String name = "product-" + meta.name + "-meta";
		return( findRepository( name ) );
	}
	
	public ServerMirrorRepository findProductConfigurationRepository( ServerProductMeta meta ) {
		String name = "product-" + meta.name + "-conf";
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

			addRepository( repo );
		}
	}

	public void save( Document doc , Element root ) throws Exception {
		for( ServerMirrorRepository repo : repoMap.values() ) {
			Element resElement = Common.xmlCreateElement( doc , root , "repository" );
			repo.save( doc , resElement );
		}
	}

	public void addProductMirrors( ServerTransaction transaction , ServerProduct product , boolean forceClear ) throws Exception {
		ActionBase action = transaction.getAction();
		UrmStorage storage = action.artefactory.getUrmStorage();

		LocalFolder products = storage.getServerProductsFolder( action );
		LocalFolder productfolder = products.getSubFolder( action , product.PATH );
		if( productfolder.checkExists( action ) )  {
			if( !forceClear ) {
				String path = action.getLocalPath( productfolder.folderPath );
				action.exit1( _Error.ProductPathAlreadyExists1 , "Product path already exists - " + path , path );
			}
			productfolder.removeThis( action );
		}
			
		productfolder.ensureExists( action );
		
		// meta
		ServerMirrorRepository meta = new ServerMirrorRepository( this );
		String name = "product-" + product.NAME + "-meta";
 		meta.createProductMeta( transaction , product , name );
 		addRepository( meta );
 		
 		// conf
		ServerMirrorRepository conf = new ServerMirrorRepository( this );
		name = "product-" + product.NAME + "-conf";
		conf.createProductConf( transaction , product , name );
 		addRepository( conf );
	}

	public void addProjectMirror( ServerTransaction transaction , MetaSourceProject project ) throws Exception {
		ServerMirrorRepository repo = new ServerMirrorRepository( this );
		String name = "project-" + project.meta.name + "-" + project.PROJECT;
		repo.createProjectSource( transaction , project , name );
 		addRepository( repo );
	}
	
	public void deleteProductResources( ServerTransaction transaction , ServerProduct product , boolean fsDeleteFlag , boolean vcsDeleteFlag , boolean logsDeleteFlag ) throws Exception {
		List<ServerMirrorRepository> repos = new LinkedList<ServerMirrorRepository>();
		for( ServerMirrorRepository repo : repoMap.values() ) {
			if( repo.PRODUCT.equals( product.NAME ) )
				repos.add( repo );
		}
		
		for( ServerMirrorRepository repo : repos ) {
			repo.dropMirror( transaction );
			repoMap.remove( repo.NAME );
		}
	}
	
}
