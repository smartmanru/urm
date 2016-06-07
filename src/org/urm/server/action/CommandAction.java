package org.urm.server.action;

import org.urm.common.action.CommandMethod;

abstract public class CommandAction {

	public CommandMethod method;
	
	public abstract void run( ActionInit action ) throws Exception;

	public void setMethod( CommandMethod method ) {
		this.method = method;
	}

}
