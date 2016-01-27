package ru.egov.urm.run.xdoc;

import ru.egov.urm.run.ActionBase;

public class XDocCommand {

	public XDocCommand() {
	}

	public void createDesignDoc( ActionBase action , String CMD , String OUTDIR ) throws Exception {
		ActionCreateDesignDoc ma = new ActionCreateDesignDoc( action , null , CMD , OUTDIR );
		ma.runSimple();
	}

}
