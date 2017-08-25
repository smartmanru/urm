package org.urm.engine;

import java.util.HashMap;
import java.util.Map;

import org.urm.action.ActionBase;
import org.urm.action.build.ActionPatch;
import org.urm.engine.ServerBlotter.BlotterEvent;
import org.urm.engine.ServerBlotter.BlotterType;
import org.urm.engine.action.ActionInit;
import org.urm.engine.dist.Dist;
import org.urm.engine.dist.DistRepository;
import org.urm.engine.dist.DistRepository.DistOperation;
import org.urm.engine.dist.DistRepositoryItem;
import org.urm.meta.ServerProductMeta;
import org.urm.meta.engine.ServerDirectory;
import org.urm.meta.product.Meta;
import org.urm.meta.product.MetaSourceProject;

public class ServerBlotterSet extends ServerEventsSource {

	public ServerBlotter blotter;
	public BlotterType type;
	
	private Map<String,ServerBlotterItem> items;
	private Map<String,ServerBlotterMemo> memos;
	private ServerBlotterStat stat;
	
	public ServerBlotterSet( ServerBlotter blotter , BlotterType type , ServerEvents events , String setId ) {
		super( events , setId );
		this.blotter = blotter;
		this.type = type;

		items = new HashMap<String,ServerBlotterItem>();
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

	public synchronized void start( ActionInit action ) throws Exception {
		if( isReleaseSet() )
			startReleaseSet( action );
	}

	public synchronized void startReleaseSet( ActionInit action ) throws Exception {
		ServerDirectory directory = action.getServerDirectory();
		for( String productName : directory.getProducts() ) {
			Meta meta = action.getProductMetadata( productName );
			ServerProductMeta storage = meta.getStorage( action );
			DistRepository repo = storage.getDistRepository( action );
			if( repo != null )
				startReleaseSetRepo( action , repo );
		}
	}

	public synchronized void startReleaseSetRepo( ActionInit action , DistRepository repo ) throws Exception {
		for( DistRepositoryItem repoItem : repo.getRunItems() ) {
			String key = getReleaseKey( repoItem );
			ServerBlotterReleaseItem item = new ServerBlotterReleaseItem( this , key );
			item.createReleaseItem( repoItem );
			items.put( item.ID , item );
		}
	}
	
	public void houseKeeping( long time ) {
		clear( true );
	}

	public void clear() {
		clear( false );
	}
	
	private synchronized void clear( boolean houseKeeping ) {
		stat.statInit( blotter.day );
		
		if( houseKeeping ) {
			ServerBlotterItem[] set = items.values().toArray( new ServerBlotterItem[0] );
			for( ServerBlotterItem item : set ) {
				if( item.toberemoved ) {
					items.remove( item.ID );
					removeItem( item );
				}
			}
		}
		else {
			for( ServerBlotterItem item : items.values() )
				removeItem( item );
			items.clear();
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
		Map<String,ServerBlotterItem> selected = null;
		if( includeFinished )
			selected = items;
		else {
			selected = new HashMap<String,ServerBlotterItem>();
			for( ServerBlotterItem item : items.values() ) {
				if( !item.toberemoved )
					selected.put( item.ID , item );
			}
		}
			
		return( selected.values().toArray( new ServerBlotterItem[0] ) ); 
	}

	public synchronized ServerBlotterItem getItem( String ID ) {
		return( items.get( ID ) );
	}
	
	public synchronized void finishItem( ServerBlotterActionItem item ) {
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
	
	public synchronized void startChildAction( ServerBlotterActionItem baseItem , ServerBlotterTreeItem treeItem ) {
		baseItem.startChildAction( treeItem );
		stat.statAddChildItem( baseItem , treeItem );
	}

	public synchronized void stopChildAction( ServerBlotterActionItem baseItem , ServerBlotterTreeItem treeItem , boolean success ) {
		baseItem.stopChildAction( treeItem , success );
		stat.statFinishChildItem( baseItem , treeItem , success );
	}

	public synchronized ServerBlotterActionItem createRootItem( ActionInit action ) {
		ServerBlotterActionItem item = new ServerBlotterActionItem( this , action , null , null , null );
		
		item.createRootItem();
		addItem( item );
		return( item );
	}

	public synchronized ServerBlotterActionItem createBuildItem( ServerBlotterActionItem rootItem , ServerBlotterActionItem baseItem , ServerBlotterTreeItem parentTreeItem , ActionPatch action ) {
		ServerBlotterActionItem item = new ServerBlotterActionItem( this , action , rootItem , baseItem , parentTreeItem );
		
		MetaSourceProject project = action.builder.project;
		item.createBuildItem( project.meta.name , project.NAME , action.builder.TAG , action.logDir , action.logFile );
		addItem( item );
		return( item );
	}

	public synchronized ServerBlotterTreeItem createChildItem( ActionBase action , ServerBlotterActionItem parentBaseItem , ServerBlotterTreeItem parentTreeItem ) {
		ServerBlotterTreeItem item = new ServerBlotterTreeItem( action , parentTreeItem.rootItem , parentTreeItem , null );
		parentTreeItem.addChild( item );
		action.setBlotterItem( parentBaseItem , item );
		return( item );
	}
	
	private void removeItem( ServerBlotterItem item ) {
		item.setRemoved();
		
		if( item.isRootItem() ) {
			try {
				ServerBlotterActionItem rootItem = ( ServerBlotterActionItem )item; 
				ActionInit action = ( ActionInit )rootItem.action;
				if( rootItem.success && action.isDebug() == false )
					action.artefactory.workFolder.removeThis( action );
			}
			catch( Throwable e ) {
				blotter.engine.log( "Clear roots" , e );
			}
		}
	}
	
	private void addItem( ServerBlotterActionItem item ) {
		if( item.isBuildItem() ) {
			String key = "build#" + item.INFO_PRODUCT + "#" + item.INFO_PROJECT;
			ServerBlotterMemo memo = memos.get( key );
			if( memo == null ) {
				memo = new ServerBlotterMemo( this , key );
				memos.put( key , memo );
			}
			
			item.setMemo( memo );
		}
		
		items.put( item.ID , item );
		stat.statAddItem( item );
	}

	
	public synchronized ServerBlotterReleaseItem affectReleaseItem( ActionBase action , boolean success , DistOperation op , DistRepositoryItem repoItem ) {
		String key = getReleaseKey( repoItem );
		
		ServerBlotterReleaseItem item = null;
		if( op == DistOperation.CREATE ) {
			item = new ServerBlotterReleaseItem( this , key );
			item.createReleaseItem( repoItem );
			items.put( item.ID , item );
			notifyItem( item , BlotterEvent.BLOTTER_START );
		}
		else {
			item = ( ServerBlotterReleaseItem )items.get( key );
			if( item == null )
				return( null );
			
			if( success && ( op == DistOperation.DROP || op == DistOperation.ARCHIVE ) ) {
				items.remove( item.ID );
				notifyItem( item , BlotterEvent.BLOTTER_STOP );
			}
			else
				notifyChildItem( item , action.blotterTreeItem , BlotterEvent.BLOTTER_RELEASEACTION );
		}
		
		return( item );
	}

	private String getReleaseKey( DistRepositoryItem repoItem ) {
		return( repoItem.repo.meta.name + "-" + repoItem.RELEASEDIR );
	}

	private String getReleaseKey( Dist dist ) {
		return( dist.meta.name + "-" + dist.RELEASEDIR );
	}
	
	public synchronized ServerBlotterReleaseItem findReleaseItem( Dist dist ) {
		String key = getReleaseKey( dist );
		ServerBlotterReleaseItem item = ( ServerBlotterReleaseItem )items.get( key );
		return( item );
	}

	public synchronized ServerBlotterReleaseItem findReleaseItem( String productName , String releaseVer ) {
		for( ServerBlotterItem item : items.values() ) {
			ServerBlotterReleaseItem releaseItem = ( ServerBlotterReleaseItem )item;
			if( releaseItem.repoItem.dist.isMaster() )
				continue;
			
			if( productName.equals( releaseItem.INFO_PRODUCT ) && releaseVer.equals( releaseItem.repoItem.dist.release.RELEASEVER ) )
				return( releaseItem );
		}
		return( null );
	}

	public boolean checkLifecycleUsed( String LC ) {
		for( ServerBlotterItem item : items.values() ) {
			ServerBlotterReleaseItem releaseItem = ( ServerBlotterReleaseItem )item;
			if( LC.equals( releaseItem.repoItem.dist.release.schedule.LIFECYCLE ) )
				return( true );
		}
		return( false );
	}

}
