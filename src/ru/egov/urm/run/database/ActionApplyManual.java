package ru.egov.urm.run.database;

import ru.egov.urm.meta.MetaEnvServer;
import ru.egov.urm.run.ActionBase;
import ru.egov.urm.run.ActionScopeTarget;
import ru.egov.urm.run.ActionScopeTargetItem;
import ru.egov.urm.storage.DistStorage;

public class ActionApplyManual extends ActionBase {

	DistStorage release;
	MetaEnvServer server;
	
	public ActionApplyManual( ActionBase action , String stream , DistStorage release , MetaEnvServer server ) {
		super( action , stream );
		this.release = release;
		this.server = server;
	}

	@Override protected boolean executeScopeTarget( ActionScopeTarget target ) throws Exception {
		log( "apply manual database items ..." );
		for( ActionScopeTargetItem item : target.getItems( this ) )
			applyManual( item.NAME );
			
		return( true );
	}

	private void applyManual( String index ) throws Exception {
	}
	
}
