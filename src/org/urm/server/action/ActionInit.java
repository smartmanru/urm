package org.urm.server.action;

import org.urm.server.ServerContext;
import org.urm.server.ServerProduct;
import org.urm.server.ServerRegistry;
import org.urm.server.SessionContext;
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

	public void setServerSystemProductLayout( ServerProduct product ) throws Exception {
		session.setServerSystemProductLayout( product.NAME , product.PATH );
		meta.clearAll();
	}
	
	public void setServerSystemProductLayout( String name ) throws Exception {
		ServerRegistry registry = getRegistry();
		ServerProduct product = registry.getProduct( name ); 
		setServerSystemProductLayout( product );
	}
	
	public void clearServerProductLayout() throws Exception {
		session.clearServerProductLayout();
		meta.clearAll();
	}
	
	public ServerRegistry getRegistry() {
		return( engine.getRegistry() );
	}
	
	public ServerContext getServerContext() {
		ServerRegistry registry = engine.getRegistry();
		return( registry.getServerContext() );
	}
	
}
