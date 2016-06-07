package org.urm.server.action;

import org.urm.server.CommandExecutor;
import org.urm.server.meta.Metadata;

public class ActionInit extends ActionBase {

	public CommandAction commandAction;
	public String actionName;
	
	public ActionInit( CommandExecutor executor , CommandContext context , CommandOutput output , Metadata meta , CommandAction commandAction , String actionName ) {
		super( executor , context , output , meta );
		this.commandAction = commandAction;
		this.actionName = actionName;
	}

	protected boolean executeScopeTarget( ActionScopeTarget scopeProject ) throws Exception {
		exit( "unexpected operation" );
		return( false );
	}
	
}
