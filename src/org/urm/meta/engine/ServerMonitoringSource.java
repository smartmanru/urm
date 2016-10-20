package org.urm.meta.engine;

import org.urm.engine.ServerEventsSource;
import org.urm.engine.ServerEventsState;

public class ServerMonitoringSource extends ServerEventsSource {

	public ServerMonitoring mon;
	public int level;
	
	public ServerMonitoringSource( ServerMonitoring mon , int level , String name ) {
		super( mon.events , name );
		this.mon = mon;
		this.level = level;
	}
	
	@Override
	public ServerEventsState getStateData( int stateId ) {
		ServerMonitoringState state = new ServerMonitoringState( mon , level , stateId );
		if( level == ServerMonitoring.MONITORING_SYSTEMS )
			getSystemsState( state );
		else
		if( level == ServerMonitoring.MONITORING_PRODUCTS )
			getProductsState( state );
		else
		if( level == ServerMonitoring.MONITORING_ENVIRONMENTS )
			getEnvironmentsState( state );
		else
		if( level == ServerMonitoring.MONITORING_DATACENTERS )
			getDatacentersState( state );
		else
		if( level == ServerMonitoring.MONITORING_SERVERS )
			getServersState( state );
		else
		if( level == ServerMonitoring.MONITORING_NODES )
			getNodesState( state );
		return( state );
	}

	private void getSystemsState( ServerMonitoringState state ) {
	}
	
	private void getProductsState( ServerMonitoringState state ) {
	}
	
	private void getEnvironmentsState( ServerMonitoringState state ) {
	}
	
	private void getDatacentersState( ServerMonitoringState state ) {
	}
	
	private void getServersState( ServerMonitoringState state ) {
	}
	
	private void getNodesState( ServerMonitoringState state ) {
	}
	
}
