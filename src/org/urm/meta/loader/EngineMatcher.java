package org.urm.meta.loader;

import org.urm.common.RunContext;
import org.urm.db.DBConnection;
import org.urm.db.engine.DBEngineMirrors;
import org.urm.db.env.DBMetaEnv;
import org.urm.db.product.DBMeta;
import org.urm.db.product.DBMetaSources;
import org.urm.db.system.DBAppSystem;
import org.urm.engine.Engine;
import org.urm.engine.data.EngineDirectory;
import org.urm.engine.data.EngineMirrors;
import org.urm.engine.data.EngineResources;
import org.urm.engine.properties.PropertyEntity;
import org.urm.meta.engine.AppProduct;
import org.urm.meta.engine.AppSystem;
import org.urm.meta.engine.AuthResource;
import org.urm.meta.engine.MirrorRepository;
import org.urm.meta.env.MetaEnv;
import org.urm.meta.loader.Types.EnumModifyType;
import org.urm.meta.product.MetaSources;
import org.urm.meta.product.MetaSourceProject;
import org.urm.meta.product.ProductMeta;

public class EngineMatcher {

	public EngineLoader loader;
	public Engine engine;
	public RunContext execrc;

	protected ProductMeta matchStorage;
	protected MetaEnv matchEnv;
	
	protected String matchValueInitial;
	protected int matchOwnerId;
	protected PropertyEntity matchItemEntity;
	protected String matchItemProperty;
	protected String matchItemIndex;
	
	public EngineMatcher( EngineLoader loader ) {
		this.loader = loader;
		this.engine = loader.engine;
		this.execrc = engine.execrc;
	}

	public void prepareMatchDirectory() {
		EngineMirrors mirrors = loader.getMirrors();
		mirrors.clearProductReferences();
	}
	
	public void matchSystem( EngineLoader loader , EngineDirectory directory , AppSystem system , boolean update ) {
		try {
			if( update ) {
				prepareMatchSystem( system , true , true );
				DBAppSystem.matchSystem( loader , directory , system , update );
			}
			else
			if( system.MATCHED ) {
				prepareMatchSystem( system , false , true );
				DBAppSystem.matchSystem( loader , directory , system , update );
			}
			
			doneSystem( system );
		}
		catch( Throwable e ) {
			loader.log( "match problem " , e );
			return;
		}
	}
	
	public boolean matchProductMirrors( AppProduct product ) {
		boolean ok = true;
		
		EngineMirrors mirrors = loader.getMirrors();
		MirrorRepository repo = mirrors.findProductMetaRepository( product.NAME );
		if( repo != null )
			repo.setProduct( product.ID );
		else {
			loader.trace( "missing mirror product meta repository, product=" + product.NAME );
			ok = false;
		}
		
		repo = mirrors.findProductDataRepository( product.NAME );
		if( repo != null )
			repo.setProduct( product.ID );
		else {
			loader.trace( "missing mirror product data repository, product=" + product.NAME );
			ok = false;
		}
		
		return( ok );
	}
	
	public boolean matchProduct( EngineLoader loader , AppProduct product , ProductMeta set , boolean update ) {
		// product meta
		try {
			prepareMatchProduct( product , false , false );

			boolean matched = true;
			if( !matchProjectMirrors( product , set.getSources() , update ) )
				matched = false;
			
			if( !set.isMatched() )
				matched = false;
			
			if( !matched ) {
				matchProductUpdateStatus( set , false , true );
				return( false );
			}
			
			doneProduct( product , set );
		}
		catch( Throwable e ) {
			loader.log( "match problem " , e );
			matchProductUpdateStatus( set , false , true );
			return( false );
		}
		
		matchProductUpdateStatus( set , true , true );
		return( true );
	}
	
	private void prepareMatchSystem( AppSystem system , boolean update , boolean useOldMatch ) throws Exception {
	}
	
	private void prepareMatchProduct( AppProduct product , boolean update , boolean useOldMatch ) throws Exception {
	}
	
	private void prepareMatchEnv( MetaEnv env , boolean update , boolean useOldMatch ) throws Exception {
	}
	
	private void doneSystem( AppSystem system ) throws Exception {
		EngineDirectory directory = loader.getDirectory();
		if( system.MATCHED )
			directory.addSystem( system );
	}
	
	private void doneProduct( AppProduct product , ProductMeta set ) throws Exception {
	}

	private boolean matchProjectMirrors( AppProduct product , MetaSources sources , boolean update ) throws Exception {
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
					DBMetaSources.modifyProject( c , storage , project , false , EnumModifyType.MATCH );
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

	private void matchProductUpdateStatus( ProductMeta set , boolean matched , boolean finish ) {
		try {
			if( !matched )
				set.setMatched( false );
			if( finish )
				DBMeta.setMatched( loader , set , matched );
		}
		catch( Throwable e ) {
			loader.log( "update match status" , e );
		}
	}

	public String matchProductBefore( ProductMeta storage , String value , int ownerId , PropertyEntity entity , String prop , String index ) throws Exception {
		this.matchStorage = storage;
		this.matchValueInitial = value;
		this.matchOwnerId = ownerId;
		this.matchItemEntity = entity;
		this.matchItemProperty = prop;
		this.matchItemIndex = index;
		return( value );
	}

	public void matchProductDone( MatchItem item ) throws Exception {
		matchProductDone( item , matchStorage , matchOwnerId , matchItemEntity , matchItemProperty , matchItemIndex );
	}

	public void matchProductDone( MatchItem item , ProductMeta storage , int ownerId , PropertyEntity entity , String prop , String index ) throws Exception {
		if( item != null && !item.MATCHED ) {
			engine.error( "product match failed: object=" + ownerId + ", entity=" + entity.PARAMENTITY_TYPE.name() + ", property=" + prop + ", value=" + matchValueInitial );
			matchProductUpdateStatus( matchStorage , false , false );
		}
	}
	
	public String matchEnvBefore( MetaEnv env , String value , int ownerId , PropertyEntity entity , String prop , String index ) throws Exception {
		this.matchEnv = env;
		this.matchValueInitial = value;
		this.matchOwnerId = ownerId;
		this.matchItemEntity = entity;
		this.matchItemProperty = prop;
		this.matchItemIndex = index;
		return( value );
	}

	public void matchEnvDone( MatchItem item ) throws Exception {
		matchEnvDone( item , matchEnv , matchOwnerId , matchItemEntity , matchItemProperty , matchItemIndex );
	}

	public void matchEnvDone( MatchItem item , MetaEnv env , int ownerId , PropertyEntity entity , String prop , String index ) throws Exception {
		if( item != null && !item.MATCHED ) {
			engine.error( "env match failed: env=" + env.NAME + ", object=" + ownerId + ", entity=" + entity.PARAMENTITY_TYPE.name() + ", property=" + prop + ", value=" + item.FKNAME );
			matchEnvUpdateStatus( env , false , false );
		}
	}
	
	private void matchEnvUpdateStatus( MetaEnv env , boolean matched , boolean finish ) {
		try {
			if( !matched )
				env.setMatched( false );
			if( finish )
				DBMetaEnv.setMatched( loader , env , matched );
		}
		catch( Throwable e ) {
			loader.log( "update match status" , e );
		}
	}

	public boolean matchEnv( EngineLoader loader , ProductMeta set , MetaEnv env , boolean update ) {
		// product meta
		try {
			prepareMatchEnv( env , false , false );

			boolean matched = true;
			if( !env.checkMatched() )
				matched = false;
			
			if( !matched ) {
				matchEnvUpdateStatus( env , false , true );
				return( false );
			}
			
			doneEnv( set , env );
		}
		catch( Throwable e ) {
			loader.log( "match problem " , e );
			matchEnvUpdateStatus( env , false , true );
			return( false );
		}
		
		matchEnvUpdateStatus( env , true , true );
		return( true );
	}
	
	private void doneEnv( ProductMeta set , MetaEnv env ) throws Exception {
	}

}
