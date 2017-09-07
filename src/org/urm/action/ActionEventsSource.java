package org.urm.action;

import org.urm.engine.events.EngineEvents;
import org.urm.engine.events.EngineEventsSource;
import org.urm.engine.events.EngineEventsState;
import org.urm.engine.status.ObjectState;
import org.urm.engine.status.ScopeState;

public class ActionEventsSource extends EngineEventsSource {

	ActionEventsState rootState;

	public ActionEventsSource( ActionCore action ) {
		super( action.engine.getEvents() , action.getClass().getSimpleName() + "-" + action.ID );
	}

	@Override
	public EngineEventsState getState() {
		return( rootState );
	}

	public void setRootState( ScopeState scopeState ) {
		rootState = new ActionEventsState( this , scopeState );
	}
	
	public void finishScopeItem( ScopeState state ) {
		super.notify( EngineEvents.EVENT_FINISHSTATE , state );
	}
	
	public void finishScopeItem( int eventType , ScopeState state ) {
		super.notify( eventType , state );
	}
	
	public void forwardState( int eventType , ObjectState state ) {
		super.notify( eventType , state );
	}

	public ScopeState findSetState( ActionScopeSet set ) {
		return( rootState.scopeState.findSetState( set ) );
	}

	public ScopeState findTargetState( ActionScopeTarget target ) {
		return( rootState.scopeState.findTargetState( target ) );
	}

	public void customEvent( int eventType , Object data ) {
		super.notify( eventType , data );
	}

}
