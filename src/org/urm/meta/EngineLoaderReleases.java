package org.urm.meta;

import org.urm.action.ActionBase;
import org.urm.db.release.DBProductReleases;
import org.urm.engine.dist.DistRepository;
import org.urm.meta.product.Meta;
import org.urm.meta.product.ProductMeta;
import org.urm.meta.release.ProductReleases;

public class EngineLoaderReleases {

	public EngineLoader loader;
	public ProductMeta set;
	public Meta meta;
	
	public EngineLoaderReleases( EngineLoader loader , ProductMeta set ) {
		this.loader = loader;
		this.set = set;
		this.meta = set.meta;
	}

	public void createAll( boolean forceClearMeta , boolean forceClearDist ) throws Exception {
		ProductReleases releases = new ProductReleases( set , meta );
		set.setReleases( releases );
		
		DBProductReleases.createdb( loader , releases , forceClearMeta , forceClearDist );

		// old
		ActionBase action = loader.getAction();
		DistRepository repo = DistRepository.createInitialRepository( action , set.meta , forceClearDist );
		releases.setDistRepository( repo );
	}
	
	public void loadReleases( ProductMeta set , boolean importxml ) throws Exception {
		ProductReleases releases = new ProductReleases( set , meta );
		set.setReleases( releases );

		DBProductReleases.loaddb( loader , releases , importxml );
		
		// old
		ActionBase action = loader.getAction();
		try {
			DistRepository repo = DistRepository.loadDistRepository( action , set.meta , importxml );
			releases.setDistRepository( repo );
		}
		catch( Throwable e ) {
			loader.setLoadFailed( action , _Error.UnableLoadProductReleases1 , e , "unable to load release repository, product=" + set.name , set.name );
		}
	}
	
}
