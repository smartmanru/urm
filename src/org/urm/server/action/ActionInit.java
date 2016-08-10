package org.urm.server.action;

import org.urm.server.SessionContext;
import org.urm.server.meta.FinalLoader;
import org.urm.server.meta.FinalMetaProduct;
import org.urm.server.meta.FinalRegistry;
import org.urm.server.storage.Artefactory;

public class ActionInit extends ActionBase {

	public CommandAction commandAction;
	public String actionName;

	public ActionInit( SessionContext session , Artefactory artefactory , CommandExecutor executor , CommandContext context , CommandOutput output , CommandAction commandAction , String actionName ) {
		super( session , artefactory , executor , context , output );
		this.actionInit = this;
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

	public void setServerSystemProductLayout( String name ) throws Exception {
		FinalRegistry registry = getRegistry();
		FinalMetaProduct product = registry.getProduct( this , name ); 
		session.setServerSystemProductLayout( product.NAME , product.PATH );
		meta.clearAll();
	}
	
	public void clearServerProductLayout() throws Exception {
		session.clearServerProductLayout();
		meta.clearAll();
	}
	
	public FinalRegistry getRegistry() {
		FinalLoader loader = engine.metaLoader;
		return( loader.getRegistry( this ) );
	}
	
	public FinalLoader getMetaLoader() {
		return( engine.metaLoader );
	}

}
