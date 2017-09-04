package org.urm.engine.status;

import java.util.HashMap;
import java.util.Map;

import org.urm.action.ScopeState.SCOPESTATE;
import org.urm.engine.events.EngineEvents;
import org.urm.engine.events.EngineEventsSource;
import org.urm.engine.events.EngineEventsState;
import org.urm.engine.status.ServerStatusData.OBJECT_STATE;
import org.urm.meta.EngineObject;
import org.urm.meta.engine.ServerMonitoring;

public class ServerStatusSource extends EngineEventsSource {

	public ServerMonitoring mon;
	public int level;
	public EngineObject object;
	public ServerStatusData state;
	private ServerStatusData primary;
	private Map<String,ServerStatusData> extra;

	public ServerStatusSource( EngineEvents events , EngineObject object , int level , String name ) {
		super( events , name );
		this.object = object;
		this.level = level;
		
		state = new ServerStatusData( this );
		primary = new ServerStatusData( this );
		extra= new HashMap<String,ServerStatusData>(); 
	}
	
	@Override
	public EngineEventsState getState() {
		return( state );
	}

	public void setObject( EngineObject object ) {
		this.object = object;
	}
	
	public void clearState() {
		state.setState( OBJECT_STATE.STATE_NOMONITORING );
		primary.setState( OBJECT_STATE.STATE_NOMONITORING );
		extra.clear();
	}
	
	public boolean setState( SCOPESTATE state ) {
		OBJECT_STATE newState = ServerStatusData.getState( state );
		return( setState( newState ) );
	}
	
	public boolean setState( OBJECT_STATE newState ) {
		if( primary.state == newState )
			return( false );

		primary.setState( newState );
		return( updateFinalState() );
	}
	
	private boolean updateFinalState() {
		OBJECT_STATE finalState = getFinalState();
		
		if( finalState != state.state ) {
			state.setState( finalState );
			super.trigger( EngineEvents.EVENT_MONITORSTATECHANGED , state );
			return( true );
		}
		
		return( false );
	}

	public synchronized ServerStatusData getExtraState( String key ) {
		ServerStatusData extraState = extra.get( key );
		if( extraState == null ) {
			extraState = new ServerStatusData( this );
			extra.put( key , extraState );
		}
		return( extraState );
	}
	
	public boolean setExtraState( String key , OBJECT_STATE newState ) {
		ServerStatusData extraState = getExtraState( key );
		if( extraState.state == newState )
			return( false );

		extraState.setState( newState );
		return( updateFinalState() );
	}
	
	private OBJECT_STATE getFinalState() {
		OBJECT_STATE state = primary.state;
		for( ServerStatusData extraState : extra.values() )
			state = ServerStatusData.addState( extraState.state , state );
		return( state );
	}
	
	public void setPrimaryLog( String[] log ) {
		primary.setLog( log );
	}

	public void setExtraLog( String key , String[] log ) {
		ServerStatusData extraState = getExtraState( key );
		extraState.setLog( log );
	}

	public String[] getPrimaryLog() {
		return( primary.log );
	}

	public String[] getExtraLog( String key ) {
		ServerStatusData extraState = getExtraState( key );
		return( extraState.log );
	}

	public void customEvent( int eventType , Object data ) {
		super.trigger( eventType , data );
	}
	
}
