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
	private ServerBlotterStat stat;
	
	public ServerBlotterSet( ServerBlotter blotter , BlotterType type , ServerEvents events , String setId ) {
		super( events , setId );
		this.blotter = blotter;
		this.type = type;
		
		items = new LinkedList<ServerBlotterItem>();
		memos = new HashMap<String,ServerBlotterMemo>();

		stat = new ServerBlotterStat( this );
	}
	
	@Override
	public ServerEventsState getState() {
		return( new ServerEventsState( this , super.getStateId() ) );
	}
	
	public synchronized ServerBlotterStat getStatistics() {
		ServerBlotterStat statCopy = stat.copy();
		return( statCopy );
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
		stat.statAddItem( item );
	}
	
	public synchronized void finishItem( ServerBlotterItem item ) {
		ServerBlotterMemo memo = item.memo;
		if( memo != null && item.success ) {
			long elapsed = item.stopTime - item.startTime;
			memo.addEvent( elapsed );
		}
		
		items.remove( item );
		stat.statFinishItem( item );
	}
	
	public void notifyItem( ServerBlotterItem item , ActionBase action , BlotterEvent event ) {
		ServerBlotterEvent data = new ServerBlotterEvent( item , action , event );
		super.trigger( ServerEvents.EVENT_BLOTTEREVENT , data );
	}
	
	public void startChildAction( ServerBlotterItem item , ActionBase action ) {
		item.startChildAction( action );
		stat.statAddChildItem( item , action );
	}

	public void stopChildAction( ServerBlotterItem item , ActionBase action , boolean success ) {
		item.stopChildAction( action , success );
		stat.statFinishChildItem( item , action , success );
	}

}
