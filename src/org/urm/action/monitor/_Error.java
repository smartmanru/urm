package org.urm.action.monitor;

import org.urm.common.ExitException;

public class _Error {

	public static int ErrorBase = ExitException.BaseActionMonitor;

	public static final int MonitorEnvFailed0 = ErrorBase + 1;
	public static final int MonitorEnvHttpFailed1 = ErrorBase + 2;
	public static final int MonitorEnvHttpPostFailed1 = ErrorBase + 3;
	public static final int MonitorEnvWebServiceFailed1 = ErrorBase + 4;
	public static final int MonitorTargetFailed1 = ErrorBase + 3;
	
}
