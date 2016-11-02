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

	public void setObject( ServerObject object ) {
		this.object = object;
	}
	
	public void clearState() {
		data.setState( MONITORING_STATE.MONITORING_NOMONITORING );
	}
	
	public boolean setState( SCOPESTATE state ) {
		MONITORING_STATE newState = ServerMonitoringState.getState( state );
		return( setState( newState ) );
	}
	
	public boolean setState( MONITORING_STATE newState ) {
		if( newState != data.state ) {
			data.setState( newState );
			super.trigger( ServerMonitoring.EVENT_MONITORSTATECHANGED , data );
			return( true );
		}
		
		return( false );
	}
	
	public boolean addState( SCOPESTATE state ) {
		MONITORING_STATE addState = ServerMonitoringState.getState( state );
		MONITORING_STATE newState = ServerMonitoringState.addState( data.state , addState );
		if( newState != data.state ) {
			data.setState( newState );
			super.trigger( ServerMonitoring.EVENT_MONITORCHILDCHANGED , data );
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

	public void customEvent( int eventType , Object data ) {
		super.trigger( eventType , data );
	}
	
}
