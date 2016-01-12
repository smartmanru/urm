package ru.egov.urm.run.xdoc;

import ru.egov.urm.meta.MetaDesign;
import ru.egov.urm.run.ActionBase;

public class XDocCommand {

	public XDocCommand() {
	}

	public void createDesignDoc( ActionBase action , MetaDesign design , String CMD , String OUTFILE ) throws Exception {
		ActionCreateDesignDoc ma = new ActionCreateDesignDoc( action , null , design , CMD , OUTFILE );
		ma.runSimple();
	}

}
