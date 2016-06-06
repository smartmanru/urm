package org.urm.meta;

import org.urm.action.ActionBase;
import org.w3c.dom.Node;

public class MetaEnvDeployment {

	Metadata meta;
	public MetaEnvDC dc;
	
	public MetaEnvDeployment( Metadata meta , MetaEnvDC dc ) {
		this.meta = meta;
		this.dc = dc;
	}
	
	public void load( ActionBase action , Node node ) throws Exception {
	}
}
