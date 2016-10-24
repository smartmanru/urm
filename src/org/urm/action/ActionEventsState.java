package org.urm.action;

import org.urm.engine.ServerEventsState;

public class ActionEventsState extends ServerEventsState {

	ActionEventsSource actionSource;
	
	public ActionEventsState( ActionEventsSource actionSource , int stateId ) {
		super( actionSource , stateId );
		this.actionSource = actionSource;
	}
	
}
