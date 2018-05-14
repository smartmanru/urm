package org.urm.meta.env;

import org.urm.common.RunErrorClass;

public class _Error {

	public static final int ErrorBase = RunErrorClass.BaseMetaEnv;
	public static final int ErrorInternalBase = ErrorBase + RunErrorClass.InternalBase;

	public static final int UnknownSegment1 = ErrorBase + 1;
	public static final int NoSegmentDefined0 = ErrorBase + 2;
	public static final int UnknownDeploymentPath1 = ErrorBase + 3;
	public static final int InvalidServerNode2 = ErrorBase + 4;
	public static final int MissingServerPrimaryNode1 = ErrorBase + 5;
	public static final int MissingStandbyNode0 = ErrorBase + 6;
	public static final int MissingActiveNode0 = ErrorBase + 7;
	public static final int RootpathEmptyRequiredForDeployments1 = ErrorBase + 8;
	public static final int RootpathEmptyRequiredForGeneric1 = ErrorBase + 9;
	public static final int UnknownServer1 = ErrorBase + 10;
	public static final int MissingProxyNode0 = ErrorBase + 11;
	public static final int MissingRootPath1 = ErrorBase + 12;
	public static final int MissingBinPath1 = ErrorBase + 13;
	public static final int DuplicateComponent1 = ErrorBase + 14;
	public static final int DuplicateBinaryItem1 = ErrorBase + 15;
	public static final int DuplicateConfItem1 = ErrorBase + 16;
	public static final int DuplicateSchemaItem1 = ErrorBase + 17;
}
