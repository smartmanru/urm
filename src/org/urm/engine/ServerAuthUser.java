package org.urm.engine;

import org.urm.action.ActionBase;
import org.urm.common.Common;
import org.urm.common.ConfReader;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class ServerAuthUser {

	ServerAuth auth;
	
	public String NAME;
	public String FULLNAME;
	public String EMAIL;
	public boolean ADMIN;
	
	public ServerAuthUser( ServerAuth auth ) {
		this.auth = auth;
	}

	public void create( ActionBase action , String name , String email , String full , boolean admin ) {
		this.NAME = name;
		this.EMAIL = email;
		this.FULLNAME = full;
		this.ADMIN = admin;
	}
	
	public void setData( ActionBase action , String email , String full , boolean admin ) {
		this.EMAIL = email;
		this.FULLNAME = full;
		this.ADMIN = admin;
	}
	
	public void loadLocalUser( Node root ) throws Exception {
		NAME = ConfReader.getAttrValue( root , "name" );
		FULLNAME = ConfReader.getAttrValue( root , "fullname" );
		EMAIL = ConfReader.getAttrValue( root , "email" );
		ADMIN = ConfReader.getBooleanAttrValue( root , "admin" , false );
	}

	public void save( Document doc , Element root ) throws Exception {
		Common.xmlSetElementAttr( doc , root , "name" , NAME );
		Common.xmlSetElementAttr( doc , root , "fullname" , FULLNAME );
		Common.xmlSetElementAttr( doc , root , "email" , EMAIL );
		Common.xmlSetElementAttr( doc , root , "admin" , Common.getBooleanValue( ADMIN ) );
	}

}
