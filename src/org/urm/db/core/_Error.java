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
	
}
