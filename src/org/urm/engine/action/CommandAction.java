package org.urm.engine.action;

import org.urm.common.action.CommandMethodMeta;

abstract public class CommandAction {

	public CommandMethodMeta method;
	
	public abstract void run( ActionInit action ) throws Exception;

	public void setMethod( CommandMethodMeta method ) {
		this.method = method;
	}

}
