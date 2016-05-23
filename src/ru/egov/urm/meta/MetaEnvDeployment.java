package ru.egov.urm.meta;

import org.w3c.dom.Node;

import ru.egov.urm.action.ActionBase;

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
