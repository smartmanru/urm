package org.urm.meta.engine;

import org.urm.db.core.DBEnums.DBEnumLifecycleType;
import org.urm.engine.dist.DistRepository;
import org.urm.meta.product.Meta;
import org.urm.meta.product.ProductMeta;
import org.urm.meta.release.Release;
import org.urm.meta.release.ReleaseRepository;

public class ProductReleases {

	public Meta meta;

	DistRepository distRepo;
	ReleaseRepository releaseRepo;
	
	public ProductReleases( ProductMeta storage , Meta meta ) {
		this.meta = meta;
	}

	public ProductReleases copy( Meta rmeta ) throws Exception {
		ProductReleases r = new ProductReleases( rmeta.getStorage() , rmeta );
		r.releaseRepo = releaseRepo.copy( rmeta , r );  
		r.distRepo = distRepo.copy( rmeta , r.releaseRepo );  
		return( r );
	}

	public void setDistRepository( DistRepository repo ) {
		this.distRepo = repo;
	}
	
	public DistRepository getDistRepository() {
		return( distRepo );
	}
	
	public void setReleaseRepository( ReleaseRepository rrepo ) {
		this.releaseRepo = rrepo;
	}
	
	public ReleaseRepository getReleaseRepository() {
		return( releaseRepo );
	}
	
	public Release findRelease( String RELEASEVER ) {
		return( releaseRepo.findRelease( RELEASEVER ) );
	}

	public String getNextRelease( DBEnumLifecycleType type ) {
		AppProduct product = meta.findProduct();
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
	
	public Release findLastRelease( DBEnumLifecycleType type ) {
		String[] versions = releaseRepo.getActiveVersions();
		for( int k = versions.length - 1; k >= 0; k-- ) {
			Release release = releaseRepo.findRelease( versions[ k ] );
			if( type == null || release.TYPE == type )
				return( release );
		}
		return( null );
	}
	
}
