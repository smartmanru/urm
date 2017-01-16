package org.urm.engine;

import java.util.HashMap;
import java.util.Map;

import org.urm.action.ActionBase;
import org.urm.common.Common;
import org.urm.engine.ServerBlotter.BlotterEvent;
import org.urm.engine.ServerBlotter.BlotterType;
import org.urm.engine.action.ActionInit;

public class ServerBlotterSet extends ServerEventsSource {

	ServerBlotter blotter;
	BlotterType type;
	
	private Map<Integer,ServerBlotterItem> items;
	private Map<String,ServerBlotterMemo> memos;
	private ServerBlotterStat stat;
	
	public ServerBlotterSet( ServerBlotter blotter , BlotterType type , ServerEvents events , String setId ) {
		super( events , setId );
		this.blotter = blotter;
		this.type = type;

		items = new HashMap<Integer,ServerBlotterItem>();
		memos = new HashMap<String,ServerBlotterMemo>();
		stat = new ServerBlotterStat( this );
	}
	
	@Override
	public ServerEventsState getState() {
		return( new ServerEventsState( this , super.getStateId() ) );
	}
	
	public synchronized void init() {
		clear();
		memos.clear();
	}
	
	public void houseKeeping( long time ) {
		clear();
	}
	
	public synchronized void clear() {
		if( isRootSet() )
			clearRoots();
		
		stat.statClear();
		items.clear();
	}
	
	public synchronized ServerBlotterStat getStatistics() {
		return( stat.copy() );
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

	public synchronized ServerBlotterItem[] getItems( boolean includeFinished ) {
		Map<Integer,ServerBlotterItem> selected = null;
		if( includeFinished )
			selected = items;
		else {
			selected = new HashMap<Integer,ServerBlotterItem>();
			for( ServerBlotterItem item : items.values() ) {
				if( !item.stopped )
					selected.put( item.action.ID , item );
			}
		}
			
		return( selected.values().toArray( new ServerBlotterItem[0] ) ); 
	}

	public synchronized ServerBlotterItem getItem( int actionId ) {
		return( items.get( actionId ) );
	}
	
	public synchronized void addItem( ServerBlotterItem item ) {
		long itemDay = Common.getDay( item.startTime );
		if( itemDay != blotter.day )
			return;
			
		if( item.isBuildItem() ) {
			String key = "build#" + item.INFO_PRODUCT + "#" + item.INFO_PROJECT;
			ServerBlotterMemo memo = memos.get( key );
			if( memo == null ) {
				memo = new ServerBlotterMemo( this , key );
				memos.put( key , memo );
			}
			
			item.setMemo( memo );
		}
		
		items.put( item.action.ID , item );
		stat.statAddItem( item );
	}
	
	public synchronized void finishItem( ServerBlotterItem item ) {
		long itemDay = Common.getDay( item.startTime );
		if( itemDay != blotter.day )
			return;
		
		ServerBlotterMemo memo = item.memo;
		if( memo != null && item.success ) {
			long elapsed = item.stopTime - item.startTime;
			memo.addEvent( elapsed );
		}
		
		stat.statFinishItem( item );
	}
	
	public void notifyItem( ServerBlotterItem item , ActionBase action , BlotterEvent event ) {
		ServerBlotterEvent data = new ServerBlotterEvent( item , action , event );
		super.trigger( ServerEvents.EVENT_BLOTTEREVENT , data );
	}
	
	public void startChildAction( ServerBlotterItem item , ActionBase action ) {
		long itemDay = Common.getDay( item.startTime );
		if( itemDay != blotter.day )
			return;
		
		item.startChildAction( action );
		stat.statAddChildItem( item , action );
	}

	public void stopChildAction( ServerBlotterItem item , ActionBase action , boolean success ) {
		long itemDay = Common.getDay( item.startTime );
		if( itemDay != blotter.day )
			return;
		
		item.stopChildAction( action , success );
		stat.statFinishChildItem( item , action , success );
	}

	private void clearRoots() {
		for( ServerBlotterItem item : items.values() ) {
			ActionInit action = ( ActionInit )item.action;
			try {
				action.artefactory.workFolder.removeThis( action );
			}
			catch( Throwable e ) {
				blotter.engine.log( "Clear roots" , e );
			}
		}
	}
	
}
