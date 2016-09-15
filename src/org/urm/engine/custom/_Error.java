package org.urm.engine.custom;

import org.urm.common.RunError;

public class _Error {

	public static final int ErrorBase = RunError.BaseEngineCustom;
	public static final int ErrorInternalBase = ErrorBase + RunError.InternalBase;
	
	public static final int CustomBuildNotSet0 = ErrorBase + 1;
	public static final int UnableLoadCustomBuild1 = ErrorBase + 2;
	public static final int CustomDeployNotSet0 = ErrorBase + 3;
	public static final int UnableLoadCustomDeploy1 = ErrorBase + 4;
	public static final int CustomDatabaseNotSet0 = ErrorBase + 5;
	public static final int UnableLoadCustomDatabase1 = ErrorBase + 6;

}
