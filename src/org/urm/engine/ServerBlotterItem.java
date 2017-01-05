package org.urm.engine;

import org.urm.action.ActionBase;
import org.urm.engine.ServerBlotter.BlotterType;

public class ServerBlotterItem {

	ServerBlotter blotter;
	BlotterType type;
	
	public ActionBase action;
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
	
	public ServerBlotterItem( ServerBlotter blotter , BlotterType type , ActionBase action ) {
		this.blotter = blotter;
		this.type = type;
		this.action = action;
		
		action.setBlotterItem( this );
		startTime = System.currentTimeMillis();
		stopTime = 0;
		success = false;
		stopped = false;
		errors = false;
	}

	public boolean isRootItem() {
		return( type == BlotterType.BLOTTER_ROOT );
	}
	
	public boolean isBuildItem() {
		return( type == BlotterType.BLOTTER_BUILD );
	}
	
	public boolean isReleaseItem() {
		return( type == BlotterType.BLOTTER_RELEASE );
	}
	
	public boolean isDeployItem() {
		return( type == BlotterType.BLOTTER_DEPLOY );
	}
	
	public void startChildAction( ActionBase action ) {
		action.setBlotterItem( this );
	}
	
	public void stopChildAction( ActionBase action , boolean success ) {
		if( !success )
			errors = true;
	}
	
	public void createRootItem() {
	}

	public void createBuildItem( String product , String project , String tag ) {
		this.INFO_PRODUCT = product;
		this.INFO_PROJECT = project;
		this.INFO_TAG = tag;
		
		this.INFO_NAME = "build " + tag;
	}

	public void stopAction( boolean success ) {
		this.success = success;
	}
	
}
