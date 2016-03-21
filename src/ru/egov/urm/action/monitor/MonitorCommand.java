package ru.egov.urm.run.monitor;

import ru.egov.urm.meta.MetaMonitoring;
import ru.egov.urm.run.ActionBase;

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
