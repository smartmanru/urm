package org.urm.server;

import org.urm.server.action.ActionBase;

public class ServerTransaction {

	public ActionBase action;
	
	public ServerTransaction( ActionBase action ) {
		this.action = action;
	}
	
}
