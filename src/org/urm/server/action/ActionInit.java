package org.urm.server.action;

import org.urm.server.SessionContext;
import org.urm.server.meta.FinalMetaLoader;
import org.urm.server.storage.Artefactory;

public class ActionInit extends ActionBase {

	public CommandAction commandAction;
	public String actionName;
	
	public ActionInit( SessionContext session , Artefactory artefactory , CommandExecutor executor , CommandContext context , CommandOutput output , CommandAction commandAction , String actionName ) {
		super( session , artefactory , executor , context , output );
		this.commandAction = commandAction;
		this.actionName = actionName;
	}

	@Override
	protected void runBefore() throws Exception {
		exit( "unexpected operation" );
	}
	
	@Override
	protected void runBefore( ActionScope scope ) throws Exception {
		exit( "unexpected operation" );
	}
	
	public FinalMetaLoader getMetaLoader() {
		return( engine.metaLoader );
	}
	
}
