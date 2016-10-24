package org.urm.action;

import org.urm.engine.ServerEventsSource;
import org.urm.engine.ServerEventsState;

public class ActionEventsSource extends ServerEventsSource {

	public ActionEventsSource( ActionCore action ) {
		super( action.engine.getEvents() , "action-" + action.ID );
	}

	@Override
	public ServerEventsState getState() {
		return( null );
	}
	
}
