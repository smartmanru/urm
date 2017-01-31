package org.urm.engine;

import org.urm.action.ActionBase;
import org.urm.engine.ServerBlotter.BlotterType;
import org.urm.engine.action.ActionInit;
import org.urm.engine.action.ActionInit.RootActionType;
import org.urm.engine.storage.Folder;

public class ServerBlotterItem {

	public ServerBlotterSet blotterSet;
	public ActionBase action;
	
	public ServerBlotterMemo memo;
	public ServerBlotterItem root;
	public ServerBlotterItem parent;
	public ServerBlotterTreeItem treeItem;
	
	public String ID;
	public long startTime;
	public long stopTime;
	public boolean success;
	public boolean stopped;
	public boolean errors;
	public boolean removed;
	
	public String INFO_NAME = "";
	public String INFO_PRODUCT = "";
	public String INFO_PROJECT = "";
	public String INFO_TAG = "";

	public RootActionType rootType;
	public Folder logFolder;
	public String logFile;
	
	public ServerBlotterItem( ServerBlotterSet blotterSet , ActionBase action , ServerBlotterItem rootItem , ServerBlotterItem parentItem , ServerBlotterTreeItem parentTreeItem ) {
		this.blotterSet = blotterSet;
		this.action = action;
		
		this.root = ( rootItem == null )? this : rootItem;
		this.parent = parentItem;
		this.treeItem = new ServerBlotterTreeItem( action , this.root , parentTreeItem , this );
		
		startTime = treeItem.startTime;
		stopTime = 0;
		success = false;
		stopped = false;
		errors = false;
		removed = false;
		
		action.setBlotterItem( this , treeItem );
	}

	public void setRemoved() {
		removed = true;
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
	
	public void startChildAction( ServerBlotterTreeItem treeItem ) {
	}
	
	public void stopChildAction( ServerBlotterTreeItem treeItem , boolean success ) {
		if( !success )
			errors = true;
	}
	
	public void createRootItem() {
		ActionInit init = ( ActionInit )action;
		
		this.ID = "action-" + action.ID;
		this.INFO_NAME = init.getFormalName();
		this.INFO_PRODUCT = init.session.productName;
		
		this.rootType = init.type;
	}

	public void createBuildItem( String product , String project , String tag , Folder logFolder , String logFile ) {
		this.ID = "action-" + action.ID;
		this.INFO_NAME = "build " + tag;
		this.INFO_PRODUCT = product;
		this.INFO_PROJECT = project;
		this.INFO_TAG = tag;

		this.rootType = action.actionInit.type;
		this.logFolder = logFolder;
		this.logFile = logFile;
	}

	public void stopAction( boolean success ) {
		this.success = success;
		
		stopTime = System.currentTimeMillis();
		stopped = true;
	}
	
}
