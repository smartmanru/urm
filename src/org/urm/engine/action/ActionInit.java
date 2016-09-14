package org.urm.engine.action;

import org.urm.action.ActionBase;
import org.urm.action.ActionScope;
import org.urm.common.Common;
import org.urm.engine.ServerContext;
import org.urm.engine.ServerDirectory;
import org.urm.engine.ServerProduct;
import org.urm.engine.ServerSettings;
import org.urm.engine.SessionContext;
import org.urm.engine.storage.Artefactory;

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
		Common.exitUnexpected();
	}
	
	@Override
	protected void runBefore( ActionScope scope ) throws Exception {
		Common.exitUnexpected();
	}

	public void setServerSystemProductLayout( ServerProduct product ) throws Exception {
		session.setServerSystemProductLayout( this , product.NAME , product.PATH );
		meta.clearAll();
	}
	
	public void setServerSystemProductLayout( String name ) throws Exception {
		ServerDirectory directory = getDirectory();
		ServerProduct product = directory.getProduct( name ); 
		setServerSystemProductLayout( product );
	}
	
	public void clearServerProductLayout() {
		session.clearServerProductLayout();
		meta.clearAll();
	}
	
	public ServerSettings getSettings() {
		return( engine.getSettings() );
	}
	
	public ServerDirectory getDirectory() {
		return( engine.getDirectory() );
	}
	
	public ServerContext getServerContext() {
		ServerSettings settings = engine.getSettings();
		return( settings.getServerContext() );
	}
	
}
