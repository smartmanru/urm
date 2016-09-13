package org.urm.server.vcs;

import org.urm.common.ExitException;

public class _Error {

	public static int ErrorBase = ExitException.BaseServerVCS;
	
	public static final int UnexectedVcsType2 = ErrorBase + 1;
	public static final int NonSvnResource1 = ErrorBase + 2;
	public static final int NonGitResource1 = ErrorBase + 3;
	public static final int Unable—ommit0 = ErrorBase + 4;
	public static final int UnableCloneRepository2 = ErrorBase + 5;
	public static final int MissingLocalDirectory1 = ErrorBase + 6;
	public static final int LocalDirectoryShouldNotExist1 = ErrorBase + 7;
	public static final int LocalFileOrDirectoryShouldNotExist1 = ErrorBase + 8;
	public static final int UnablePushOrigin1 = ErrorBase + 9;
	public static final int UnableFetchOrigin1 = ErrorBase + 10;
	public static final int MissingBranchDateRevision0 = ErrorBase + 11;
	public static final int MissingMirrorDirectory1 = ErrorBase + 12;
	public static final int MirrorDirectoryAlreadyExists1 = ErrorBase + 13;
	public static final int MissingMirrorPathParameter0 = ErrorBase + 14;
	public static final int MissingSvnPath1 = ErrorBase + 15;
	public static final int UnableCheckRepositoryPath0 = ErrorBase + 16;
	public static final int UnableCheckOut1 = ErrorBase + 17;
	public static final int NotUnderVersionControl1 = ErrorBase + 18;
	
}
