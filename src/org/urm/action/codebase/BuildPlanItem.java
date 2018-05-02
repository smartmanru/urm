package org.urm.action.codebase;

import org.urm.action.ActionBase;
import org.urm.engine.BlotterService;
import org.urm.engine.blotter.EngineBlotterActionItem;
import org.urm.engine.blotter.EngineBlotterSet;
import org.urm.engine.dist.ReleaseBuildScopeProject;
import org.urm.engine.dist.ReleaseDistScopeDelivery;
import org.urm.engine.dist.ReleaseDistScopeDeliveryItem;

public class BuildPlanItem {
	
	public BuildPlanSet set;
	public int pos;
	public ReleaseBuildScopeProject buildTarget;
	public ReleaseDistScopeDelivery distTarget;
	public ReleaseDistScopeDeliveryItem distItem;
	public String key;
	public String dbVersion;
	
	public boolean execute;
	public boolean startTag;
	public boolean startBuild;
	public boolean startGet;
	public boolean doneTag;
	public boolean doneBuild;
	public boolean doneGet;
	public boolean failedTag;
	public boolean failedBuild;
	public boolean failedGet;
	public boolean executeBuild;
	public boolean executeGet;
	public boolean executeGetAllowed;
	public boolean noBuild;
	public boolean noGet;
	
	private ActionBase patchAction;
	
	public BuildPlanItem( BuildPlanSet set , int pos , String key ) {
		this.set = set;
		this.pos = pos;
		this.key = key;
		
		execute = true;
		executeBuild = false;
		executeGet = false;
		executeGetAllowed = true;
		
		startBuild = false;
		startGet = false;
		doneBuild = false;
		doneGet = false;
		failedBuild = false;
		failedGet = false;
		noBuild = false;
		noGet = false;
	}

	public void createProject( ReleaseBuildScopeProject target ) {
		this.buildTarget = target;
	}
	
	public void createDelivery( ReleaseDistScopeDelivery target ) {
		this.distTarget = target;
	}
	
	public void createDeliveryItem( ReleaseDistScopeDeliveryItem target ) {
		this.distItem = target;
	}
	
	public void createDatabase( String dbVersion ) {
		this.dbVersion = dbVersion;
	}
	
	public String getFullPos() {
		return( set.getFullPos() + "." + ( pos + 1 ) );
	}
	
	public void setExecute( boolean execute ) {
		this.execute = execute;
		executeBuild = ( execute && buildTarget != null && buildTarget.project.isBuildable() )? true : false;
		boolean canGet = true;
		if( buildTarget != null ) {
			if( buildTarget.isEmpty() )
				canGet = false;
		}
			
		executeGet = ( canGet && execute && executeGetAllowed )? true : false;
	}

	public void setGet( boolean get ) {
		executeGetAllowed = get;
		boolean canGet = true;
		if( buildTarget != null ) {
			if( buildTarget.isEmpty() )
				canGet = false;
		}
		executeGet = ( canGet && execute && executeGetAllowed )? true : false;
	}
	
	public void clearRun() {
		doneBuild = false;
		doneGet = false;
		failedBuild = false;
		failedGet = false;
		noBuild = false;
		noGet = false;
	}

	public void setNotRun() {
		if( executeBuild && doneBuild == false )
			noBuild = true;
		if( executeGet && doneGet == false )
			noGet = true;
	}

	public synchronized void setTagStart() {
		startTag = true;
	}
	
	public synchronized void setBuildStart() {
		startBuild = true;
	}
	
	public synchronized void setPatchStart( ActionBase patchAction ) {
		this.patchAction = patchAction;
	}
	
	public synchronized void setGetStart() {
		startGet = true;
	}
	
	public synchronized void setTagDone( boolean success ) {
		doneTag = true;
		failedTag = ( success )? false : true;
	}
	
	public synchronized void setBuildDone( boolean success ) {
		doneBuild = true;
		failedBuild = ( success )? false : true;
	}
	
	public synchronized void setPatchDone( boolean success ) {
	}
	
	public synchronized void setGetDone( boolean success ) {
		doneGet = true;
		failedGet = ( success )? false : true;
	}
	
	public synchronized EngineBlotterActionItem findBlotterItem() {
		if( patchAction == null )
			return( null );
		
		BlotterService service = set.plan.engine.getBlotterService();
		EngineBlotterSet set = service.getBuildBlotter();
		return( set.findActionItem( patchAction ) );
	}
	
}
