package org.urm.meta.env;

import org.urm.meta.product.Meta;

public class MetaEnvDeployment {

	protected Meta meta;
	public MetaEnvSegment sg;
	
	public MetaEnvDeployment( Meta meta , MetaEnvSegment sg ) {
		this.meta = meta;
		this.sg = sg;
	}
	
	public MetaEnvDeployment copy( Meta rmeta , MetaEnvSegment rsg ) throws Exception {
		MetaEnvDeployment r = new MetaEnvDeployment( rmeta , rsg );
		return( r );
	}

}
