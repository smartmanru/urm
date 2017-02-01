package org.urm.engine;

import org.urm.action.ActionBase;
import org.urm.engine.action.ActionInit;
import org.urm.engine.action.ActionInit.RootActionType;
import org.urm.engine.storage.Folder;

public class ServerBlotterActionItem extends ServerBlotterItem {

	public ActionBase action;
	public ServerBlotterTreeItem treeItem;

	public long startTime;
	public long stopTime;
	public boolean success;
	public boolean stopped;
	public boolean errors;
	
	public String INFO_NAME = "";
	public String INFO_PRODUCT = "";
	public String INFO_PROJECT = "";
	public String INFO_TAG = "";

	public Folder logFolder;
	public String logFile;
	
	public RootActionType rootType;

	public ServerBlotterMemo memo;
	public ServerBlotterActionItem root;
	public ServerBlotterActionItem parent;
	
	public ServerBlotterActionItem( ServerBlotterSet blotterSet , ActionBase action , ServerBlotterActionItem rootItem , ServerBlotterActionItem parentItem , ServerBlotterTreeItem parentTreeItem ) {
		super( blotterSet , "action-" + action.ID );
		
		this.action = action;
		this.root = ( rootItem == null )? this : rootItem;
		this.parent = parentItem;
		this.treeItem = new ServerBlotterTreeItem( action , this.root , parentTreeItem , this );
		
		startTime = treeItem.startTime;
		stopTime = 0;
		success = false;
		stopped = false;
		errors = false;
		
		action.setBlotterItem( this , treeItem );
	}

	public void setMemo( ServerBlotterMemo memo ) {
		this.memo = memo;
	}
	
	public void startChildAction( ServerBlotterTreeItem treeItem ) {
	}
	
	public void stopChildAction( ServerBlotterTreeItem treeItem , boolean success ) {
		if( !success )
			errors = true;
	}
	
	public void createRootItem() {
		ActionInit init = ( ActionInit )action;
		
		this.INFO_NAME = init.getFormalName();
		this.INFO_PRODUCT = init.session.productName;
		
		this.rootType = init.type;
	}

	public void createBuildItem( String product , String project , String tag , Folder logFolder , String logFile ) {
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
		if( success && action.isDebug() == false )
			super.setTobeRemoved();
	}
	
}
