package org.urm.server.meta;

import org.urm.common.ConfReader;
import org.w3c.dom.Node;

public class FinalMetaProduct {

	public FinalMetaSystem system;
	
	public String NAME;
	public String DESC;
	public String PATH;

	public FinalMetaProduct( FinalMetaSystem system ) {
		this.system = system;
	}

	public void load( Node node ) throws Exception {
		NAME = ConfReader.getAttrValue( node , "name" );
		DESC = ConfReader.getAttrValue( node , "desc" );
		PATH = ConfReader.getAttrValue( node , "path" );
	}
	
	public String[] getEnvironments() throws Exception {
		FinalLoader loader = system.engine.loader;
		FinalMetaStorage storage = loader.getMetaStorage( NAME );
		return( storage.getEnvironments() );
	}
	
	public MetaEnv getEnvironment( String envId ) throws Exception {
		FinalLoader loader = system.engine.loader;
		FinalMetaStorage storage = loader.getMetaStorage( NAME );
		return( storage.getEnvironment( envId ) );
	}
	
}
