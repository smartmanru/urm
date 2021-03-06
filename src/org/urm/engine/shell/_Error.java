package org.urm.engine.shell;

import org.urm.common.RunErrorClass;

public class _Error {

	public static final int ErrorBase = RunErrorClass.BaseEngineShell;
	public static final int ErrorInternalBase = ErrorBase + RunErrorClass.InternalBase;
	
	public static final int MissingAccountDetails0 = ErrorBase + 1;
	public static final int InvalidDirectory1 = ErrorBase + 2;
	public static final int UnableGetWindowsProcess0 = ErrorBase + 3;
	public static final int ShellInputStreamError0 = ErrorBase + 4;
	public static final int TooManyFilesInPath3 = ErrorBase + 5;
	public static final int CannotFindFile2 = ErrorBase + 6;
	public static final int MissingDirectory1 = ErrorBase + 7;
	public static final int UnableReadDirectory1 = ErrorBase + 8;
	public static final int ErrorExecutingCmd1 = ErrorBase + 9;
	public static final int CheckCreateDirectoryError1 = ErrorBase + 10;
	public static final int CheckDirectoryError1 = ErrorBase + 11;
	public static final int RemoveDirectoryContentError1 = ErrorBase + 12;
	public static final int RemoveDirectoryError1 = ErrorBase + 13;
	public static final int ErrorsDeleteDirs1 = ErrorBase + 14;
	public static final int ErrorsDeleteFiles1 = ErrorBase + 15;
	public static final int ErrorsDeleteDirs2 = ErrorBase + 16;
	public static final int ErrorsDeleteFiles2 = ErrorBase + 17;
	public static final int UnexpectedDirContentLine1 = ErrorBase + 18;
	public static final int UnableGetMd5Sum1 = ErrorBase + 19;
	public static final int ErrorReadingFiles1 = ErrorBase + 20;
	public static final int ShellInitFailed1 = ErrorBase + 21;
	public static final int ErrorExecutingCmd2 = ErrorInternalBase + 22;
	public static final int NotDirectoryPath1 = ErrorBase + 23;
	public static final int UnableDownload1 = ErrorBase + 24;
	public static final int ErrorExecutingCmd3 = ErrorInternalBase + 25;
	public static final int RunCommandClosedSession1 = ErrorBase + 26;
	public static final int UnableObtainCommandStatus0 = ErrorBase + 27;
	public static final int UnableConnectHost1 = ErrorBase + 28;
	public static final int ServerShutdown0 = ErrorBase + 29;
	public static final int NotConnectUnavailableShell0 = ErrorBase + 30;
	public static final int UnableCreateLocalShell0 = ErrorBase + 31;
	public static final int UnableReadStream0 = ErrorBase + 32;
	public static final int MissingTextReader = ErrorBase + 33;
	public static final int CommandKilled = ErrorBase + 34;
	public static final int MissingAuthKey1 = ErrorBase + 35;
	public static final int MismatchedOsType2 = ErrorBase + 36;
	public static final int MissingAuthKeyData1 = ErrorBase + 37;
	public static final int MissingAuthPasswordData1 = ErrorBase + 38;
	public static final int InvalidAuthData1 = ErrorBase + 39;
	public static final int ScpMissingDestinationDirectory1 = ErrorBase + 40;
	public static final int ScpDestinationCannotBeFile1 = ErrorBase + 41;
	public static final int ScpDestinationAlreadyExists1 = ErrorBase + 42;
	public static final int UnableCreateDirectory1 = ErrorBase + 43;
	public static final int UnknownDatacenter1 = ErrorBase + 44;
	public static final int AccessTypeNotSupported1 = ErrorBase + 45;
	public static final int UnableCreateRemoteShell0 = ErrorBase + 46;
	public static final int MissingDatacenter0 = ErrorBase + 47;
	public static final int UnableConnectAccount1 = ErrorBase + 48;
	public static final int ShellUnavailable1 = ErrorBase + 49;
	
}
