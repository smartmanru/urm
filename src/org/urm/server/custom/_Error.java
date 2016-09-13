package org.urm.server.custom;

import org.urm.common.ExitException;

public class _Error {

	public static int ErrorBase = ExitException.BaseServerCustom;
	
	public static final int CustomBuildNotSet0 = ErrorBase + 1;
	public static final int UnableLoadCustomBuild1 = ErrorBase + 2;
	public static final int CustomDeployNotSet0 = ErrorBase + 3;
	public static final int UnableLoadCustomDeploy1 = ErrorBase + 4;
	public static final int CustomDatabaseNotSet0 = ErrorBase + 5;
	public static final int UnableLoadCustomDatabase1 = ErrorBase + 6;

}
