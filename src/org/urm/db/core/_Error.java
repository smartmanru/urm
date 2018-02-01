package org.urm.db.core;

import org.urm.common.RunErrorClass;

public class _Error {

	public static final int ErrorBase = RunErrorClass.BaseDBCore;
	public static final int ErrorInternalBase = ErrorBase + RunErrorClass.InternalBase;
	
	public static final int DuplicateCustomVar1 = ErrorBase + 1;
	public static final int OverrideAppVar1 = ErrorBase + 2;
	public static final int SetSystemVarAsCustom1 = ErrorBase + 3;
	public static final int UnknownAppVar1 = ErrorBase + 4;
	public static final int UnexpectedCustom1 = ErrorBase + 5;
	public static final int SettingsImportErrors1 = ErrorBase + 6;
	public static final int UnexpectedNameNull1 = ErrorBase + 7;
	public static final int UnexpectedAppVar1 = ErrorBase + 8;
	public static final int UnexpectedCustomVar1 = ErrorBase + 9;
	
	public static final int InvalidEnumIntType2 = ErrorBase + 11;
	public static final int MissingEnumIntType1 = ErrorBase + 12;
	public static final int InvalidEnumStringType2 = ErrorBase + 13;
	public static final int MissingEnumStringType1 = ErrorBase + 14;
	public static final int UnexpectedEnum1 = ErrorBase + 15;
	public static final int UnexpectedEnumItem2 = ErrorBase + 16;
	public static final int MissingEnum1 = ErrorBase + 17;
	public static final int MissingEnumItem2 = ErrorBase + 18;
	public static final int InvalidEnum3 = ErrorBase + 19;
	public static final int SetSystemVarWrongPlace1 = ErrorBase + 20;
	
}
