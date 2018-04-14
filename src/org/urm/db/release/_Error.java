package org.urm.db.release;

import org.urm.common.RunErrorClass;

public class _Error {

	public static final int ErrorBase = RunErrorClass.BaseDBRelease;
	public static final int ErrorInternalBase = ErrorBase + RunErrorClass.InternalBase;
	
	public static final int ReleaseAlreadyExists1 = ErrorBase + 1;
	public static final int ReleasePhaseInvalidStart2 = ErrorBase + 2;
	public static final int ReleasePhaseInvalidFinish2 = ErrorBase + 3;
	
}
