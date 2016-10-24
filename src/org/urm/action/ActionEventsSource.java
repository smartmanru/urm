package org.urm.action;

import org.urm.engine.ServerEventsSource;
import org.urm.engine.ServerEventsState;

public class ActionEventsSource extends ServerEventsSource {

	ActionEventsState state;

	public static int EVENT_FINISHSTATE;
	
	public ActionEventsSource( ActionCore action ) {
		super( action.engine.getEvents() , "action-" + action.ID );
	}

	@Override
	public ServerEventsState getState() {
		return( state );
	}

	public void setState( ScopeState scopeState ) {
		state = new ActionEventsState( this , scopeState );
	}
	
	public void finishState( ScopeState state ) {
		super.trigger( EVENT_FINISHSTATE , state );
	}
	
}
