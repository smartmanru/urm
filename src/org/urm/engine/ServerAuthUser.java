package org.urm.engine;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

public class ServerAuthUser {

	ServerAuth auth;
	
	public ServerAuthUser( ServerAuth auth ) {
		this.auth = auth;
	}

	public void loadLocalUser( Node node ) throws Exception {
	}

	public void save( Document doc , Node root ) throws Exception {
	}
	
}
