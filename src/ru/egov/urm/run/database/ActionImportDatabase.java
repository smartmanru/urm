package ru.egov.urm.run.database;

import ru.egov.urm.meta.MetaEnvServer;
import ru.egov.urm.run.ActionBase;

public class ActionImportDatabase extends ActionBase {

	MetaEnvServer server;
	String SPECFILE;
	String CMD;
	String SCHEMA;
	
	public ActionImportDatabase( ActionBase action , String stream , MetaEnvServer server , String CMD , String SCHEMA ) {
		super( action , stream );
		this.server = server;
		this.SPECFILE = "import-" + server.NAME + ".conf";
		this.CMD = CMD;
		this.SCHEMA = SCHEMA;
	}

	@Override protected boolean executeSimple() throws Exception {
		return( true );
	}
	
}
