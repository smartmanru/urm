package org.urm.engine.blotter;

import java.util.LinkedList;
import java.util.List;

import org.urm.action.ActionBase;

public class EngineBlotterTreeItem {

	public ActionBase action;
	public EngineBlotterActionItem rootItem;
	public EngineBlotterTreeItem parentItem;
	public EngineBlotterActionItem baseItem;
	
	public long startTime;
	public long stopTime;
	public boolean success;
	public boolean stopped;
	public boolean errors;
	
	private List<EngineBlotterTreeItem> childs;
	
	public EngineBlotterTreeItem( ActionBase action , EngineBlotterActionItem rootItem , EngineBlotterTreeItem parentItem , EngineBlotterActionItem baseItem ) {
		this.action = action;
		this.rootItem = rootItem;
		this.parentItem = parentItem;
		this.baseItem = baseItem;
		
		startTime = System.currentTimeMillis();
		stopTime = 0;
		success = false;
		stopped = false;
		errors = false;
		
		childs = new LinkedList<EngineBlotterTreeItem>();
	}
	
	public synchronized EngineBlotterTreeItem[] getChildren() {
		return( childs.toArray( new EngineBlotterTreeItem[0] ) );
	}

	public synchronized void addChild( EngineBlotterTreeItem item ) {
		childs.add( item );
	}
	
	public synchronized void stopAction( boolean success ) {
		this.success = success;
		
		stopTime = System.currentTimeMillis();
		stopped = true;
	}
	
}
