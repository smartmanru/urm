package org.urm.engine;

public class ServerBlotterItem {

	ServerBlotter blotter;
	ServerBlotterItem parent;
	
	public ServerBlotterItem( ServerBlotter blotter , ServerBlotterItem parent ) {
		this.blotter = blotter;
		this.parent = parent;
	}
	
}
