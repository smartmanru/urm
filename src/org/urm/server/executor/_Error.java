package org.urm.server.executor;

import org.urm.common.ExitException;

public class _Error {

	public static int ErrorBase = ExitException.BaseServerExecutor;
	
	public static final int ScopeEmpty0 = ErrorBase + 1;
	public static final int InvalidHostIP = ErrorBase + 2;
	public static final int InvalidActionValue1 = ErrorBase + 3;
	public static final int InvalidHostsCommand1 = ErrorBase + 4;
	public static final int WrongArgs0 = ErrorBase + 5;
	public static final int UnexpectedCumulativeParameters0 = ErrorBase + 6;
	
}
