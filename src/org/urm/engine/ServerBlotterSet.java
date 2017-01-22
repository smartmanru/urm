package org.urm.engine;

import java.util.HashMap;
import java.util.Map;

import org.urm.action.ActionBase;
import org.urm.action.build.ActionPatch;
import org.urm.engine.ServerBlotter.BlotterEvent;
import org.urm.engine.ServerBlotter.BlotterType;
import org.urm.engine.action.ActionInit;
import org.urm.meta.product.MetaSourceProject;

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
		stat.statInit( blotter.day );
		ServerBlotterItem[] set = items.values().toArray( new ServerBlotterItem[0] );
		for( ServerBlotterItem item : set ) {
			if( item.stopped )
				removeItem( item );
		}
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
	
	public synchronized void finishItem( ServerBlotterItem item ) {
		ServerBlotterMemo memo = item.memo;
		if( memo != null && item.success ) {
			long elapsed = item.stopTime - item.startTime;
			memo.addEvent( elapsed );
		}
		
		stat.statFinishItem( item );
	}
	
	public void notifyItem( ServerBlotterItem item , BlotterEvent event ) {
		ServerBlotterEvent data = new ServerBlotterEvent( item , event );
		super.trigger( ServerEvents.EVENT_BLOTTEREVENT , data );
	}
	
	public void notifyChildItem( ServerBlotterItem baseItem , ServerBlotterTreeItem treeItem , BlotterEvent event ) {
		ServerBlotterEvent data = new ServerBlotterEvent( baseItem , treeItem , event );
		super.trigger( ServerEvents.EVENT_BLOTTEREVENT , data );
	}
	
	public synchronized void startChildAction( ServerBlotterItem baseItem , ServerBlotterTreeItem treeItem ) {
		baseItem.startChildAction( treeItem );
		stat.statAddChildItem( baseItem , treeItem );
	}

	public synchronized void stopChildAction( ServerBlotterItem baseItem , ServerBlotterTreeItem treeItem , boolean success ) {
		baseItem.stopChildAction( treeItem , success );
		stat.statFinishChildItem( baseItem , treeItem , success );
	}

	public synchronized ServerBlotterItem createRootItem( ActionInit action ) {
		ServerBlotterItem item = new ServerBlotterItem( this , action , null , null , null );
		
		item.createRootItem();
		addItem( item );
		return( item );
	}

	public synchronized ServerBlotterItem createBuildItem( ServerBlotterItem rootItem , ServerBlotterItem baseItem , ServerBlotterTreeItem parentTreeItem , ActionPatch action ) {
		ServerBlotterItem item = new ServerBlotterItem( this , action , rootItem , baseItem , parentTreeItem );
		
		MetaSourceProject project = action.builder.project;
		item.createBuildItem( project.meta.name , project.NAME , action.builder.TAG , action.logDir , action.logFile );
		addItem( item );
		return( item );
	}

	public synchronized ServerBlotterTreeItem createChildItem( ActionBase action , ServerBlotterItem parentBaseItem , ServerBlotterTreeItem parentTreeItem ) {
		ServerBlotterTreeItem item = new ServerBlotterTreeItem( action , parentTreeItem.rootItem , parentTreeItem , null );
		parentTreeItem.addChild( item );
		action.setBlotterItem( parentBaseItem , item );
		return( item );
	}
	
	private void removeItem( ServerBlotterItem item ) {
		items.remove( item.action.ID );
		item.setRemoved();
		
		if( item.isRootItem() ) {
			try {
				ActionInit action = ( ActionInit )item.action;
				if( item.stopped )
					action.artefactory.workFolder.removeThis( action );
			}
			catch( Throwable e ) {
				blotter.engine.log( "Clear roots" , e );
			}
		}
	}
	
	private void addItem( ServerBlotterItem item ) {
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
	
}
