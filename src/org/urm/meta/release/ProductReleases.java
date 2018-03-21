package org.urm.meta.release;

import org.urm.engine.dist.DistRepository;
import org.urm.meta.product.Meta;
import org.urm.meta.product.ProductMeta;

public class ProductReleases {

	public Meta meta;

	DistRepository repo;
	
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
	
}
