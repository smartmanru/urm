package org.urm.meta.engine;

import org.urm.common.Common;
import org.urm.common.ConfReader;
import org.urm.meta.ServerObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class ServerHostAccount extends ServerObject {

	ServerNetworkHost host;

	String ID;
	boolean keyAccess;
	public String AUTHKEY;
	
	public ServerHostAccount( ServerNetworkHost host ) {
		super( host );
	}
	
	public ServerHostAccount copy( ServerNetworkHost rh ) throws Exception {
		ServerHostAccount r = new ServerHostAccount( rh );
		r.ID = ID;
		r.keyAccess = keyAccess;
		r.AUTHKEY = AUTHKEY;
		return( r );
	}
	
	public void load( Node root ) throws Exception {
		if( root == null )
			return;
		
		ID = ConfReader.getAttrValue( root , "id" );
		keyAccess = ConfReader.getBooleanAttrValue( root , "usekey" , true );
		AUTHKEY = ConfReader.getAttrValue( root , "authkey" );
	}

	public void save( Document doc , Element root ) throws Exception {
		Common.xmlSetElementAttr( doc , root , "id" , ID );
		Common.xmlSetElementAttr( doc , root , "usekey" , Common.getBooleanValue( keyAccess ) );
		Common.xmlSetElementAttr( doc , root , "authkey" , AUTHKEY );
	}

	public String getFinalAccount() {
		return( ID + "@" + host.ID );
	}
}
