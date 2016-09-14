package org.urm.engine;

import org.urm.common.ConfReader;
import org.urm.engine.action.ActionBase;
import org.w3c.dom.Node;

public class ServerProduct {

	public ServerDirectory directory;
	public ServerSystem system;
	
	public String NAME;
	public String DESC;
	public String PATH;

	public ServerProduct( ServerDirectory directory , ServerSystem system ) {
		this.directory = directory;
		this.system = system;
	}

	public ServerProduct copy( ServerDirectory nr , ServerSystem rs ) {
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
	
	public ServerProductMeta getMeta( ActionBase action ) throws Exception {
		ServerLoader loader = action.engine.getLoader();
		ServerProductMeta storage = loader.findMetaStorage( NAME );
		if( storage == null )
			action.exitUnexpectedState();
		return( storage );
	}
	
	public void modifyProduct( ServerTransaction transaction , ServerProduct productNew ) throws Exception {
		DESC = productNew.DESC;
		PATH = productNew.PATH;
	}
	
}
