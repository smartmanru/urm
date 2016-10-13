package org.urm.meta.engine;

import org.urm.common.Common;
import org.urm.common.ConfReader;
import org.urm.engine.ServerTransaction;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class ServerBaseItem {

	public ServerBaseGroup group;
	public String ID;
	public String DESC;
	
	public ServerBaseItem( ServerBaseGroup group ) {
		this.group = group;
	}

	public void createBaseItem( ServerTransaction transaction , String ID , String DESC ) {
		this.ID = ID;
		this.DESC = DESC;
	}
	
	public void modifyBaseItem( ServerTransaction transaction , String ID , String DESC ) {
		this.ID = ID;
		this.DESC = DESC;
	}
	
	public ServerBaseItem copy( ServerBaseGroup rgroup ) {
		return( null );
	}

	public void load( Node root ) throws Exception {
		ID = ConfReader.getAttrValue( root , "id" );
		DESC = ConfReader.getAttrValue( root , "desc" );
	}
	
	public void save( Document doc , Element root ) throws Exception {
		Common.xmlSetElementAttr( doc , root , "id" , ID );
		Common.xmlSetElementAttr( doc , root , "desc" , DESC );
	}
	
}
