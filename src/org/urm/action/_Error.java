package org.urm.action;

import org.urm.common.RunErrorClass;

public class _Error {

	public static final int ErrorBase = RunErrorClass.BaseAction;
	public static final int ErrorInternalBase = ErrorBase + RunErrorClass.InternalBase;
	
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
	public static final int SegmentUndefined0 = ErrorBase + 15;
	public static final int UnknownDistributiveItem1 = ErrorBase + 16;
	public static final int UnexpectedNonManualItem1 = ErrorBase + 17;
	public static final int BuildVersionNotSet0 = ErrorBase + 18;
	public static final int InternalActionError1 = ErrorBase + 19;
	public static final int MissingMirrorConfig0 = ErrorBase + 20;
	public static final int AccessDenied0 = ErrorBase + 21;
	public static final int MissingEnvironment0 = ErrorBase + 22;

}
