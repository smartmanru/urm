package org.urm.action.deploy;

import org.urm.meta.product.MetaEnvServer;

public class DeployPlanItem {

	public DeployPlanSet set;
	public int pos;
	public String key;
	public MetaEnvServer server;
	
	public boolean database;
	public boolean app;
	
	public boolean execute;
	public boolean startDeploy;
	public boolean doneRedist;
	public boolean doneDeploy;
	public boolean failedRedist;
	public boolean failedDeploy;
	public boolean executeRedist;
	public boolean executeDeploy;

	public DeployPlanItem( DeployPlanSet set , MetaEnvServer server , int pos , String key ) {
		this.set = set;
		this.pos = pos;
		this.server = server;
		this.key = key;
		
		execute = true;
		startDeploy = false;
		doneRedist = false;
		doneDeploy = false;
		failedRedist = false;
		failedDeploy = false;
		executeRedist = false;
		executeDeploy = false;
		
		database = false;
		app = false;
	}

	public void createApp() {
		app = true;
	}
	
	public void createDatabase() {
		database = true;
	}
	
	public String getFullPos() {
		return( set.getFullPos() + "." + ( pos + 1 ) );
	}

	public void setExecute( boolean execute ) {
		this.execute = execute;
		
		if( app ) {
			executeRedist = false;
			if( !server.isDatabase() ) {
				if( set.sg.plan.redist && execute )
					executeRedist = true;
			}
		}
		
		executeDeploy = false;
		if( server.isDeployPossible() ) {
			if( set.sg.plan.deploy && execute )
				executeDeploy = true;
		}
	}
	
}
