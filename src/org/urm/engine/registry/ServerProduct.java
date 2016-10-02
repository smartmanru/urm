package org.urm.engine.registry;

import org.urm.action.ActionBase;
import org.urm.common.ConfReader;
import org.urm.engine.ServerLoader;
import org.urm.engine.ServerObject;
import org.urm.engine.ServerTransaction;
import org.urm.engine.meta.Meta;
import org.w3c.dom.Node;

public class ServerProduct extends ServerObject {

	public ServerDirectory directory;
	public ServerSystem system;
	
	public String NAME;
	public String DESC;
	public String PATH;

	public ServerProduct( ServerDirectory directory , ServerSystem system ) {
		super( directory );
		this.directory = directory;
		this.system = system;
	}
	
	public void createProduct( ServerTransaction transaction , String newName , String newDesc , String newPath ) throws Exception {
		NAME = newName;
		DESC = newDesc;
		PATH = newPath;
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
	
	public Meta getMeta( ActionBase action ) throws Exception {
		ServerLoader loader = action.engine.getLoader();
		return( loader.getMetadata( action , NAME ) );
	}
	
	public void modifyProduct( ServerTransaction transaction , ServerProduct productNew ) throws Exception {
		DESC = productNew.DESC;
		PATH = productNew.PATH;
	}
	
}
