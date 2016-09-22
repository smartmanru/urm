package org.urm.engine.meta;

import org.urm.action.ActionBase;
import org.w3c.dom.Node;

public class MetaEnvDeployment {

	protected Meta meta;
	public MetaEnvDC dc;
	
	public MetaEnvDeployment( Meta meta , MetaEnvDC dc ) {
		this.meta = meta;
		this.dc = dc;
	}
	
	public void load( ActionBase action , Node node ) throws Exception {
	}

	public MetaEnvDeployment copy( ActionBase action , Meta meta , MetaEnvDC dc ) throws Exception {
		MetaEnvDeployment r = new MetaEnvDeployment( meta , dc );
		return( r );
	}
	
}
