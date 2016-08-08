package org.urm.server.action;

import org.urm.server.SessionContext;
import org.urm.server.meta.FinalLoader;
import org.urm.server.meta.FinalRegistry;
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
	
	public FinalLoader getMetaLoader() {
		return( engine.metaLoader );
	}
	
	public FinalRegistry changeRegistry( FinalRegistry sourceRegistry ) throws Exception {
		startTransaction();
		if( sourceRegistry == engine.metaLoader.getRegistry( this ) )
			return( sourceRegistry.copy( this ) );
		
		cancelTransaction();
		return( null );
	}

	private void startTransaction() throws Exception {
	}
	
	private void cancelTransaction() throws Exception {
	}
	
}
