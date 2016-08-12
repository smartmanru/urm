package org.urm.server.meta;

import org.urm.server.action.ActionBase;
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
}
