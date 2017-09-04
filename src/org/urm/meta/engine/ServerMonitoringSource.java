package org.urm.meta.engine;

import java.util.HashMap;
import java.util.Map;

import org.urm.action.ScopeState.SCOPESTATE;
import org.urm.engine.events.ServerEvents;
import org.urm.engine.events.ServerEventsSource;
import org.urm.engine.events.ServerEventsState;
import org.urm.meta.ServerObject;
import org.urm.meta.engine.ServerMonitoringState.MONITORING_STATE;

public class ServerMonitoringSource extends ServerEventsSource {

	public ServerMonitoring mon;
	public int level;
	public ServerObject object;
	public ServerMonitoringState state;
	private ServerMonitoringState primary;
	private Map<String,ServerMonitoringState> extra;

	public ServerMonitoringSource( ServerMonitoring mon , ServerObject object , int level , String name ) {
		super( mon.events , name );
		this.mon = mon;
		this.object = object;
		this.level = level;
		
		state = new ServerMonitoringState( this );
		primary = new ServerMonitoringState( this );
		extra= new HashMap<String,ServerMonitoringState>(); 
	}
	
	@Override
	public ServerEventsState getState() {
		return( state );
	}

	public void setObject( ServerObject object ) {
		this.object = object;
	}
	
	public void clearState() {
		state.setState( MONITORING_STATE.STATE_NOMONITORING );
		primary.setState( MONITORING_STATE.STATE_NOMONITORING );
		extra.clear();
	}
	
	public boolean setState( SCOPESTATE state ) {
		MONITORING_STATE newState = ServerMonitoringState.getState( state );
		return( setState( newState ) );
	}
	
	public boolean setState( MONITORING_STATE newState ) {
		if( primary.state == newState )
			return( false );

		primary.setState( newState );
		return( updateFinalState() );
	}
	
	private boolean updateFinalState() {
		MONITORING_STATE finalState = getFinalState();
		
		if( finalState != state.state ) {
			state.setState( finalState );
			super.trigger( ServerEvents.EVENT_MONITORSTATECHANGED , state );
			return( true );
		}
		
		return( false );
	}

	public synchronized ServerMonitoringState getExtraState( String key ) {
		ServerMonitoringState extraState = extra.get( key );
		if( extraState == null ) {
			extraState = new ServerMonitoringState( this );
			extra.put( key , extraState );
		}
		return( extraState );
	}
	
	public boolean setExtraState( String key , MONITORING_STATE newState ) {
		ServerMonitoringState extraState = getExtraState( key );
		if( extraState.state == newState )
			return( false );

		extraState.setState( newState );
		return( updateFinalState() );
	}
	
	private MONITORING_STATE getFinalState() {
		MONITORING_STATE state = primary.state;
		for( ServerMonitoringState extraState : extra.values() )
			state = ServerMonitoringState.addState( extraState.state , state );
		return( state );
	}
	
	public void setPrimaryLog( String[] log ) {
		primary.setLog( log );
	}

	public void setExtraLog( String key , String[] log ) {
		ServerMonitoringState extraState = getExtraState( key );
		extraState.setLog( log );
	}

	public String[] getPrimaryLog() {
		return( primary.log );
	}

	public String[] getExtraLog( String key ) {
		ServerMonitoringState extraState = getExtraState( key );
		return( extraState.log );
	}

	public void customEvent( int eventType , Object data ) {
		super.trigger( eventType , data );
	}
	
}
