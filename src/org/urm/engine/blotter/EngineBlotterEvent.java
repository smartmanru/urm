package org.urm.engine.blotter;

import org.urm.engine.blotter.EngineBlotter.BlotterEvent;

public class EngineBlotterEvent {

	public EngineBlotterItem baseItem;
	public EngineBlotterTreeItem childItem;
	public BlotterEvent event;
	
	public EngineBlotterEvent( EngineBlotterItem item , BlotterEvent event ) {
		this.baseItem = item;
		this.event = event;
	}
	
	public EngineBlotterEvent( EngineBlotterItem baseItem , EngineBlotterTreeItem childItem , BlotterEvent event ) {
		this.baseItem = baseItem;
		this.childItem = childItem;
		this.event = event;
	}
	
}
