package org.urm.engine.executor;

import org.urm.common.RunErrorClass;

public class _Error {

	public static final int ErrorBase = RunErrorClass.BaseEngineExecutor;
	public static final int ErrorInternalBase = ErrorBase + RunErrorClass.InternalBase;
	
	public static final int ScopeEmpty0 = ErrorBase + 1;
	public static final int InvalidHostIP = ErrorBase + 2;
	public static final int InvalidActionValue1 = ErrorBase + 3;
	public static final int InvalidHostsCommand1 = ErrorBase + 4;
	public static final int UnexpectedCumulativeParameters0 = ErrorBase + 6;
	public static final int MissingSegmentName0 = ErrorBase + 7;
	
}
