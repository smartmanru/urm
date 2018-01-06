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

	public void setRootState( ScopeState state ) {
		rootState = new ActionEventsState( this , state );
	}
	
	public void finishScopeItem( ScopeState state ) {
		super.notify( EngineEvents.OWNER_ENGINE , EngineEvents.EVENT_FINISHSTATE , state );
	}
	
	public void startScopeItem( ScopeState state ) {
		super.notify( EngineEvents.OWNER_ENGINE , EngineEvents.EVENT_STARTSTATE , state );
	}
	
	public void finishScopeItem( int eventOwner , int eventType , ScopeState state ) {
		super.notify( eventOwner , eventType , state );
	}

	public void startScopeItem( int eventOwner , int eventType , ScopeState state ) {
		super.notify( eventOwner , eventType , state );
	}
	
	public void forwardState( int eventOwner , int eventType , ObjectState state ) {
		super.notify( eventOwner , eventType , state );
	}

	public void customEvent( int eventOwner , int eventType , Object data ) {
		super.notify( eventOwner , eventType , data );
	}

}
