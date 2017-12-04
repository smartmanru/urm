package org.urm.meta.engine;

import java.util.HashMap;
import java.util.Map;

import org.urm.common.Common;
import org.urm.engine.Engine;
import org.urm.meta.EngineObject;
import org.urm.meta.ProductMeta;
import org.urm.meta.product.MetaSourceProject;

public class EngineMirrors extends EngineObject {

	public Engine engine;

	Map<String,MirrorRepository> repoMap;
	Map<Integer,MirrorRepository> repoMapById;

	public EngineMirrors( Engine engine ) {
		super( null );
		this.engine = engine;
		
		repoMap = new HashMap<String,MirrorRepository>();
		repoMapById = new HashMap<Integer,MirrorRepository>();
	}

	@Override
	public String getName() {
		return( "server-mirrors" );
	}
	
	public EngineMirrors copy() throws Exception {
		EngineMirrors r = new EngineMirrors( engine );
		
		for( MirrorRepository repo : repoMap.values() ) {
			MirrorRepository rc = repo.copy( r );
			r.addRepository( rc );
		}
		return( r );
	}
	
	public void addRepository( MirrorRepository repo ) {
		repoMap.put( repo.NAME , repo );
		repoMapById.put( repo.ID , repo );
	}
	
	public void removeRepository( MirrorRepository repo ) {
		repoMap.remove( repo.NAME );
		repoMapById.remove( repo.ID );
	}
	
	public String[] getRepositoryNames() {
		return( Common.getSortedKeys( repoMap ) );
	}
	
	public MirrorRepository getRepository( String name ) throws Exception {
		MirrorRepository repo = repoMap.get( name );
		if( repo == null )
			Common.exit1( _Error.UnknownMirrorRepository1 , "Unknown mirror repository name=" + name , name );
		return( repo );
	}
	
	public MirrorRepository getRepository( int mirrorId ) throws Exception {
		MirrorRepository repo = repoMapById.get( mirrorId );
		if( repo == null )
			Common.exit1( _Error.UnknownMirrorRepository1 , "Unknown mirror repository id=" + mirrorId , "" + mirrorId );
		return( repo );
	}
	
	public MirrorRepository findRepository( String name ) {
		return( repoMap.get( name ) );
	}
	
	public MirrorRepository findServerRepository() {
		return( findRepository( "core" ) );
	}

	public MirrorRepository findProjectRepository( MetaSourceProject project ) {
		String name = "project-" + project.meta.name + "-" + project.NAME;
		return( findRepository( name ) );
	}
	
	public MirrorRepository findProductMetaRepository( ProductMeta meta ) {
		String name = "product-" + meta.name + "-meta";
		return( findRepository( name ) );
	}
	
	public MirrorRepository findProductDataRepository( ProductMeta meta ) {
		String name = "product-" + meta.name + "-data";
		return( findRepository( name ) );
	}

	public void clearProductReferences() {
		for( MirrorRepository repo : repoMap.values() )
			repo.setProduct( null );
	}
	
}
