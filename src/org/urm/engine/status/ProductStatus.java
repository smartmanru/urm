package org.urm.engine.status;

import org.urm.meta.engine.AppProduct;

public class ProductStatus extends Status {

	public AppProduct product;
	
	public ProductStatus( AppProduct product ) {
		super( STATETYPE.TypeProduct , null , product );
		this.product = product;
	}

}
