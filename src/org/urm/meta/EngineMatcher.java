package org.urm.meta;

import org.urm.common.RunContext;
import org.urm.db.DBConnection;
import org.urm.db.engine.DBEngineMirrors;
import org.urm.db.product.DBMeta;
import org.urm.db.product.DBMetaSources;
import org.urm.engine.Engine;
import org.urm.meta.engine.AppProduct;
import org.urm.meta.engine.AppSystem;
import org.urm.meta.engine.AuthResource;
import org.urm.meta.engine.EngineDirectory;
import org.urm.meta.engine.EngineMirrors;
import org.urm.meta.engine.EngineResources;
import org.urm.meta.engine.MirrorRepository;
import org.urm.meta.product.MetaSources;
import org.urm.meta.product.MetaSourceProject;

public class EngineMatcher {

	public EngineLoader loader;
	public Engine engine;
	public RunContext execrc;

	public EngineMatcher( EngineLoader loader ) {
		this.loader = loader;
		this.engine = loader.engine;
		this.execrc = engine.execrc;
	}

	public void prepareMatchDirectory() {
		EngineMirrors mirrors = loader.getMirrors();
		mirrors.clearProductReferences();
	}
	
	public void prepareMatchSystem( AppSystem system , boolean update , boolean useOldMatch ) throws Exception {
	}
	
	public void prepareMatchProduct( AppProduct product , boolean update , boolean useOldMatch ) throws Exception {
	}
	
	public void doneSystem( AppSystem system ) throws Exception {
		EngineDirectory directory = loader.getDirectory();
		if( system.MATCHED )
			directory.addSystem( system );
	}
	
	public void doneProduct( AppProduct product , ProductMeta set ) throws Exception {
		EngineDirectory directory = loader.getDirectory();
		directory.addProduct( product );
		product.setStorage( set );
	}

	public boolean matchProductMirrors( AppProduct product ) {
		boolean ok = true;
		
		EngineMirrors mirrors = loader.getMirrors();
		MirrorRepository repo = mirrors.findProductMetaRepository( product.NAME );
		if( repo != null )
			repo.setProduct( product.ID );
		else {
			loader.trace( "missing mirror product meta repository" );
			ok = false;
		}
		
		repo = mirrors.findProductDataRepository( product.NAME );
		if( repo != null )
			repo.setProduct( product.ID );
		else {
			loader.trace( "missing mirror product data repository" );
			ok = false;
		}
		
		return( ok );
	}
	
	public boolean matchProjectMirrors( AppProduct product , MetaSources sources , boolean update ) throws Exception {
		boolean ok = true;
		
		EngineMirrors mirrors = loader.getMirrors();
		
		for( MetaSourceProject project : sources.getAllProjectList( false ) ) {
			MirrorRepository repo = mirrors.findProjectRepository( project );
			if( repo != null ) {
				if( update ) {
					Integer resId = repo.RESOURCE_ID;
					if( !project.MIRROR_RESOURCE.isEmpty() ) {
						EngineResources resources = loader.getResources();
						AuthResource res = resources.findResource( project.MIRROR_RESOURCE );
						if( res == null ) {
							loader.trace( "missing expected resource, project=" + project.NAME + ", resource=" + project.MIRROR_RESOURCE );
							return( false );
						}
						
						resId = res.ID;
					}
					
					repo.setProductProject( product.ID , project.ID );
					repo.setMirror( resId , project.MIRROR_REPOSITORY , project.MIRROR_REPOPATH , project.MIRROR_CODEPATH );
					project.setMirror( repo );
					
					DBConnection c = loader.getConnection();
					DBEngineMirrors.modifyRepository( c , repo , false );
					
					ProductMeta storage = sources.meta.getStorage();
					DBMetaSources.modifyProject( c , storage , project , false );
				}
				else {
					repo.setProductProject( product.ID , project.ID );
					project.setMirror( repo );
				}
			}
			else {
				loader.trace( "missing mirror product project=" + project.NAME + " repository" );
				ok = false;
			}
		}
		
		return( ok );
	}

	public void matchProductUpdateStatus( AppProduct product , ProductMeta set , boolean update , boolean matched ) {
		if( matched != set.MATCHED ) {
			try {
				if( update )
					DBMeta.setMatched( loader , set , matched );
				set.setMatched( matched );
			}
			catch( Throwable e ) {
				loader.log( "update match status" , e );
			}
		}
	}
	
}
