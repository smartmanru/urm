package org.urm.engine;

import org.urm.action.ActionBase;
import org.urm.engine.meta.Meta;
import org.urm.engine.meta.MetaProductVersion;

public class ServerProductContext {

	Meta meta;
	
	public String CONFIG_PRODUCT;
	public String CONFIG_PRODUCTHOME;
	
	public int CONFIG_LASTPRODTAG;
	public int CONFIG_NEXTPRODTAG;
	public int CONFIG_VERSION_BRANCH_MAJOR;
	public int CONFIG_VERSION_BRANCH_MINOR;
	public int CONFIG_VERSION_BRANCH_NEXTMAJOR;
	public int CONFIG_VERSION_BRANCH_NEXTMINOR;

	public ServerProductContext( Meta meta ) {
		this.meta = meta;
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
			if( !action.session.product )
				action.exitUnexpectedState();
			CONFIG_PRODUCT = action.session.productName;
		}
		
		CONFIG_PRODUCTHOME = action.context.session.productPath;
		CONFIG_LASTPRODTAG = version.lastProdTag;
		CONFIG_NEXTPRODTAG = version.nextProdTag;
		CONFIG_VERSION_BRANCH_MAJOR = version.majorFirstNumber;
		CONFIG_VERSION_BRANCH_MINOR = version.majorSecondNumber;
		CONFIG_VERSION_BRANCH_NEXTMAJOR = version.majorNextFirstNumber;
		CONFIG_VERSION_BRANCH_NEXTMINOR = version.majorNextSecondNumber;
	}
	
}