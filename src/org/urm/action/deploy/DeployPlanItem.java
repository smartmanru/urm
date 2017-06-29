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
	public boolean executeRedist;
	public boolean executeDeploy;
	
	public boolean startDeploy;
	public boolean doneRedist;
	public boolean doneDeploy;
	public boolean failedRedist;
	public boolean failedDeploy;
	public boolean noRedist;
	public boolean noDeploy;

	public DeployPlanItem( DeployPlanSet set , MetaEnvServer server , int pos , String key ) {
		this.set = set;
		this.pos = pos;
		this.server = server;
		this.key = key;
		
		database = false;
		app = false;
		
		execute = true;
		executeRedist = false;
		executeDeploy = false;
		
		startDeploy = false;
		doneRedist = false;
		doneDeploy = false;
		failedRedist = false;
		failedDeploy = false;
		noRedist = false;
		noDeploy = false;
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

	public void clearRun() {
		startDeploy = false;
		doneRedist = false;
		doneDeploy = false;
		failedRedist = false;
		failedDeploy = false;
		noRedist = false;
		noDeploy = false;
	}

	public void setDoneRedist( boolean success ) {
		doneRedist = true;
		failedRedist = success;
	}

	public void setDeployStarted() {
		startDeploy = true;
	}
	
	public void setDeployDone( boolean success ) {
		doneDeploy = true;
		failedDeploy = success;
	}
	
	public void setRedistNotRun() {
		if( executeRedist && doneRedist == false )
			noRedist = true;
	}
	
	public void setDeployNotRun() {
		if( executeDeploy && doneDeploy == false )
			noDeploy = true;
	}
	
}
