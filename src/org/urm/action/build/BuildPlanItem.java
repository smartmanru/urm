package org.urm.action.build;

import org.urm.engine.dist.ReleaseTarget;

public class BuildPlanItem {
	
	public BuildPlanSet set;
	public int pos;
	public ReleaseTarget target;
	public String key;
	
	public boolean execute;
	public boolean doneBuild;
	public boolean doneGet;
	public boolean failedBuild;
	public boolean failedGet;
	public boolean executeBuild;
	public boolean executeGet;
	
	public BuildPlanItem( BuildPlanSet set , ReleaseTarget target , int pos , String key ) {
		this.set = set;
		this.pos = pos;
		this.target = target;
		this.key = key;
		
		execute = true;
		executeBuild = false;
		executeGet = false;
		
		doneBuild = false;
		doneGet = false;
		failedBuild = false;
		failedGet = false;
	}
	
	public String getFullPos() {
		return( set.getFullPos() + "." + ( pos + 1 ) );
	}
	
	public void setExecute( boolean execute ) {
		this.execute = execute;
		executeBuild = ( execute && target.isBuildableProject() )? true : false;
		boolean canGet = ( target.isProjectTarget() )? ( ( target.isEmpty() )? false : true ) : true;
		executeGet = ( canGet && execute )? true : false;
	}

	public void setBuildDone( boolean success ) {
		doneBuild = true;
		failedBuild = ( success )? false : true;
	}
	
	public void clearRun() {
		doneBuild = false;
		doneGet = false;
		failedBuild = false;
		failedGet = false;
	}
	
}
