package org.urm.server;

import org.urm.common.ConfReader;
import org.urm.server.meta.MetaEnv;
import org.w3c.dom.Node;

public class ServerProduct {

	public ServerRegistry registry;
	public ServerSystem system;
	
	public String NAME;
	public String DESC;
	public String PATH;

	public ServerProduct( ServerRegistry registry , ServerSystem system ) {
		this.registry = registry;
		this.system = system;
	}

	public ServerProduct copy( ServerRegistry nr , ServerSystem rs ) {
		ServerProduct rp = new ServerProduct( nr , rs );
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
		ServerLoader loader = system.registry.loader;
		ServerMetaSet storage = loader.getMetaStorage( NAME );
		if( storage == null )
			return( new String[0] );
		return( storage.getEnvironments() );
	}
	
	public MetaEnv getEnvironment( String envId ) throws Exception {
		ServerLoader loader = system.registry.loader;
		ServerMetaSet storage = loader.getMetaStorage( NAME );
		if( storage == null )
			return( null );
		return( storage.getEnvironment( envId ) );
	}

	public void modifyProduct( ServerTransaction transaction , ServerProduct productNew ) throws Exception {
		DESC = productNew.DESC;
		PATH = productNew.PATH;
	}
	
}
