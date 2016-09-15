package org.urm.action.xdoc;

import org.urm.common.RunError;

public class _Error {

	public static final int ErrorBase = RunError.BaseActionXDoc;
	public static final int ErrorInternalBase = ErrorBase + RunError.InternalBase;
	
	public static final int UnknownDesignCommand1 = ErrorBase + 1;
	public static final int UnknownProdDesignServer1 = ErrorBase + 2;
	public static final int ProdServerNotDesign1 = ErrorBase + 3;
}
