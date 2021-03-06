package org.urm.action.release;

import org.urm.common.RunErrorClass;

public class _Error {

	public static final int ErrorBase = RunErrorClass.BaseActionRelease;
	public static final int ErrorInternalBase = ErrorBase + RunErrorClass.InternalBase;
	
	public static final int OperationCancelled0 = ErrorBase + 1;
	public static final int ReleaseSetChangeErrors0 = ErrorBase + 2;
	public static final int CannotChangeCumulative0 = ErrorBase + 3;
	public static final int CannotBuildCumulative0 = ErrorBase + 4;
	public static final int CannotDownloadCumulative0 = ErrorBase + 5;
	public static final int NotCumulativeRelease0 = ErrorBase + 6;
	public static final int AddCumulativeVersionFailed1 = ErrorBase + 7;
	public static final int CannotDropProd0 = ErrorBase + 8;
	public static final int ArchiveNotCompleted1 = ErrorBase + 9;
	public static final int CannotCopyProd0 = ErrorBase + 10;
	public static final int NotFinalizedProd0 = ErrorBase + 11;
	public static final int CannotChangeCompletedProd0 = ErrorBase + 12;
	public static final int CannotAppendIncompleteRelease0 = ErrorBase + 13;
	public static final int CannotSkipRelease1 = ErrorBase + 14;
	public static final int CannotAppendOlderRelease1 = ErrorBase + 15;
	public static final int InvalidSyntax0 = ErrorBase + 16;
	public static final int CannotExtendScope1 = ErrorBase + 17;
	public static final int CannotReduceScope1 = ErrorBase + 18;
	public static final int CannotFindMasterRelease1 = ErrorBase + 19;
	public static final int UnableFinalizeRelease0 = ErrorBase + 20;
	public static final int UnableFindDistributive1 = ErrorBase + 21;
	
}
