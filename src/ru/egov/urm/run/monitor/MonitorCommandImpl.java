package ru.egov.urm.run.monitor;

import ru.egov.urm.meta.MetaMonitoring;
import ru.egov.urm.run.ActionBase;
import ru.egov.urm.run.CommandExecutor;

public class MonitorCommandImpl {

	CommandExecutor executor;
	MetaMonitoring mon;
	
	public MonitorCommandImpl( CommandExecutor executor , MetaMonitoring mon ) {
		this.executor = executor;
		this.mon = mon;
	}

	public void runMonitor( ActionBase action ) throws Exception {
		ActionMonitorTop ca = new ActionMonitorTop( action , null , mon );
		ca.runSimple();
	}

}
