package org.urm.action;

import org.urm.engine.events.EngineEventsState;
import org.urm.engine.status.ScopeState;

public class ActionEventsState extends EngineEventsState {

	public ActionEventsSource actionSource;
	public ScopeState scopeState;
	
	public ActionEventsState( ActionEventsSource actionSource , ScopeState scopeState ) {
		super( actionSource , 0 );
		this.actionSource = actionSource;
		this.scopeState = scopeState;
	}
	
}
