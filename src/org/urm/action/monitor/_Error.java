package org.urm.action.monitor;

import org.urm.common.RunError;

public class _Error {

	public static final int ErrorBase = RunError.BaseActionMonitor;
	public static final int ErrorInternalBase = ErrorBase + RunError.InternalBase;

	public static final int MonitorEnvFailed0 = ErrorBase + 1;
	public static final int MonitorEnvHttpFailed1 = ErrorBase + 2;
	public static final int MonitorEnvHttpPostFailed1 = ErrorBase + 3;
	public static final int MonitorEnvWebServiceFailed1 = ErrorBase + 4;
	public static final int MonitorTargetFailed1 = ErrorBase + 3;
	
}
