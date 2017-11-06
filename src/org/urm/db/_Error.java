package org.urm.db;

import org.urm.common.RunErrorClass;

public class _Error {

	public static final int ErrorBase = RunErrorClass.BaseDB;
	public static final int ErrorInternalBase = ErrorBase + RunErrorClass.InternalBase;
	
	public static final int InvalidEnumIntType2 = ErrorBase + 1;
	public static final int MissingEnumIntType1 = ErrorBase + 2;
	public static final int InvalidEnumStringType2 = ErrorBase + 3;
	public static final int MissingEnumStringType1 = ErrorBase + 4;
	
}
