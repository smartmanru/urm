package ru.egov.urm.run.database;

import ru.egov.urm.meta.MetaEnvServer;
import ru.egov.urm.run.ActionBase;
import ru.egov.urm.run.ActionScope;
import ru.egov.urm.run.CommandExecutor;
import ru.egov.urm.storage.DistStorage;

public class DatabaseCommandImpl {

	CommandExecutor executor;
	
	public DatabaseCommandImpl( CommandExecutor executor ) {
		this.executor = executor;
	}

	public void getReleaseScripts( ActionBase action , ActionScope scope , DistStorage dist ) throws Exception {
		ActionGetDB ma = new ActionGetDB( action , null , dist );
		ma.runAll( scope );
	}

	public void applyManual( ActionBase action , ActionScope scope , DistStorage dist , MetaEnvServer server ) throws Exception {
		ActionApplyManual ma = new ActionApplyManual( action , null , dist , server );
		ma.runAll( scope );
	}

}
