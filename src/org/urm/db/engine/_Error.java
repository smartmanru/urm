package org.urm.db.engine;

import org.urm.common.RunErrorClass;

public class _Error {

	public static final int ErrorBase = RunErrorClass.BaseDBEngine;
	public static final int ErrorInternalBase = ErrorBase + RunErrorClass.InternalBase;
	
	public static final int GroupNotEmpty0 = ErrorBase + 1;
	public static final int DatacenterNotEmpty0 = ErrorBase + 2;
	public static final int NetworkNotEmpty0 = ErrorBase + 3;
	public static final int HostNotEmpty0 = ErrorBase + 4;
	public static final int DuplicateBuilder1 = ErrorBase + 5;
	public static final int DuplicateSystem1 = ErrorBase + 6;
	public static final int SystemNotEmpty0 = ErrorBase + 7;
	public static final int UnknownProduct1 = ErrorBase + 8;
	public static final int UnableCreateProduct0 = ErrorBase + 9;
	public static final int UnableDeleteUsedParam2 = ErrorBase + 10;
	public static final int GroupHasActive0 = ErrorBase + 11;
	public static final int GroupIsOffline0 = ErrorBase + 12;
	public static final int ItemIsInvalid0 = ErrorBase + 13;
	public static final int ItemInvalidData0 = ErrorBase + 14;
	
}
