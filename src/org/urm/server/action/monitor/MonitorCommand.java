package org.urm.server.action.monitor;

import org.urm.server.action.ActionBase;
import org.urm.server.meta.MetaMonitoring;

public class MonitorCommand {

	MetaMonitoring mon;
	
	public MonitorCommand( MetaMonitoring mon ) {
		this.mon = mon;
	}

	public void runMonitor( ActionBase action ) throws Exception {
		ActionMonitorTop ca = new ActionMonitorTop( action , null , mon );
		ca.runSimple();
	}

}
