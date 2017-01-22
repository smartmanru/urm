package org.urm.engine;

import java.util.LinkedList;
import java.util.List;

import org.urm.action.ActionBase;
import org.urm.action.build.ActionPatch;
import org.urm.common.Common;
import org.urm.engine.action.ActionInit;

public class ServerBlotter {

	public enum BlotterType {
		BLOTTER_ROOT ,
		BLOTTER_BUILD ,
		BLOTTER_RELEASE ,
		BLOTTER_DEPLOY
	};
	
	public enum BlotterEvent {
		BLOTTER_START ,
		BLOTTER_STOP ,
		BLOTTER_STARTCHILD ,
		BLOTTER_STOPCHILD
	};
	
	public ServerEngine engine;
	
	public long day;
	
	protected ServerBlotterSet blotterRoots;
	protected ServerBlotterSet blotterBuilds;
	protected ServerBlotterSet blotterReleases;
	protected ServerBlotterSet blotterDeploy;
	private List<ServerBlotterSet> blotters;
	
	public ServerBlotter( ServerEngine engine ) {
		this.engine = engine;

		day = Common.getDay( System.currentTimeMillis() );
		
		blotters = new LinkedList<ServerBlotterSet>(); 
		blotterRoots = addBlotter( BlotterType.BLOTTER_ROOT , "blotter.roots" );
		blotterBuilds = addBlotter( BlotterType.BLOTTER_BUILD , "blotter.builds" );
		blotterReleases = addBlotter( BlotterType.BLOTTER_RELEASE , "blotter.releases" );
		blotterDeploy = addBlotter( BlotterType.BLOTTER_DEPLOY , "blotter.deploy" );
	}

	private ServerBlotterSet addBlotter( BlotterType type , String name ) {
		ServerEvents events = engine.getEvents();
		ServerBlotterSet set = new ServerBlotterSet( this , type , events , name );
		blotters.add( set );
		return( set );
	}
	
	public void init() {
		for( ServerBlotterSet set : blotters )
			set.init();
	}
	
	public void clear() {
		for( ServerBlotterSet set : blotters )
			set.clear();
	}
	
	public void runHouseKeeping( long time ) {
		long timeDay = Common.getDay( time );
		if( timeDay == day )
			return;
		
		day = timeDay;
		for( ServerBlotterSet set : blotters )
			set.houseKeeping( time );
	}
	
	public ServerBlotterItem[] getBlotterItems( BlotterType type , boolean includeFinished ) {
		ServerBlotterSet set = getBlotterSet( type );
		if( set == null )
			return( new ServerBlotterItem[0] );
		return( set.getItems( includeFinished ) ); 
	}
	
	public ServerBlotterItem getBlotterItem( BlotterType type , int actionId ) {
		ServerBlotterSet set = getBlotterSet( type );
		if( set == null )
			return( null );
		return( set.getItem( actionId ) ); 
	}
	
	public ServerBlotterStat getBlotterStatistics( BlotterType type ) {
		ServerBlotterSet set = getBlotterSet( type );
		if( set == null )
			return( null );
		return( set.getStatistics() ); 
	}
	
	public ServerBlotterSet getBlotterSet( BlotterType type ) {
		for( ServerBlotterSet set : blotters ) {
			if( set.type == type )
				return( set );
		}
		return( null );
	}
	
	public void startAction( ActionBase action ) throws Exception {
		if( action instanceof ActionInit ) {
			ServerBlotterItem item = blotterRoots.createRootItem( ( ActionInit )action );
			notifyItem( item , BlotterEvent.BLOTTER_START );
			return;
		}
		
		ServerBlotterItem rootItem = action.parent.blotterRootItem;
		ServerBlotterTreeItem parentTreeItem = action.parent.blotterTreeItem;
		ServerBlotterItem parentBaseItem = getBaseItem( rootItem , parentTreeItem );

		if( action instanceof ActionPatch ) {
			ServerBlotterItem baseItem = blotterBuilds.createBuildItem( rootItem , parentBaseItem , parentTreeItem , ( ActionPatch )action );
			startChildAction( rootItem , parentBaseItem , baseItem.treeItem );
			notifyItem( baseItem , BlotterEvent.BLOTTER_START );
			return;
		}
		
		ServerBlotterTreeItem treeItem = blotterRoots.createChildItem( action , parentBaseItem , parentTreeItem );
		startChildAction( rootItem , parentBaseItem , treeItem );
	}

	private void startChildAction( ServerBlotterItem rootItem , ServerBlotterItem baseItem , ServerBlotterTreeItem treeItem ) {
		blotterRoots.startChildAction( rootItem , treeItem );
		notifyChildItem( rootItem , treeItem , BlotterEvent.BLOTTER_STARTCHILD );
		
		if( baseItem != rootItem ) {
			ServerBlotterSet set = baseItem.blotterSet;
			set.startChildAction( baseItem , treeItem );
			notifyChildItem( baseItem , treeItem , BlotterEvent.BLOTTER_STARTCHILD );
		}
	}
	
	private ServerBlotterItem getBaseItem( ServerBlotterItem rootItem , ServerBlotterTreeItem treeItem ) {
		ServerBlotterTreeItem parentItem = treeItem;
		while( parentItem != null ) {
			if( parentItem.baseItem != null )
				return( parentItem.baseItem );
		
			parentItem = parentItem.parentItem;
		}
		return( rootItem );
	}
	
	public void stopAction( ActionBase action , boolean success ) throws Exception {
		if( action.blotterTreeItem == null )
			return;

		ServerBlotterTreeItem treeItem = action.blotterTreeItem;
		ServerBlotterItem rootItem = treeItem.rootItem;
		ServerBlotterItem baseItem = treeItem.baseItem;
		treeItem.stopAction( success );
		
		if( baseItem != null ) {
			baseItem.stopAction( success );
			finishItem( baseItem );
			notifyItem( baseItem , BlotterEvent.BLOTTER_STOP );
			
			if( baseItem != rootItem )
				stopChildAction( rootItem , baseItem.parent , treeItem , success );
			return;
		}
		
		baseItem = getBaseItem( rootItem , treeItem );
		stopChildAction( rootItem , baseItem , treeItem , success );
	}

	private void stopChildAction( ServerBlotterItem rootItem , ServerBlotterItem baseItem , ServerBlotterTreeItem treeItem , boolean success ) {
		blotterRoots.stopChildAction( rootItem , treeItem , success );
		notifyChildItem( rootItem , treeItem , BlotterEvent.BLOTTER_STOPCHILD );
		
		if( baseItem != rootItem ) {
			ServerBlotterSet set = baseItem.blotterSet;
			set.stopChildAction( baseItem , treeItem , success );
			notifyChildItem( baseItem , treeItem , BlotterEvent.BLOTTER_STOPCHILD );
		}
	}
	
	private void notifyItem( ServerBlotterItem item , BlotterEvent event ) {
		ServerBlotterSet set = item.blotterSet;
		set.notifyItem( item , event );
	}
	
	private void notifyChildItem( ServerBlotterItem baseItem , ServerBlotterTreeItem treeItem , BlotterEvent event ) {
		ServerBlotterSet set = baseItem.blotterSet;
		set.notifyChildItem( baseItem , treeItem , event );
	}
	
	private void finishItem( ServerBlotterItem item ) {
		ServerBlotterSet set = item.blotterSet;
		set.finishItem( item );
	}
	
	public ServerEventsSubscription subscribe( ServerEventsApp app , ServerEventsListener listener , BlotterType type ) {
		ServerBlotterSet set = getBlotterSet( type );
		if( set == null )
			return( null );
		
		return( app.subscribe( set , listener ) );
	}

}
