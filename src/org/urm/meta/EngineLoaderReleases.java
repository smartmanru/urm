package org.urm.meta;

import org.urm.action.ActionBase;
import org.urm.engine.dist.DistRepository;
import org.urm.meta.product.Meta;
import org.urm.meta.product.ProductMeta;

public class EngineLoaderReleases {

	public EngineLoader loader;
	public ProductMeta set;
	public Meta meta;
	
	public EngineLoaderReleases( EngineLoader loader , ProductMeta set ) {
		this.loader = loader;
		this.set = set;
		this.meta = set.meta;
	}

	public void loadReleases( ProductMeta set , boolean importxml ) throws Exception {
		ActionBase action = loader.getAction();
		
		try {
			DistRepository repo = DistRepository.loadDistRepository( action , set.meta , importxml );
			set.setReleases( repo );
		}
		catch( Throwable e ) {
			loader.setLoadFailed( action , _Error.UnableLoadProductReleases1 , e , "unable to load release repository, product=" + set.name , set.name );
		}
	}
	
}
