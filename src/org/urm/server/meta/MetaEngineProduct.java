package org.urm.server.meta;

import org.urm.common.ConfReader;
import org.w3c.dom.Node;

public class MetaEngineProduct {

	public MetaEngineSystem system;
	
	public String NAME;
	public String PATH;

	public MetaEngineProduct( MetaEngineSystem system ) {
		this.system = system;
	}

	public void load( Node node ) throws Exception {
		NAME = ConfReader.getAttrValue( node , "name" );
		PATH = ConfReader.getAttrValue( node , "path" );
	}
	
	public String[] getEnvironments() throws Exception {
		FinalMetaLoader loader = system.engine.loader;
		FinalMetaStorage storage = loader.getMetaStorage( NAME );
		return( storage.getEnvironments() );
	}
	
	public MetaEnv getEnvironment( String envId ) throws Exception {
		FinalMetaLoader loader = system.engine.loader;
		FinalMetaStorage storage = loader.getMetaStorage( NAME );
		return( storage.getEnvironment( envId ) );
	}
	
}
