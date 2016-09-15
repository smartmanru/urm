package org.urm.engine;

import org.urm.common.RunError;

public class _Error {

	public static final int ErrorBase = RunError.BaseEngine;
	public static final int ErrorInternalBase = ErrorBase + RunError.InternalBase;
	
	public static final int MainExecutorExpected0 = ErrorBase + 1;
	public static final int UnknownBuilder1 = ErrorBase + 2;
	public static final int ResourceAlreadyExists1 = ErrorBase + 3;
	public static final int UnknownResource1 = ErrorBase + 4;
	public static final int BuilderAlreadyExists1 = ErrorBase + 5;
	public static final int TransactionResourceOld1 = ErrorBase + 6;
	public static final int DuplicateSystem1 = ErrorBase + 7;
	public static final int TransactionSystemOld1 = ErrorBase + 8;
	public static final int UnknownSystem1 = ErrorBase + 9;
	public static final int UnknownProduct1 = ErrorBase + 10;
	public static final int DuplicateProduct1 = ErrorBase + 11;
	public static final int UnknownCommandExecutor1 = ErrorBase + 12;
	public static final int UnknownSessionProduct1 = ErrorBase + 13;
	public static final int UnusableProductMetadata1 = ErrorBase + 14;
	public static final int MissingSecretProperties0 = ErrorBase + 15;
	public static final int UnablePublishRepository0 = ErrorBase + 16;
	public static final int NonEmptyRepository1 = ErrorBase + 17;
	public static final int NoProductID = ErrorBase + 18;
	public static final int TransactionBuilderOld1 = ErrorBase + 19;
	public static final int DuplicateResource1 = ErrorBase + 20;
	public static final int UnableReadEnginePropertyFile1 = ErrorBase + 21;
	public static final int TransactionAborted0 = ErrorBase + 22;
	public static final int TransactionMissingResourceChanges0 = ErrorBase + 23;
	public static final int TransactionMissingBuildersChanges0 = ErrorBase + 24;
	public static final int TransactionMissingDirectoryChanges0 = ErrorBase + 25;
	public static final int TransactionMissingSettingsChanges0 = ErrorBase + 26;
	public static final int TransactionMissingMetadataChanges0 = ErrorBase + 27;
	public static final int MasterpathEmpty0 = ErrorBase + 28;
	public static final int MissingProductFolder1 = ErrorBase + 29;
	public static final int ProductpathEmpty0 = ErrorBase + 30;
	public static final int UnknownCallSession1 = ErrorBase + 31;
	public static final int InternalTransactionError1 = ErrorBase + 32;
	
}
