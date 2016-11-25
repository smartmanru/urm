package org.urm.engine;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

public class ServerAuthGroup {

	ServerAuth auth;
	
	public ServerAuthGroup( ServerAuth auth ) {
		this.auth = auth;
	}
	
	public void loadGroup( Node node ) throws Exception {
	}
	
	public void save( Document doc , Node root ) throws Exception {
	}
	
}
