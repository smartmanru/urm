package org.urm.engine.blotter;

import java.util.Date;

import org.urm.action.ActionBase;
import org.urm.engine.action.ActionInit;
import org.urm.engine.action.ActionInit.RootActionType;
import org.urm.engine.storage.Folder;

public class EngineBlotterActionItem extends EngineBlotterItem {

	public ActionBase action;
	public EngineBlotterTreeItem treeItem;

	public Date startTime;
	public Date stopTime;
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

	public EngineBlotterMemo memo;
	public EngineBlotterActionItem root;
	public EngineBlotterActionItem parent;
	
	public EngineBlotterActionItem( EngineBlotterSet blotterSet , ActionBase action , EngineBlotterActionItem rootItem , EngineBlotterActionItem parentItem , EngineBlotterTreeItem parentTreeItem ) {
		super( blotterSet , "action-" + action.ID );
		
		this.action = action;
		this.root = ( rootItem == null )? this : rootItem;
		this.parent = parentItem;
		this.treeItem = new EngineBlotterTreeItem( action , this.root , parentTreeItem , this );
		
		startTime = treeItem.startTime;
		stopTime = null;
		success = false;
		stopped = false;
		errors = false;
		
		action.setBlotterItem( this , treeItem );
	}

	public void setMemo( EngineBlotterMemo memo ) {
		this.memo = memo;
	}
	
	public void startChildAction( EngineBlotterTreeItem treeItem ) {
	}
	
	public void stopChildAction( EngineBlotterTreeItem treeItem , boolean success ) {
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
		
		stopTime = new Date();
		stopped = true;
		super.setTobeRemoved();
	}

	public int getProgressPercent() {
		Date currentTime = new Date();
		long elapsed = currentTime.getTime() - startTime.getTime();
		int percent;
		if( memo.isNew() ) {
			if( elapsed <= 5000 )
				percent = ( int )( elapsed / 100 );
			else {
				percent = ( int )( 50 + 8 * Math.log10( elapsed - 5000 ) );
				if( percent > 99 )
					percent = 99;
			}
		}
		else {
			if( elapsed <= memo.lastDuration )
				percent = ( int )( ( elapsed * 80 ) / memo.lastDuration );
			else
				percent = 80 + ( int )( 20 * ( 1. - 1. / ( elapsed - memo.lastDuration ) ) );
		}
		
		return( percent );
	}
	
}
