package org.urm.meta.engine;

import org.urm.engine.ServerEventsSource;
import org.urm.engine.ServerEventsState;
import org.urm.meta.ServerObject;

public class ServerMonitoringSource extends ServerEventsSource {

	public ServerMonitoring mon;
	public int level;
	public ServerObject object;
	public ServerMonitoringState data;
	
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

}
