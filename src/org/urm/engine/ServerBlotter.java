package org.urm.engine;

import org.urm.action.ActionBase;

public class ServerBlotter {

	public ServerEngine engine;
	
	public ServerBlotter( ServerEngine engine ) {
		this.engine = engine;
	}
	
	public void startAction( ActionBase action ) {
	}
	
	public void stopAction( ActionBase action , boolean success ) {
	}
	
}
