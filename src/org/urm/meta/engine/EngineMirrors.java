package org.urm.meta.engine;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.urm.action.ActionBase;
import org.urm.common.Common;
import org.urm.common.ConfReader;
import org.urm.engine.Engine;
import org.urm.engine.EngineTransaction;
import org.urm.engine.storage.LocalFolder;
import org.urm.engine.storage.UrmStorage;
import org.urm.engine.vcs.GenericVCS;
import org.urm.engine.vcs.MirrorCase;
import org.urm.meta.EngineObject;
import org.urm.meta.ProductMeta;
import org.urm.meta.product.MetaSourceProject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class EngineMirrors extends EngineObject {

	public EngineRegistry registry;
	public Engine engine;

	Map<String,EngineMirrorRepository> repoMap;

	public EngineMirrors( EngineRegistry registry ) {
		super( registry );
		this.registry = registry;
		this.engine = registry.loader.engine;
		
		repoMap = new HashMap<String,EngineMirrorRepository>();
	}

	@Override
	public String getName() {
		return( "server-mirrors" );
	}
	
	public EngineMirrors copy() throws Exception {
		EngineMirrors r = new EngineMirrors( registry );
		
		for( EngineMirrorRepository repo : repoMap.values() ) {
			EngineMirrorRepository rc = repo.copy( r );
			r.addRepository( rc );
		}
		return( r );
	}
	
	public void addRepository( EngineMirrorRepository repo ) {
		repoMap.put( repo.NAME , repo );
	}
	
	public Map<String,EngineMirrorRepository> getRepositories() {
		return( repoMap );
	}
	
	public EngineMirrorRepository getRepository( String name ) {
		return( repoMap.get( name ) );
	}
	
	private EngineMirrorRepository findRepository( String name ) {
		return( repoMap.get( name ) );
	}
	
	public EngineMirrorRepository findServerRepository() {
		return( findRepository( "core" ) );
	}

	public EngineMirrorRepository findProjectRepository( MetaSourceProject project ) {
		String name = "project-" + project.meta.name + "-" + project.NAME;
		return( findRepository( name ) );
	}
	
	public EngineMirrorRepository findProductMetaRepository( ProductMeta meta ) {
		String name = "product-" + meta.name + "-meta";
		return( findRepository( name ) );
	}
	
	public EngineMirrorRepository findProductDataRepository( ProductMeta meta ) {
		String name = "product-" + meta.name + "-data";
		return( findRepository( name ) );
	}
	
	public void load( Node root ) throws Exception {
		if( root == null )
			return;
		
		Node[] list = ConfReader.xmlGetChildren( root , "repository" );
		if( list == null )
			return;
		
		for( Node node : list ) {
			EngineMirrorRepository repo = new EngineMirrorRepository( this );
			repo.load( node );
			addRepository( repo );
		}
	}

	public void save( Document doc , Element root ) throws Exception {
		for( EngineMirrorRepository repo : repoMap.values() ) {
			Element resElement = Common.xmlCreateElement( doc , root , "repository" );
			repo.save( doc , resElement );
		}
	}

	public void addProductMirrors( EngineTransaction transaction , Product product , boolean forceClear ) throws Exception {
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
		EngineMirrorRepository meta = new EngineMirrorRepository( this );
		String name = "product-" + product.NAME + "-meta";
 		meta.createProductMeta( transaction , product , name );
 		addRepository( meta );
 		
 		// conf
		EngineMirrorRepository conf = new EngineMirrorRepository( this );
		name = "product-" + product.NAME + "-data";
		conf.createProductData( transaction , product , name );
 		addRepository( conf );
	}

	public void createProjectMirror( EngineTransaction transaction , MetaSourceProject project ) throws Exception {
		EngineMirrorRepository repo = new EngineMirrorRepository( this );
		String name = "project-" + project.meta.name + "-" + project.NAME;
		repo.createProjectSource( transaction , project , name );
 		addRepository( repo );
	}
	
	public void deleteProductResources( EngineTransaction transaction , Product product , boolean fsDeleteFlag , boolean vcsDeleteFlag , boolean logsDeleteFlag ) throws Exception {
		List<EngineMirrorRepository> repos = new LinkedList<EngineMirrorRepository>();
		for( EngineMirrorRepository repo : repoMap.values() ) {
			if( repo.PRODUCT.equals( product.NAME ) )
				repos.add( repo );
		}
		
		for( EngineMirrorRepository repo : repos ) {
			repo.dropMirror( transaction , vcsDeleteFlag );
			repoMap.remove( repo.NAME );
		}
	}

	public void changeProjectMirror( EngineTransaction transaction , MetaSourceProject project ) throws Exception {
		deleteProjectMirror( transaction , project );
		createProjectMirror( transaction , project );
	}
	
	public void deleteProjectMirror( EngineTransaction transaction , MetaSourceProject project ) throws Exception {
		EngineMirrorRepository repoOld = findProjectRepository( project );
		if( repoOld != null ) {
			repoOld.dropMirror( transaction , false );
			repoMap.remove( repoOld.NAME );
		}
	}

	public void dropResourceMirrors( EngineTransaction transaction , EngineAuthResource res ) throws Exception {
		ActionBase action = transaction.getAction();
		GenericVCS vcs = GenericVCS.getVCS( action , null , res.NAME );
		MirrorCase mc = vcs.getMirror( null );
		mc.removeResourceFolder();

		for( EngineMirrorRepository repo : repoMap.values() ) {
			if( repo.RESOURCE.equals( res.NAME ) )
				repo.clearMirror( transaction );
		}
	}

}
