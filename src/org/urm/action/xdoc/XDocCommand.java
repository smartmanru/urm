package org.urm.action.xdoc;

import org.urm.action.ActionBase;
import org.urm.common.action.CommandMethodMeta.SecurityAction;
import org.urm.meta.product.Meta;

public class XDocCommand {

	public XDocCommand() {
	}

	public void createDesignDoc( ActionBase action , Meta meta , String CMD , String OUTDIR ) throws Exception {
		ActionCreateDesignDoc ma = new ActionCreateDesignDoc( action , null , meta , CMD , OUTDIR );
		ma.runSimpleProduct( null , meta.name , SecurityAction.ACTION_XDOC , false );
	}

}
