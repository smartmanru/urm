package org.urm.server.action;

import org.urm.server.CommandExecutor;
import org.urm.server.SessionContext;
import org.urm.server.storage.Artefactory;

public class ActionInit extends ActionBase {

	public CommandAction commandAction;
	public String actionName;
	
	public ActionInit( SessionContext session , Artefactory artefactory , CommandExecutor executor , CommandContext context , CommandOutput output , CommandAction commandAction , String actionName ) {
		super( session , artefactory , executor , context , output );
		this.commandAction = commandAction;
		this.actionName = actionName;
	}

	protected boolean executeScopeTarget( ActionScopeTarget scopeProject ) throws Exception {
		exit( "unexpected operation" );
		return( false );
	}
	
}
