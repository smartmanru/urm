package ru.egov.urm.run.database;

import ru.egov.urm.meta.MetaEnvServer;
import ru.egov.urm.meta.MetaReleaseDelivery;
import ru.egov.urm.run.ActionBase;
import ru.egov.urm.run.ActionScopeTarget;
import ru.egov.urm.storage.DistStorage;

public class ActionApplyAutomatic extends ActionBase {

	DistStorage release;
	MetaReleaseDelivery delivery;
	String indexScope;
	
	public ActionApplyAutomatic( ActionBase action , String stream , DistStorage release , MetaReleaseDelivery delivery , String indexScope ) {
		super( action , stream );
		this.release = release;
		this.delivery = delivery;
		this.indexScope = indexScope;
	}

	@Override protected boolean executeScopeTarget( ActionScopeTarget target ) throws Exception {
		MetaEnvServer server = target.envServer;
		return( true );
	}
	
}
