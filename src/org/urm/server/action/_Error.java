package org.urm.server.action;

import org.urm.common.ExitException;

public class _Error {

	public static int ErrorBase = ExitException.BaseServerAction;
	
	public static final int InternalError0 = ErrorBase + 1;
	public static final int NotImplemented0 = ErrorBase + 2;
	public static final int UnexpectedCategory1 = ErrorBase + 3;
	public static final int RequiredVariable1 = ErrorBase + 4;
	public static final int MissingSetName0 = ErrorBase + 5;
	public static final int TargetsWithoutSet0 = ErrorBase + 6;
	public static final int MissingTargets0 = ErrorBase + 7;
	public static final int MissingTargetItems0 = ErrorBase + 8;
	public static final int MissingServerNodes0 = ErrorBase + 9;
	public static final int MissingProjectItems0 = ErrorBase + 10;
	public static final int MissingServers0 = ErrorBase + 11;
	public static final int MissingDatabaseItems0 = ErrorBase + 12;
	public static final int MissingProject0 = ErrorBase + 13;
	public static final int MissingServer0 = ErrorBase + 14;
	public static final int DatacenterUndefined0 = ErrorBase + 15;
	public static final int UnknownDistributiveItem1 = ErrorBase + 16;
	public static final int UnexpectedNonManualItem1 = ErrorBase + 17;
	public static final int BuildVersionNotSet0 = ErrorBase + 18;
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
