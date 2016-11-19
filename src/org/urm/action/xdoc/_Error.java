package org.urm.action.xdoc;

import org.urm.common.RunErrorClass;

public class _Error {

	public static final int ErrorBase = RunErrorClass.BaseActionXDoc;
	public static final int ErrorInternalBase = ErrorBase + RunErrorClass.InternalBase;
	
	public static final int UnknownDesignCommand1 = ErrorBase + 1;
	public static final int UnknownProdDesignServer1 = ErrorBase + 2;
	public static final int ProdServerNotDesign1 = ErrorBase + 3;
}
