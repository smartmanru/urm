package org.urm.server;

import org.urm.server.action.ActionBase;
import org.urm.server.meta.FinalMetaSystem;

public class ServerTransaction {

	public ActionBase action;
	
	public ServerTransaction( ActionBase action ) {
		this.action = action;
	}
	
	public void addSystem( FinalMetaSystem system ) throws Exception {
		system.registry.addSystem( this , system );
	}
	
	public void modifySystem( FinalMetaSystem system , FinalMetaSystem systemNew ) throws Exception {
		system.modifySystem( this , systemNew );
	}

	public void deleteSystem( FinalMetaSystem system , boolean fsDeleteFlag , boolean vcsDeleteFlag , boolean logsDeleteFlag ) throws Exception {
		action.artefactory.deleteSystemResources( this , system , fsDeleteFlag , vcsDeleteFlag , logsDeleteFlag );
		system.registry.deleteSystem( this , system );
	}
	
}
