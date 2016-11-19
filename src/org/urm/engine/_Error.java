package org.urm.engine;

import org.urm.common.RunErrorClass;

public class _Error {

	public static final int ErrorBase = RunErrorClass.BaseEngine;
	public static final int ErrorInternalBase = ErrorBase + RunErrorClass.InternalBase;
	
	public static final int MainExecutorExpected0 = ErrorBase + 1;
	public static final int UnknownCommandExecutor1 = ErrorBase + 12;
	public static final int TransactionAborted0 = ErrorBase + 22;
	public static final int TransactionMissingResourceChanges0 = ErrorBase + 23;
	public static final int TransactionMissingBuildersChanges0 = ErrorBase + 24;
	public static final int TransactionMissingDirectoryChanges0 = ErrorBase + 25;
	public static final int TransactionMissingSettingsChanges0 = ErrorBase + 26;
	public static final int TransactionMissingMetadataChanges0 = ErrorBase + 27;
	public static final int MasterpathEmpty0 = ErrorBase + 28;
	public static final int UnknownCallSession1 = ErrorBase + 31;
	public static final int InternalTransactionError1 = ErrorBase + 32;
	public static final int TransactionMissingInfrastructureChanges0 = ErrorBase + 33;
	public static final int TransactionMissingBaseChanges0 = ErrorBase + 34;
	
}
