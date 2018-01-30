package org.urm.meta.product;

import org.urm.engine.storage.LocalFolder;
import org.urm.meta.engine.AppProduct;
import org.urm.meta.engine.EngineSettings;

public class ProductContext {

	public AppProduct product;
	public Integer ID;
	public String NAME;
	public Integer PRODUCT_ID;
	public boolean MATCHED;
	public int PV;
	
	public EngineSettings settings;
	public LocalFolder home;
	
	public ProductContext( int metaId , Integer productId , String name , boolean matched , int version ) {
		this.ID = metaId;
		this.NAME = name;
		this.PRODUCT_ID = productId;
		this.MATCHED = matched;
		this.PV = version;
	}
	
	public ProductContext( AppProduct product , boolean matched ) {
		this.product = product;
		this.MATCHED = matched;
		this.NAME = product.NAME;
		this.PRODUCT_ID = product.ID;
	}

	public void create( EngineSettings settings , LocalFolder home ) throws Exception {
		this.settings = settings;
		this.home = home;
	}

	public void setProduct( AppProduct product ) {
		this.product = product;
	}
	
}