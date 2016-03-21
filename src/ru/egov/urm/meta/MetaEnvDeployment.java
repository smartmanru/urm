package ru.egov.urm.meta;

import org.w3c.dom.Node;

import ru.egov.urm.action.ActionBase;

public class MetaEnvDeployment {

	public MetaEnvDC dc;
	
	public MetaEnvDeployment( MetaEnvDC dc ) {
		this.dc = dc;
	}
	
	public void load( ActionBase action , Node node ) throws Exception {
	}
}
