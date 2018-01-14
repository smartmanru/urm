package org.urm.meta;

import org.urm.action.ActionBase;
import org.urm.engine.storage.LocalFolder;
import org.urm.engine.storage.UrmStorage;
import org.urm.meta.engine.AppProduct;
import org.urm.meta.product.MetaProductVersion;

public class ProductContext {

	public AppProduct product;
	public int ID;
	public String NAME;
	public Integer PRODUCT_ID;
	public boolean MATCHED;
	public int PV;
	
	public String CONFIG_PRODUCT;
	public String CONFIG_PRODUCTHOME;
	
	public int CONFIG_LASTPRODTAG;
	public int CONFIG_NEXTPRODTAG;
	public int CONFIG_VERSION_BRANCH_MAJOR;
	public int CONFIG_VERSION_BRANCH_MINOR;
	public int CONFIG_VERSION_BRANCH_NEXTMAJOR;
	public int CONFIG_VERSION_BRANCH_NEXTMINOR;

	public ProductContext( int metaId , Integer productId , String name , boolean matched , int version ) {
		this.ID = metaId;
		this.NAME = name;
		this.PRODUCT_ID = productId;
		this.MATCHED = matched;
		this.PV = version;
	}
	
	public ProductContext( AppProduct product ) {
		this.product = product;
		this.MATCHED = true;
		this.PRODUCT_ID = product.ID;
	}

	public void create( ActionBase action , MetaProductVersion version ) throws Exception {
		// handle product name
		if( action.session.standalone ) {
			// read from properties
			CONFIG_PRODUCT = action.session.execrc.product;
			if( CONFIG_PRODUCT.isEmpty() )
				action.exit0( _Error.NoProductID , "Execution Context has no Product ID set (-Durm.product=value)" );
		}
		else {
			CONFIG_PRODUCT = version.meta.name;
		}
		
		UrmStorage urm = action.artefactory.getUrmStorage();
		LocalFolder folder = urm.getProductHome( action , CONFIG_PRODUCT );
		CONFIG_PRODUCTHOME = folder.folderPath;
		CONFIG_LASTPRODTAG = version.lastProdTag;
		CONFIG_NEXTPRODTAG = version.nextProdTag;
		CONFIG_VERSION_BRANCH_MAJOR = version.majorLastFirstNumber;
		CONFIG_VERSION_BRANCH_MINOR = version.majorLastSecondNumber;
		CONFIG_VERSION_BRANCH_NEXTMAJOR = version.majorNextFirstNumber;
		CONFIG_VERSION_BRANCH_NEXTMINOR = version.majorNextSecondNumber;
	}
	
}
