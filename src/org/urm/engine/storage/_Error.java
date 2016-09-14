package org.urm.engine.storage;

import org.urm.common.RunError;

public class _Error {

	public static int ErrorBase = RunError.BaseEngineStorage;
	
	public static final int RootNotPermitted0 = ErrorBase + 1;
	public static final int AttemptDeleteFiles0 = ErrorBase + 2;
	public static final int InvalidTableSetLine1 = ErrorBase + 3;
	public static final int UnableConnectAdminDatabase0 = ErrorBase + 4;
	public static final int StaticFileNotFound1 = ErrorBase + 5;
	public static final int NoContextInStaticFile1 = ErrorBase + 6;
	public static final int ContextDataNotFound2 = ErrorBase + 7;
	public static final int InvalidStateFile1 = ErrorBase + 8;
	public static final int UnknownVersionKey1 = ErrorBase + 9;
	public static final int UnableCreateConfigTar1 = ErrorBase + 10;
	public static final int ItemNotFoundInLive2 = ErrorBase + 11;
	public static final int UnknownVersionType1 = ErrorBase + 12;
	public static final int MissingTargetDirectory1 = ErrorBase + 13;
	public static final int MissingDeployDirectory1 = ErrorBase + 14;
	public static final int UnableExtractArchive1 = ErrorBase + 15;
	public static final int MissingLiveServiceFile1 = ErrorBase + 16;
	public static final int InvalidRootDir1 = ErrorBase + 17;
	public static final int InvalidRelativeDir1 = ErrorBase + 18;
	public static final int UnableExportMirror2 = ErrorBase + 19;
	public static final int MissingConfItem1 = ErrorBase + 20;
	public static final int UnableExportConfig2 = ErrorBase + 21;
	public static final int UnableExportConfigTag3 = ErrorBase + 22;
	public static final int DatabaseNotSupported2 = ErrorBase + 23;
	public static final int DuplicateBaseId2 = ErrorBase + 24;
	
}
