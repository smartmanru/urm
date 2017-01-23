package org.urm.engine;

import org.urm.engine.ServerBlotter.BlotterEvent;

public class ServerBlotterEvent {

	public ServerBlotterItem baseItem;
	public ServerBlotterTreeItem childItem;
	public BlotterEvent event;
	
	public ServerBlotterEvent( ServerBlotterItem item , BlotterEvent event ) {
		this.baseItem = item;
		this.event = event;
	}
	
	public ServerBlotterEvent( ServerBlotterItem baseItem , ServerBlotterTreeItem childItem , BlotterEvent event ) {
		this.baseItem = baseItem;
		this.childItem = childItem;
		this.event = event;
	}
	
}
