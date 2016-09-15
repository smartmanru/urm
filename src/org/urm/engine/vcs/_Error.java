package org.urm.engine.vcs;

import org.urm.common.RunError;

public class _Error {

	public static final int ErrorBase = RunError.BaseEngineVCS;
	public static final int ErrorInternalBase = ErrorBase + RunError.InternalBase;
	
	public static final int UnexectedVcsType2 = ErrorBase + 1;
	public static final int NonSvnResource1 = ErrorBase + 2;
	public static final int NonGitResource1 = ErrorBase + 3;
	public static final int UnableCommit0 = ErrorBase + 4;
	public static final int UnableCloneRepository2 = ErrorBase + 5;
	public static final int MissingLocalDirectory1 = ErrorBase + 6;
	public static final int LocalDirectoryShouldNotExist1 = ErrorBase + 7;
	public static final int LocalFileOrDirectoryShouldNotExist1 = ErrorBase + 8;
	public static final int UnablePushOrigin1 = ErrorBase + 9;
	public static final int UnableFetchOrigin1 = ErrorBase + 10;
	public static final int MissingBranchDateRevision0 = ErrorBase + 11;
	public static final int MissingRepoMirrorDirectory1 = ErrorBase + 12;
	public static final int CommitDirectoryAlreadyExists1 = ErrorBase + 13;
	public static final int MissingMirrorPathParameter0 = ErrorBase + 14;
	public static final int MissingSvnPath1 = ErrorBase + 15;
	public static final int UnableCheckRepositoryPath0 = ErrorBase + 16;
	public static final int UnableCheckOut1 = ErrorBase + 17;
	public static final int NotUnderVersionControl1 = ErrorBase + 18;
	public static final int MissingCommitDirectory1 = ErrorBase + 20;
	public static final int BareDirectoryAlreadyExists1 = ErrorBase + 21;
	public static final int MissingBareDirectory1 = ErrorBase + 22;
	public static final int GitUnableClone2 = ErrorBase + 23;
	public static final int MirrorDirectoryNotEmpty1 = ErrorBase + 24;
	public static final int UnableCreateRepoFolder1 = ErrorBase + 25;
	
}
