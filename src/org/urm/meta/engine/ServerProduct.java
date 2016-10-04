package org.urm.meta.engine;

import org.urm.action.ActionBase;
import org.urm.common.Common;
import org.urm.common.ConfReader;
import org.urm.engine.ServerObject;
import org.urm.engine.ServerTransaction;
import org.urm.meta.product.Meta;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class ServerProduct extends ServerObject {

	public ServerDirectory directory;
	public ServerSystem system;
	
	public String NAME;
	public String DESC;
	public String PATH;
	public boolean OFFLINE;

	public ServerProduct( ServerDirectory directory , ServerSystem system ) {
		super( directory );
		this.directory = directory;
		this.system = system;
	}
	
	public void createProduct( ServerTransaction transaction , String newName , String newDesc , String newPath ) throws Exception {
		NAME = newName;
		DESC = newDesc;
		PATH = newPath;
		OFFLINE = true;
	}

	public ServerProduct copy( ServerDirectory nr , ServerSystem rs ) {
		ServerProduct r = new ServerProduct( nr , rs );
		r.NAME = NAME;
		r.DESC = DESC;
		r.PATH = PATH;
		r.OFFLINE = OFFLINE;
		return( r );
	}
	
	public void load( Node node ) throws Exception {
		NAME = ConfReader.getAttrValue( node , "name" );
		DESC = ConfReader.getAttrValue( node , "desc" );
		PATH = ConfReader.getAttrValue( node , "path" );
		OFFLINE = ConfReader.getBooleanAttrValue( node , "offline" , true );
	}
	
	public Meta getMeta( ActionBase action ) throws Exception {
		return( action.getProductMetadata( NAME ) );
	}
	
	public void modifyProduct( ServerTransaction transaction ) throws Exception {
	}
	
	public boolean isOffline() {
		return( OFFLINE );
	}

	public boolean isBroken( ActionBase action ) {
		return( action.isProductBroken( NAME ) );
	}
	
	public void save( Document doc , Element root ) throws Exception {
		Common.xmlSetElementAttr( doc , root , "name" , NAME );
		Common.xmlSetElementAttr( doc , root , "desc" , DESC );
		Common.xmlSetElementAttr( doc , root , "path" , PATH );
		Common.xmlSetElementAttr( doc , root , "offline" , Common.getBooleanValue( OFFLINE ) );
	}

}
