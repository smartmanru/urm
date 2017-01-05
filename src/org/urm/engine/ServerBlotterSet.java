package org.urm.engine;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.urm.action.ActionBase;
import org.urm.engine.ServerBlotter.BlotterEvent;
import org.urm.engine.ServerBlotter.BlotterType;

public class ServerBlotterSet extends ServerEventsSource {

	ServerBlotter blotter;
	BlotterType type;
	
	private List<ServerBlotterItem> items;
	private Map<String,ServerBlotterMemo> memos;
	
	public ServerBlotterSet( ServerBlotter blotter , BlotterType type , ServerEvents events , String setId ) {
		super( events , setId );
		this.blotter = blotter;
		this.type = type;
		
		items = new LinkedList<ServerBlotterItem>();
		memos = new HashMap<String,ServerBlotterMemo>(); 
	}
	
	@Override
	public ServerEventsState getState() {
		return( new ServerEventsState( this , super.getStateId() ) );
	}
	
	public boolean isRootSet() {
		return( type == BlotterType.BLOTTER_ROOT );
	}
	
	public boolean isBuildSet() {
		return( type == BlotterType.BLOTTER_BUILD );
	}
	
	public boolean isReleaseSet() {
		return( type == BlotterType.BLOTTER_RELEASE );
	}
	
	public boolean isDeploySet() {
		return( type == BlotterType.BLOTTER_DEPLOY );
	}

	public synchronized ServerBlotterItem[] getItems() {
		return( items.toArray( new ServerBlotterItem[0] ) ); 
	}

	public synchronized void addItem( ServerBlotterItem item ) {
		if( item.isBuildItem() ) {
			String key = "build#" + item.INFO_PRODUCT + "#" + item.INFO_PROJECT;
			ServerBlotterMemo memo = memos.get( key );
			if( memo == null ) {
				memo = new ServerBlotterMemo( this , key );
				memos.put( key , memo );
			}
			
			item.setMemo( memo );
		}
		items.add( item );
	}
	
	public synchronized void finishItem( ServerBlotterItem item ) {
		ServerBlotterMemo memo = item.memo;
		if( memo != null && item.success ) {
			long elapsed = item.stopTime - item.startTime;
			memo.addEvent( elapsed );
		}
		items.remove( item );
	}
	
	public void notifyItem( ServerBlotterItem item , ActionBase action , BlotterEvent event ) {
		ServerBlotterEvent data = new ServerBlotterEvent( item , action , event );
		super.trigger( ServerEvents.EVENT_BLOTTEREVENT , data );
	}
	
}
