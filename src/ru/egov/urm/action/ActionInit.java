package ru.egov.urm.run;

import ru.egov.urm.meta.Metadata;

public class ActionInit extends ActionBase {

	public ActionInit( CommandExecutor executor , CommandContext context , CommandOptions options , CommandOutput output , Metadata meta ) {
		super( executor , context , options , output , meta );
	}

	protected boolean executeScopeTarget( ActionScopeTarget scopeProject ) throws Exception {
		exit( "unexpected operation" );
		return( false );
	}
	
}
