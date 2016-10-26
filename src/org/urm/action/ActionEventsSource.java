package org.urm.action;

import org.urm.engine.ServerEventsSource;
import org.urm.engine.ServerEventsState;

public class ActionEventsSource extends ServerEventsSource {

	ActionEventsState rootState;
	String[] log;

	public static int EVENT_FINISHSTATE = 1;
	
	public ActionEventsSource( ActionCore action ) {
		super( action.engine.getEvents() , action.getClass().getSimpleName() + "-" + action.ID );
	}

	@Override
	public ServerEventsState getState() {
		return( rootState );
	}

	public void setRootState( ScopeState scopeState ) {
		rootState = new ActionEventsState( this , scopeState );
	}
	
	public void finishScopeItem( ScopeState state ) {
		super.trigger( EVENT_FINISHSTATE , state );
	}
	
	public void forwardScopeItem( int eventType , ScopeState state ) {
		super.trigger( eventType , state );
	}

	public ScopeState findTargetState( ActionScopeTarget target ) {
		return( rootState.scopeState.findTargetState( target ) );
	}

	public void setLog( String[] log ) {
		this.log = log;
	}
	
	public String[] getLog() {
		return( log );
	}
	
}
