package org.urm.meta.product;

import org.urm.engine.data.EngineSettings;
import org.urm.engine.storage.LocalFolder;
import org.urm.meta.engine.AppProduct;

public class ProductContext {

	public AppProduct product;
	public EngineSettings settings;
	public LocalFolder home;
	
	public ProductContext( AppProduct product , EngineSettings settings , LocalFolder home ) throws Exception {
		this.product = product;
		this.settings = settings;
		this.home = home;
	}

	public void setProduct( AppProduct product ) {
		this.product = product;
	}
	
}
