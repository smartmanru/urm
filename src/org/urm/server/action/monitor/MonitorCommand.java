package org.urm.server.action.monitor;

import org.urm.meta.MetaMonitoring;
import org.urm.server.action.ActionBase;

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
