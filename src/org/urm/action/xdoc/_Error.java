package org.urm.action.xdoc;

import org.urm.common.ExitException;

public class _Error {

	public static int ErrorBase = ExitException.BaseServerActionXDoc;
	
	public static final int UnknownDesignCommand1 = ErrorBase + 1;
	public static final int UnknownProdDesignServer1 = ErrorBase + 2;
	public static final int ProdServerNotDesign1 = ErrorBase + 3;
}
