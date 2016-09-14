package org.urm.action.xdoc;

import org.urm.action.ActionBase;

public class XDocCommand {

	public XDocCommand() {
	}

	public void createDesignDoc( ActionBase action , String CMD , String OUTDIR ) throws Exception {
		ActionCreateDesignDoc ma = new ActionCreateDesignDoc( action , null , CMD , OUTDIR );
		ma.runSimple();
	}

}
