package org.urm.engine.data;

import org.urm.common.RunErrorClass;

public class _Error {

	public static final int ErrorBase = RunErrorClass.BaseEngineData;
	public static final int ErrorInternalBase = ErrorBase + RunErrorClass.InternalBase;
	
	public static final int UnknownSystem1 = ErrorBase + 1;
	public static final int UnknownProduct1 = ErrorBase + 2;
	public static final int DuplicateSystemNameUnmatched1 = ErrorBase + 3;
	public static final int DuplicateProductNameUnmatched1 = ErrorBase + 4;
	
}
