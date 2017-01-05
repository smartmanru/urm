package org.urm.engine;

import org.urm.action.ActionBase;
import org.urm.engine.ServerBlotter.BlotterType;

public class ServerBlotterItem {

	ServerBlotterSet blotterSet;
	public ActionBase action;
	
	public ServerBlotterMemo memo;
	public ServerBlotterItem parent;
	public long startTime;
	public long stopTime;
	public boolean success;
	public boolean stopped;
	public boolean errors;
	
	public String INFO_NAME;
	public String INFO_PRODUCT;
	public String INFO_PROJECT;
	public String INFO_TAG;
	
	public ServerBlotterItem( ServerBlotterSet blotterSet , ActionBase action ) {
		this.blotterSet = blotterSet;
		this.action = action;
		
		action.setBlotterItem( this );
		startTime = System.currentTimeMillis();
		stopTime = 0;
		success = false;
		stopped = false;
		errors = false;
	}

	public void setMemo( ServerBlotterMemo memo ) {
		this.memo = memo;
	}
	
	public boolean isRootItem() {
		return( blotterSet.type == BlotterType.BLOTTER_ROOT );
	}
	
	public boolean isBuildItem() {
		return( blotterSet.type == BlotterType.BLOTTER_BUILD );
	}
	
	public boolean isReleaseItem() {
		return( blotterSet.type == BlotterType.BLOTTER_RELEASE );
	}
	
	public boolean isDeployItem() {
		return( blotterSet.type == BlotterType.BLOTTER_DEPLOY );
	}
	
	public void startChildAction( ActionBase action ) {
		action.setBlotterItem( this );
	}
	
	public void stopChildAction( ActionBase action , boolean success ) {
		if( !success )
			errors = true;
	}
	
	public void createRootItem() {
		this.INFO_NAME = "root " + action.ID;
	}

	public void createBuildItem( String product , String project , String tag ) {
		this.INFO_PRODUCT = product;
		this.INFO_PROJECT = project;
		this.INFO_TAG = tag;
		
		this.INFO_NAME = "build " + tag;
	}

	public void stopAction( boolean success ) {
		this.success = success;
		
		stopTime = System.currentTimeMillis();
		stopped = true;
	}
	
}
