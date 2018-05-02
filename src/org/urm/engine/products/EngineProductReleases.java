package org.urm.engine.products;

import java.util.LinkedList;
import java.util.List;

import org.urm.action.ActionBase;
import org.urm.common.Common;
import org.urm.db.core.DBEnums.DBEnumLifecycleType;
import org.urm.engine.Engine;
import org.urm.engine.dist.Dist;
import org.urm.engine.dist.DistRepository;
import org.urm.engine.dist.VersionInfo;
import org.urm.meta.engine.AppProduct;
import org.urm.meta.product.Meta;
import org.urm.meta.product.ProductMeta;
import org.urm.meta.release.Release;
import org.urm.meta.release.ReleaseDist;
import org.urm.meta.release.ReleaseRepository;

public class EngineProductReleases {

	public Engine engine;
	public EngineProduct ep;
	private DistRepository distRepo;
	
	public EngineProductReleases( EngineProduct ep ) {
		this.engine = ep.engine;
		this.ep = ep;
	}

	public AppProduct findProduct() {
		return( ep.findProduct() );
	}
	
	public void setDistRepository( DistRepository repo ) {
		this.distRepo = repo;
	}
	
	public DistRepository getDistRepository() {
		return( distRepo );
	}
	
	public String getNextRelease( DBEnumLifecycleType type ) {
		AppProduct product = findProduct();
		
		Release release = findLastRelease( type );
		if( release != null )
			return( VersionInfo.getNextVersion( release.RELEASEVER ) );
			
		if( type == DBEnumLifecycleType.MAJOR )
			return( product.NEXT_MAJOR1 + "." + product.NEXT_MAJOR2 );
		if( type == DBEnumLifecycleType.MINOR )
			return( product.LAST_MAJOR1 + "." + product.LAST_MAJOR2 + "." + product.NEXT_MINOR1 );
		if( type == DBEnumLifecycleType.URGENT )
			return( product.LAST_MAJOR1 + "." + product.LAST_MAJOR2 + "." + product.LAST_MINOR1 + "." + product.NEXT_MINOR2 );
		return( "" );
	}

	public Release findLastRelease() {
		return( findLastRelease( null ) );
	}
	
	public String[] getActiveVersions() {
		String[] versions = new String[0];
		EngineProductRevisions revisions = ep.getRevisions();
		for( ProductMeta storage : revisions.getRevisions() ) {
			ReleaseRepository repo = storage.getReleaseRepository();
			if( repo == null )
				continue;
			
			versions = Common.addArrays( versions , repo.getActiveVersions() );
		}
		
		return( VersionInfo.orderVersions( versions ) );
	}

	public Release[] getActiveReleases() {
		List<Release> list = new LinkedList<Release>();
		for( String version : getActiveVersions() ) {
			Release release = findRelease( version );
			list.add( release );
		}

		return( list.toArray( new Release[0] ) );
	}
	
	public Release findLastRelease( DBEnumLifecycleType type ) {
		String[] versions = getActiveVersions();
		for( int k = versions.length - 1; k >= 0; k-- ) {
			Release release = findRelease( versions[ k ] );
			if( type == null || release.TYPE == type )
				return( release );
		}
		return( null );
	}
	
	public Release findRelease( String RELEASEVER ) {
		try {
			String version = VersionInfo.normalizeReleaseVer( RELEASEVER );
			EngineProductRevisions revisions = ep.getRevisions();
			for( ProductMeta storage : revisions.getRevisions() ) {
				ReleaseRepository repo = storage.getReleaseRepository();
				if( repo == null )
					continue;
				
				Release release = repo.findReleaseByFullVersion( version );
				if( release != null )
					return( release );
			}
		}
		catch( Throwable e ) {
			engine.log( "version" , e );
		}
		return( null );
	}

	public Release findRelease( int id ) {
		EngineProductRevisions revisions = ep.getRevisions();
		for( ProductMeta storage : revisions.getRevisions() ) {
			ReleaseRepository repo = storage.getReleaseRepository();
			if( repo == null )
				continue;
			
			Release release = repo.findRelease( id );
			if( release != null )
				return( release );
		}
		return( null );
	}
	
	public Release getNextRelease( String RELEASEVER ) {
		try {
			String[] versions = getActiveVersions();
			String version = VersionInfo.normalizeReleaseVer( RELEASEVER );
			int index = Common.getIndexOf( versions , version );
			if( index >= 0 && index < versions.length - 1 )
				return( findRelease( versions[ index + 1 ] ) );
		}		
		catch( Throwable e ) {
		}
		return( null );	
	}

	public ReleaseDist findReleaseDist( Dist dist ) {
		Release release = null;
		if( dist.isMaster() ) {
			EngineProductRevisions revisions = ep.getRevisions();
			for( ProductMeta storage : revisions.getRevisions() ) {
				ReleaseRepository repo = storage.getReleaseRepository();
				if( repo == null )
					continue;
				
				release = repo.findRelease( dist.release.ID );
				if( release != null )
					break;
			}
		
			if( release == null )
				return( null );
		
			return( release.getDefaultReleaseDist() );
		}
		
		release = findRelease( dist.release.ID );
		if( release == null )
			return( null );
		return( release.findDistVariant( dist.releaseDist.DIST_VARIANT ) ); 
	}
	
	public Dist getDistByLabel( ActionBase action , Meta meta , String RELEASELABEL ) throws Exception {
		return( distRepo.getDistByLabel( action , meta , RELEASELABEL ) );
	}

	public Release getReleaseByLabel( ActionBase action , String RELEASELABEL ) throws Exception {
		EngineProductRevisions revisions = ep.getRevisions();
		for( ProductMeta storage : revisions.getRevisions() ) {
			ReleaseRepository repo = storage.getReleaseRepository();
			if( repo == null )
				continue;
			
			Release release = repo.findReleaseByLabel( action , RELEASELABEL );
			if( release != null )
				return( release );
		}
		
		Common.exit0( _Error.UnknownRelease1 , "unknown release label=" + RELEASELABEL );
		return( null );
	}

	public Dist findDefaultReleaseDist( Release release ) {
		return( distRepo.findDefaultDist( release ) );
	}
	
	public Release getRelease( int id ) throws Exception {
		EngineProductRevisions revisions = ep.getRevisions();
		for( ProductMeta storage : revisions.getRevisions() ) {
			ReleaseRepository repo = storage.getReleaseRepository();
			if( repo == null )
				continue;
			
			Release release = repo.findRelease( id );
			if( release != null )
				return( release );
		}
		Common.exit0( _Error.UnknownRelease1 , "unknown release id=" + id );
		return( null );
	}

	public Release findDefaultMaster() {
		EngineProductRevisions revisions = ep.getRevisions();
		for( ProductMeta storage : revisions.getRevisions() ) {
			ReleaseRepository repo = storage.getReleaseRepository();
			Release release = repo.findDefaultMaster();
			if( release != null )
				return( release );
		}
		return( null );
	}
	
}
