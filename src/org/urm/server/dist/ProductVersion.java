package org.urm.server.dist;

import org.urm.server.meta.Metadata;

public class ProductVersion {

	public int lastProdTag;
	public int nextProdTag;
	
	public ProductVersion( Metadata meta ) {
		lastProdTag = 0;
		nextProdTag = 1;
	}
	
}
