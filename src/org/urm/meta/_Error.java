package org.urm.meta;

import org.urm.common.RunErrorClass;

public class _Error {

	public static final int ErrorBase = RunErrorClass.BaseMetaCommon;
	public static final int ErrorInternalBase = ErrorBase + RunErrorClass.InternalBase;
	
	public static final int UnknownSessionProduct1 = ErrorBase + 13;
	public static final int UnusableProductMetadata1 = ErrorBase + 14;
	public static final int MissingSecretProperties0 = ErrorBase + 15;
	public static final int NoProductID = ErrorBase + 18;
}
