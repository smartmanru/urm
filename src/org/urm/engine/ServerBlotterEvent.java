package org.urm.engine;

import org.urm.action.ActionBase;
import org.urm.engine.ServerBlotter.BlotterEvent;

public class ServerBlotterEvent {

	public ServerBlotterItem item;
	public ActionBase action;
	public BlotterEvent event;
	
	public ServerBlotterEvent( ServerBlotterItem item , ActionBase action , BlotterEvent event ) {
		this.item = item;
		this.action = action;
		this.event = event;
	}
	
}
