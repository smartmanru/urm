package org.urm.engine.action;

import org.urm.action.ActionBase;
import org.urm.common.action.CommandMethodMeta;
import org.urm.engine.status.ScopeState;

abstract public class CommandMethod {

	public CommandMethodMeta method;
	
	public abstract void run( ScopeState parentState , ActionBase action ) throws Exception;

	public void setMethod( CommandMethodMeta method ) {
		this.method = method;
	}

	public void wrongArgs( ActionBase action ) throws Exception {
		action.exit0( _Error.WrongArgs0 , "wrong args" );
	}
	
}
