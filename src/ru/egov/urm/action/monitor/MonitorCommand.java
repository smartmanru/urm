package ru.egov.urm.action.monitor;

import ru.egov.urm.action.ActionBase;
import ru.egov.urm.meta.MetaMonitoring;

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
