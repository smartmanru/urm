package org.urm.action;

import org.urm.engine.ServerEventsState;

public class ActionEventsState extends ServerEventsState {

	public ActionEventsSource actionSource;
	public ScopeState scopeState;
	
	public ActionEventsState( ActionEventsSource actionSource , ScopeState scopeState ) {
		super( actionSource , 0 );
		this.actionSource = actionSource;
		this.scopeState = scopeState;
	}
	
}
