package org.urm.server.action;

import org.urm.server.CommandExecutor;
import org.urm.server.meta.Metadata;
import org.urm.server.storage.Artefactory;

public class ActionInit extends ActionBase {

	public CommandAction commandAction;
	public String actionName;
	
	public ActionInit( Artefactory artefactory , CommandExecutor executor , CommandContext context , CommandOutput output , Metadata meta , CommandAction commandAction , String actionName ) {
		super( artefactory , executor , context , output , meta );
		this.commandAction = commandAction;
		this.actionName = actionName;
	}

	protected boolean executeScopeTarget( ActionScopeTarget scopeProject ) throws Exception {
		exit( "unexpected operation" );
		return( false );
	}
	
}
