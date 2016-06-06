package org.urm.server.action;

import org.urm.common.action.CommandExecutor;
import org.urm.common.action.CommandOptions;
import org.urm.server.meta.Metadata;

public class ActionInit extends ActionBase {

	public ActionInit( CommandExecutor executor , CommandContext context , CommandOptions options , CommandOutput output , Metadata meta ) {
		super( executor , context , options , output , meta );
	}

	protected boolean executeScopeTarget( ActionScopeTarget scopeProject ) throws Exception {
		exit( "unexpected operation" );
		return( false );
	}
	
}
