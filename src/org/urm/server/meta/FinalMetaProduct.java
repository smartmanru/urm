package org.urm.server.meta;

import org.urm.common.ConfReader;
import org.urm.server.ServerTransaction;
import org.w3c.dom.Node;

public class FinalMetaProduct {

	public FinalRegistry registry;
	public FinalMetaSystem system;
	
	public String NAME;
	public String DESC;
	public String PATH;

	public FinalMetaProduct( FinalRegistry registry , FinalMetaSystem system ) {
		this.registry = registry;
		this.system = system;
	}

	public FinalMetaProduct copy( FinalRegistry nr , FinalMetaSystem rs ) {
		FinalMetaProduct rp = new FinalMetaProduct( nr , rs );
		rp.NAME = NAME;
		rp.DESC = DESC;
		rp.PATH = PATH;
		return( rp );
	}
	
	public void load( Node node ) throws Exception {
		NAME = ConfReader.getAttrValue( node , "name" );
		DESC = ConfReader.getAttrValue( node , "desc" );
		PATH = ConfReader.getAttrValue( node , "path" );
	}
	
	public String[] getEnvironments() throws Exception {
		FinalLoader loader = system.registry.loader;
		FinalMetaSet storage = loader.getMetaStorage( NAME );
		if( storage == null )
			return( new String[0] );
		return( storage.getEnvironments() );
	}
	
	public MetaEnv getEnvironment( String envId ) throws Exception {
		FinalLoader loader = system.registry.loader;
		FinalMetaSet storage = loader.getMetaStorage( NAME );
		if( storage == null )
			return( null );
		return( storage.getEnvironment( envId ) );
	}

	public void modifyProduct( ServerTransaction transaction , FinalMetaProduct productNew ) throws Exception {
		DESC = productNew.DESC;
		PATH = productNew.PATH;
	}
	
}
