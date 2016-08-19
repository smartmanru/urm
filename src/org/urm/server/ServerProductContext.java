package org.urm.server;

import org.urm.server.action.ActionBase;
import org.urm.server.meta.Meta;
import org.urm.server.meta.MetaVersion;

public class ServerProductContext {

	Meta meta;
	
	public String CONFIG_PRODUCT;
	public String CONFIG_PRODUCTHOME;
	public String CONFIG_LASTPRODTAG;
	public String CONFIG_NEXTPRODTAG;
	public String CONFIG_VERSION_BRANCH_MAJOR;
	public String CONFIG_VERSION_BRANCH_MINOR;
	public String CONFIG_VERSION_BRANCH_NEXTMAJOR;
	public String CONFIG_VERSION_BRANCH_NEXTMINOR;

	public ServerProductContext( Meta meta ) {
		this.meta = meta;
	}

	public void load( ActionBase action , MetaVersion version ) throws Exception {
		// handle product name
		if( action.session.standalone ) {
			// read from properties
			CONFIG_PRODUCT = action.session.execrc.product;
			if( CONFIG_PRODUCT.isEmpty() )
				action.exit( "Execution Context has no Product ID set (-Durm.product=value)" );
		}
		else {
			if( !action.session.product )
				action.exitUnexpectedState();
			CONFIG_PRODUCT = action.session.productName;
		}
		
		CONFIG_PRODUCTHOME = action.context.session.productPath;
		CONFIG_LASTPRODTAG = "" + version.lastProdTag;
		CONFIG_NEXTPRODTAG = "" + version.nextProdTag;
		CONFIG_VERSION_BRANCH_MAJOR = "" + version.majorFirstNumber;
		CONFIG_VERSION_BRANCH_MINOR = "" + version.majorSecondNumber;
		CONFIG_VERSION_BRANCH_NEXTMAJOR = "" + version.majorNextFirstNumber;
		CONFIG_VERSION_BRANCH_NEXTMINOR = "" + version.majorNextSecondNumber;
	}
	
}
