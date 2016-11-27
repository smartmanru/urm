package org.urm.engine;

import org.urm.action.ActionBase;
import org.urm.common.Common;
import org.urm.common.ConfReader;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class ServerAuthGroup {

	ServerAuth auth;
	
	public String NAME;
	
	public ServerAuthGroup( ServerAuth auth ) {
		this.auth = auth;
	}

	public void create( ActionBase action , String name ) throws Exception {
		this.NAME = name;
	}
	
	public void rename( ActionBase action , String name ) throws Exception {
		this.NAME = name;
	}
	
	public void loadGroup( Node root ) throws Exception {
		NAME = ConfReader.getAttrValue( root , "name" );
	}
	
	public void save( Document doc , Element root ) throws Exception {
		Common.xmlSetElementAttr( doc , root , "name" , NAME );
	}

	public void deleteUser( ActionBase action , ServerAuthUser user ) throws Exception {
	}
	
}
