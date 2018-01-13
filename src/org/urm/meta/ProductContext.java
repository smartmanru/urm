package org.urm.meta;

import org.urm.action.ActionBase;
import org.urm.engine.storage.LocalFolder;
import org.urm.engine.storage.UrmStorage;
import org.urm.meta.engine.AppProduct;
import org.urm.meta.product.Meta;
import org.urm.meta.product.MetaProductVersion;

public class ProductContext {

	public Meta meta;
	public AppProduct product;
	
	public String CONFIG_PRODUCT;
	public String CONFIG_PRODUCTHOME;
	
	public int CONFIG_LASTPRODTAG;
	public int CONFIG_NEXTPRODTAG;
	public int CONFIG_VERSION_BRANCH_MAJOR;
	public int CONFIG_VERSION_BRANCH_MINOR;
	public int CONFIG_VERSION_BRANCH_NEXTMAJOR;
	public int CONFIG_VERSION_BRANCH_NEXTMINOR;

	public ProductContext( Meta meta , AppProduct product ) {
		this.meta = meta;
		this.product = product;
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
