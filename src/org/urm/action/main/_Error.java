package org.urm.action.main;

import org.urm.common.RunErrorClass;

public class _Error {

	public static final int ErrorBase = RunErrorClass.BaseActionMain;
	public static final int ErrorInternalBase = ErrorBase + RunErrorClass.InternalBase;
	
	public static final int InstallationNotConfigured0 = ErrorBase + 1;
	public static final int UnknownEnvironment1 = ErrorBase + 2;
	public static final int UnableSaveProduct1 = ErrorBase + 3;
	public static final int UnexpectedServerAction1 = ErrorBase + 4;
	public static final int ServerUnknownState1 = ErrorBase + 5;
	public static final int UnableStopServer2 = ErrorBase + 6;
	
}
