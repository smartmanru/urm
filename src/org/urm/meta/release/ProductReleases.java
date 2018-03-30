package org.urm.meta.release;

import org.urm.db.core.DBEnums.DBEnumLifecycleType;
import org.urm.engine.dist.DistRepository;
import org.urm.meta.product.Meta;
import org.urm.meta.product.MetaProductVersion;
import org.urm.meta.product.ProductMeta;

public class ProductReleases {

	public Meta meta;

	DistRepository repo;
	ReleaseRepository rrepo;
	
	public ProductReleases( ProductMeta storage , Meta meta ) {
		this.meta = meta;
	}

	public ProductReleases copy( Meta rmeta ) throws Exception {
		ProductReleases r = new ProductReleases( rmeta.getStorage() , rmeta );
		return( r );
	}

	public void setDistRepository( DistRepository repo ) {
		this.repo = repo;
	}
	
	public DistRepository getDistRepository() {
		return( repo );
	}
	
	public void setReleaseRepository( ReleaseRepository rrepo ) {
		this.rrepo = rrepo;
	}
	
	public ReleaseRepository getReleaseRepository() {
		return( rrepo );
	}
	
	public Release findRelease( String RELEASEVER ) {
		return( rrepo.findRelease( RELEASEVER ) );
	}

	public String getNextRelease( DBEnumLifecycleType type ) {
		MetaProductVersion version = meta.getVersion();
		if( type == DBEnumLifecycleType.MAJOR )
			return( version.majorNextFirstNumber + "." + version.majorNextSecondNumber );
		if( type == DBEnumLifecycleType.MINOR )
			return( version.majorLastFirstNumber + "." + version.majorLastSecondNumber + "." + version.nextProdTag );
		if( type == DBEnumLifecycleType.URGENT )
			return( version.majorLastFirstNumber + "." + version.majorLastSecondNumber + "." + version.lastProdTag + version.nextUrgentTag );
		return( "" );
	}
	
	public Release findLastRelease( DBEnumLifecycleType type ) {
		String[] versions = rrepo.getActiveVersions();
		for( int k = versions.length - 1; k >= 0; k-- ) {
			Release release = rrepo.findRelease( versions[ k ] );
			if( release.TYPE == type )
				return( release );
		}
		return( null );
	}
	
}
