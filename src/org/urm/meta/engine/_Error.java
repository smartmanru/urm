package org.urm.meta.engine;

import org.urm.common.RunError;

public class _Error {

	public static final int ErrorBase = RunError.BaseEngine;
	public static final int ErrorInternalBase = ErrorBase + RunError.InternalBase;
	
	public static final int UnknownBuilder1 = ErrorBase + 2;
	public static final int UnknownResource1 = ErrorBase + 4;
	public static final int BuilderAlreadyExists1 = ErrorBase + 5;
	public static final int TransactionResourceOld1 = ErrorBase + 6;
	public static final int DuplicateSystem1 = ErrorBase + 7;
	public static final int TransactionSystemOld1 = ErrorBase + 8;
	public static final int UnknownSystem1 = ErrorBase + 9;
	public static final int UnknownProduct1 = ErrorBase + 10;
	public static final int DuplicateProduct1 = ErrorBase + 11;
	public static final int UnablePublishRepository0 = ErrorBase + 16;
	public static final int TransactionBuilderOld1 = ErrorBase + 19;
	public static final int DuplicateResource1 = ErrorBase + 20;
	public static final int ProductPathAlreadyExists1 = ErrorBase + 33;
	public static final int UnableReadEnginePropertyFile1 = ErrorBase + 34;
	public static final int DuplicateBaseItem1 = ErrorBase + 35;
	
}