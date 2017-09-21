package org.urm.engine.status;

import org.urm.meta.engine.Product;

public class ProductStatus extends Status {

	public Product product;
	
	public ProductStatus( Product product ) {
		super( STATETYPE.TypeProduct , null , product );
		this.product = product;
	}

}
