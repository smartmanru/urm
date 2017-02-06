package org.urm.engine;

import java.util.LinkedList;
import java.util.List;

import org.urm.action.ActionBase;

public class ServerBlotterTreeItem {

	public ActionBase action;
	public ServerBlotterActionItem rootItem;
	public ServerBlotterTreeItem parentItem;
	public ServerBlotterActionItem baseItem;
	
	public long startTime;
	public long stopTime;
	public boolean success;
	public boolean stopped;
	public boolean errors;
	
	private List<ServerBlotterTreeItem> childs;
	
	public ServerBlotterTreeItem( ActionBase action , ServerBlotterActionItem rootItem , ServerBlotterTreeItem parentItem , ServerBlotterActionItem baseItem ) {
		this.action = action;
		this.rootItem = rootItem;
		this.parentItem = parentItem;
		this.baseItem = baseItem;
		
		startTime = System.currentTimeMillis();
		stopTime = 0;
		success = false;
		stopped = false;
		errors = false;
		
		childs = new LinkedList<ServerBlotterTreeItem>();
	}
	
	public synchronized ServerBlotterTreeItem[] getChildren() {
		return( childs.toArray( new ServerBlotterTreeItem[0] ) );
	}

	public synchronized void addChild( ServerBlotterTreeItem item ) {
		childs.add( item );
	}
	
	public synchronized void stopAction( boolean success ) {
		this.success = success;
		
		stopTime = System.currentTimeMillis();
		stopped = true;
	}
	
}
