package org.urm.meta.release;

import org.urm.common.RunErrorClass;

public class _Error {

	public static final int ErrorBase = RunErrorClass.BaseMetaRelease;
	public static final int ErrorInternalBase = ErrorBase + RunErrorClass.InternalBase;
	
	public static final int UnknownReleaseTicketSet2 = ErrorBase + 1;
	public static final int UnknownReleaseBuildTarget2 = ErrorBase + 2;
	public static final int UnknownReleaseDistTarget2 = ErrorBase + 3;
	
}
