package org.urm.action.database;

import org.urm.common.ExitException;

public class _Error {

	public static int ErrorBase = ExitException.BaseActionDatabase;
	
	public static final int ConnectFailed1 = ErrorBase + 1;
	public static final int NoRegionSet1 = ErrorBase + 2;
	public static final int ErrorApplyingScript1 = ErrorBase + 3;
	public static final int UnableFindManualFile1 = ErrorBase + 4;
	public static final int UnableConnectAdmin0 = ErrorBase + 5;
	public static final int UnknownServerSchema1 = ErrorBase + 6;
	public static final int ExportAlreadyRunning0 = ErrorBase + 7;
	public static final int UnableStartExport0 = ErrorBase + 8;
	public static final int ExportProcessErrors0 = ErrorBase + 9;
	public static final int UnableFindFiles1 = ErrorBase + 10;
	public static final int MissingDataFolder1 = ErrorBase + 11;
	public static final int NoMetadataDumpFiles1 = ErrorBase + 12;
	public static final int NoSchemaDataDumpFiles2 = ErrorBase + 13;
	public static final int ImportAlreadyRunning0 = ErrorBase + 14;
	public static final int UnableStartImport1 = ErrorBase + 15;
	public static final int ImportProcessErrors0 = ErrorBase + 16;
	public static final int UnableInitializeDatabase0 = ErrorBase + 17;
	public static final int UnknownReleaseVersion1 = ErrorBase + 18;
	public static final int UnexpectedManageCommand1 = ErrorBase + 19;
	public static final int ReleaseFinished0 = ErrorBase + 20;
	public static final int PasswordFileNotExist1 = ErrorBase + 21;
	public static final int UnableFindPassword3 = ErrorBase + 22;
	public static final int UnableDeriveAuthType0 = ErrorBase + 23;
	public static final int NeedCheckConnectivity0 = ErrorBase + 24;
	public static final int DatabaseModeNotSet0 = ErrorBase + 25;
	public static final int SqlFolderNotExist0 = ErrorBase + 26;
	public static final int DatabaseFileSetCheckFailed0 = ErrorBase + 27;
	public static final int InvalidDatabaseFolder1 = ErrorBase + 28;
	public static final int InvalidReleaseVersion1 = ErrorBase + 29;
	public static final int UnexpectedReleaseStatus1 = ErrorBase + 30;
	public static final int UnableRegisterExecution1 = ErrorBase + 31;
	public static final int InvalidScriptName1 = ErrorBase + 32;
	public static final int ScriptApplyError1 = ErrorBase + 33;
	public static final int UnexpectedOutput1 = ErrorBase + 34;
	public static final int UnexpectedTableRow3 = ErrorBase + 35;
	public static final int InvalidColumnMeta0 = ErrorBase + 36;
	public static final int ApplyFailed0 = ErrorBase + 37;
	public static final int PostRefreshApplyFailed1 = ErrorBase + 38;
	
}
