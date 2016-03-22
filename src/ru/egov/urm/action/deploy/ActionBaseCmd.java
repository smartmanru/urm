package ru.egov.urm.action.deploy;

import ru.egov.urm.action.ActionBase;
import ru.egov.urm.action.ActionScopeTarget;
import ru.egov.urm.action.ActionScopeTargetItem;
import ru.egov.urm.meta.MetaEnvServer;
import ru.egov.urm.meta.MetaEnvServerBase;
import ru.egov.urm.meta.MetaEnvServerNode;
import ru.egov.urm.storage.BaseRepository;

public class ActionBaseCmd extends ActionBase {

	String cmd;
	
	public ActionBaseCmd( ActionBase action , String stream , String cmd ) {
		super( action , stream );
		this.cmd = cmd;
	}

	@Override protected boolean executeScopeTarget( ActionScopeTarget target ) throws Exception {
		executeServer( target );
		return( true );
	}

	private void executeServer( ActionScopeTarget target ) throws Exception {
		MetaEnvServer server = target.envServer;
		MetaEnvServerBase base = server.base;
		log( "============================================ execute server=" + server.NAME + ", type=" + server.SERVERTYPE + " ..." );
		
		if( base == null ) {
			log( "server has no base defined. Skipped" );
			return;
		}
			
		log( "rootpath=" + server.ROOTPATH + ", base=" + base.ID );

		for( ActionScopeTargetItem item : target.getItems( this ) ) {
			MetaEnvServerNode node = item.envServerNode;
			log( cmd + " server=" + server.NAME + " node=" + node.POS + " ..." );
			executeNode( server , node , base );
		}
	}

	private void executeNode( MetaEnvServer server , MetaEnvServerNode node , MetaEnvServerBase base ) throws Exception {
		BaseRepository repo = artefactory.getBaseRepository( this );
		repo.getBaseInfo( this , base.ID );
	}
	
}
