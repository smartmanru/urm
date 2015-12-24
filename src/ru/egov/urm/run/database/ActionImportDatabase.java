package ru.egov.urm.run.database;

import ru.egov.urm.run.ActionBase;

public class ActionImportDatabase extends ActionBase {

	String SPECFILE;
	String CMD;
	String SCHEMA;
	
	public ActionImportDatabase( ActionBase action , String stream , String SPECFILE , String CMD , String SCHEMA ) {
		super( action , stream );
		this.SPECFILE = SPECFILE;
		this.CMD = CMD;
		this.SCHEMA = SCHEMA;
	}

	@Override protected boolean executeSimple() throws Exception {
		return( true );
	}
	
}
