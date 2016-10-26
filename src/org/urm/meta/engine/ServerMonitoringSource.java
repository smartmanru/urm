package org.urm.meta.engine;

import org.urm.action.ScopeState.SCOPESTATE;
import org.urm.engine.ServerEventsSource;
import org.urm.engine.ServerEventsState;
import org.urm.meta.ServerObject;
import org.urm.meta.engine.ServerMonitoringState.MONITORING_STATE;

public class ServerMonitoringSource extends ServerEventsSource {

	public ServerMonitoring mon;
	public int level;
	public ServerObject object;
	public ServerMonitoringState data;
	public String[] log;

	public static int EVENT_MONITORSTATECHANGED = 1;
	public static int EVENT_MONITORCHILDCHANGED = 2;
	
	public ServerMonitoringSource( ServerMonitoring mon , ServerObject object , int level , String name ) {
		super( mon.events , name );
		this.mon = mon;
		this.object = object;
		this.level = level;
		
		data = new ServerMonitoringState( this );
	}
	
	@Override
	public ServerEventsState getState() {
		return( data );
	}

	public void clearState() {
		data.setState( MONITORING_STATE.MONITORING_NEVERQUERIED );
	}
	
	public boolean setState( SCOPESTATE state ) {
		MONITORING_STATE newState = ServerMonitoringState.getState( state );
		return( setState( newState ) );
	}
	
	public boolean setState( MONITORING_STATE newState ) {
		if( newState != data.state ) {
			data.setState( newState );
			super.trigger( EVENT_MONITORSTATECHANGED , data );
			return( true );
		}
		
		return( false );
	}
	
	public boolean addState( SCOPESTATE state ) {
		MONITORING_STATE addState = ServerMonitoringState.getState( state );
		MONITORING_STATE newState = ServerMonitoringState.addState( data.state , addState );
		if( newState != data.state ) {
			data.setState( newState );
			super.trigger( EVENT_MONITORCHILDCHANGED , data );
			return( true );
		}
		
		return( false );
	}

	public void setLog( String[] log ) {
		this.log = log;
	}

	public String[] getLog() {
		return( log );
	}
	
}
