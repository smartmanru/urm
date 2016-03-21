package ru.egov.urm.meta;

import org.w3c.dom.Node;

import ru.egov.urm.action.ActionBase;

public class MetaEnvServerBase {

	public MetaEnvServer server;
	
	public MetaEnvServerBase( MetaEnvServer server ) {
		this.server = server;
	}

	public void load( ActionBase action , Node node ) throws Exception {
	}
	
}
