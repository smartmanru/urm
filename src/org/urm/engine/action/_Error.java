package org.urm.engine.action;

import org.urm.common.RunError;

public class _Error {

	public static final int ErrorBase = RunError.BaseEngineAction;
	public static final int ErrorInternalBase = ErrorBase + RunError.InternalBase;
	
	public static final int ReleaseWrongBuildMode1 = ErrorBase + 19;
	public static final int UnknownFlagVar1 = ErrorBase + 20;
	public static final int UnknownEnumVar1 = ErrorBase + 21;
	public static final int UnknownParamVar1 = ErrorBase + 22;
	public static final int UnknownExecutorAction2 = ErrorBase + 23;
	public static final int NameUndefined1 = ErrorBase + 24;
	public static final int UnexpectedExtraArguments1 = ErrorBase + 25;
	public static final int ArgumentRequired1 = ErrorBase + 26;
	public static final int UnknownBuildMode1 = ErrorBase + 27;
	
}
