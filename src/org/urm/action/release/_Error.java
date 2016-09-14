package org.urm.action.release;

import org.urm.common.ExitException;

public class _Error {

	public static int ErrorBase = ExitException.BaseActionRelease;
	
	public static final int OperationCancelled0 = ErrorBase + 1;
	public static final int ReleaseSetChangeErrors0 = ErrorBase + 2;
	public static final int CannotChangeCumulative0 = ErrorBase + 3;
	public static final int CannotBuildCumulative0 = ErrorBase + 4;
	public static final int CannotDownloadCumulative0 = ErrorBase + 5;
	public static final int NotCumulativeRelease0 = ErrorBase + 6;
	public static final int AddCumulativeVersionFailed1 = ErrorBase + 7;
	
}
