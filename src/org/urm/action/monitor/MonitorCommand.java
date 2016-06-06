package org.urm.action.monitor;

import org.urm.action.ActionBase;
import org.urm.meta.MetaMonitoring;

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
