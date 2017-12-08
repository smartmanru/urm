package org.urm.meta;

import org.urm.common.RunContext;
import org.urm.engine.Engine;
import org.urm.meta.engine.AppProduct;
import org.urm.meta.engine.AppSystem;
import org.urm.meta.engine.EngineDirectory;
import org.urm.meta.engine.EngineMirrors;
import org.urm.meta.engine.MirrorRepository;
import org.urm.meta.product.MetaSource;
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
		product.setMatched( set );
	}

	public void matchProductMirrors( AppProduct product ) {
		EngineMirrors mirrors = loader.getMirrors();
		MirrorRepository repo = mirrors.findProductMetaRepository( product.NAME );
		if( repo != null )
			repo.setProduct( product.ID );
		repo = mirrors.findProductDataRepository( product.NAME );
		if( repo != null )
			repo.setProduct( product.ID );
	}
	
	public void matchProjectMirrors( AppProduct product , MetaSource sources ) {
		EngineMirrors mirrors = loader.getMirrors();
		
		for( MetaSourceProject project : sources.getAllProjectList( false ) ) {
			MirrorRepository repo = mirrors.findProjectRepository( project );
			if( repo != null ) {
				repo.setProductProject( product.ID , 0 );
				project.setMirror( repo.ID );
			}
		}
	}
	
}
