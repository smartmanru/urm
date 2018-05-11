package org.urm.action.build;

import org.urm.common.RunErrorClass;

public class _Error {

	public static final int ErrorBase = RunErrorClass.BaseActionBuild;
	public static final int ErrorInternalBase = ErrorBase + RunErrorClass.InternalBase;
	
	public static final int UnknownBuilderMethod2 = ErrorBase + 1;
	public static final int TargetPathAlreadyExists1 = ErrorBase + 2;
	public static final int UnableCheckoutFile1 = ErrorBase + 3;
	public static final int UnableRegisterBuildStatus0 = ErrorBase + 4;
	public static final int NoProjectDefaultBranch1 = ErrorBase + 5;
	public static final int UnableCheckout1 = ErrorBase + 6;
	public static final int MissingUploadFile1 = ErrorBase + 7;
	public static final int BuildErrors0 = ErrorBase + 8;
	public static final int NoPropertiesInPom0 = ErrorBase + 9;
	public static final int UnexpectedBuilderVersion1 = ErrorBase + 10;
	public static final int ProjectBuildError1 = ErrorBase + 11;
	public static final int ProjectPatchError1 = ErrorBase + 12;
	
}
