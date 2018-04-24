package org.urm.engine.blotter;

import java.util.HashMap;
import java.util.Map;

import org.urm.action.ActionBase;
import org.urm.action.codebase.ActionPatch;
import org.urm.engine.BlotterService;
import org.urm.engine.EventService;
import org.urm.engine.BlotterService.BlotterEvent;
import org.urm.engine.BlotterService.BlotterType;
import org.urm.engine.action.ActionInit;
import org.urm.engine.data.EngineDirectory;
import org.urm.engine.dist.Dist;
import org.urm.engine.events.EngineEventsSource;
import org.urm.engine.events.EngineEventsState;
import org.urm.engine.products.EngineProductReleases;
import org.urm.engine.run.EngineMethod;
import org.urm.meta.engine.AppProduct;
import org.urm.meta.product.Meta;
import org.urm.meta.product.MetaSourceProject;
import org.urm.meta.release.Release;
import org.urm.meta.release.ReleaseRepository.ReleaseOperation;

public class EngineBlotterSet extends EngineEventsSource {

	public BlotterService blotter;
	public BlotterType type;
	
	private Map<String,EngineBlotterItem> items;
	private Map<String,EngineBlotterMemo> memos;
	private EngineBlotterStat stat;
	
	public EngineBlotterSet( BlotterService blotter , BlotterType type , EventService events , String setId ) {
		super( events , setId );
		this.blotter = blotter;
		this.type = type;

		items = new HashMap<String,EngineBlotterItem>();
		memos = new HashMap<String,EngineBlotterMemo>();
		stat = new EngineBlotterStat( this );
	}
	
	@Override
	public EngineEventsState getState() {
		return( new EngineEventsState( this , super.getStateId() ) );
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
		EngineDirectory directory = action.getEngineDirectory();
		for( String productName : directory.getProductNames() ) {
			AppProduct product = directory.findProduct( productName );
			EngineProductReleases releases =  product.findReleases();
			startReleaseSetRepo( action , releases );
		}
	}

	public synchronized void startReleaseSetRepo( ActionInit action , EngineProductReleases releases ) throws Exception {
		for( String version : releases.getActiveVersions() ) {
			Release release = releases.findRelease( version );
			String key = getReleaseKey( release );
			EngineBlotterReleaseItem item = new EngineBlotterReleaseItem( this , key );
			item.createReleaseItem( release );
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
			EngineBlotterItem[] set = items.values().toArray( new EngineBlotterItem[0] );
			for( EngineBlotterItem item : set ) {
				if( item.toberemoved ) {
					items.remove( item.ID );
					removeItem( item );
				}
			}
		}
		else {
			for( EngineBlotterItem item : items.values() )
				removeItem( item );
			items.clear();
		}
	}
	
	public synchronized EngineBlotterStat getStatistics() {
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

	public synchronized EngineBlotterItem[] getItems( boolean includeFinished ) {
		Map<String,EngineBlotterItem> selected = null;
		if( includeFinished )
			selected = items;
		else {
			selected = new HashMap<String,EngineBlotterItem>();
			for( EngineBlotterItem item : items.values() ) {
				if( !item.toberemoved )
					selected.put( item.ID , item );
			}
		}
			
		return( selected.values().toArray( new EngineBlotterItem[0] ) ); 
	}

	public synchronized EngineBlotterItem getItem( String ID ) {
		return( items.get( ID ) );
	}
	
	public synchronized void finishItem( EngineBlotterActionItem item ) {
		EngineBlotterMemo memo = item.memo;
		if( memo != null && item.success ) {
			long elapsed = item.stopTime - item.startTime;
			memo.addEvent( elapsed );
		}
		
		stat.statFinishItem( item );
	}
	
	public void notifyItem( ActionBase action , EngineBlotterItem item , BlotterEvent event ) {
		EngineBlotterEvent data = new EngineBlotterEvent( item , event );
		addBlotterEvent( action , data );
	}
	
	public void notifyChildItem( ActionBase action , EngineBlotterItem baseItem , EngineBlotterTreeItem treeItem , BlotterEvent event ) {
		EngineBlotterEvent data = new EngineBlotterEvent( baseItem , treeItem , event );
		addBlotterEvent( action , data );
	}

	private void addBlotterEvent( ActionBase action , EngineBlotterEvent data ) {
		EngineMethod method = action.getMethod();
		if( method != null )
			method.addCommitEvent( this , EventService.OWNER_ENGINE , EventService.EVENT_BLOTTEREVENT , data );
		else
			super.notify( EventService.OWNER_ENGINE , EventService.EVENT_BLOTTEREVENT , data );
	}
	
	public synchronized void startChildAction( EngineBlotterActionItem baseItem , EngineBlotterTreeItem treeItem ) {
		baseItem.startChildAction( treeItem );
		stat.statAddChildItem( baseItem , treeItem );
	}

	public synchronized void stopChildAction( EngineBlotterActionItem baseItem , EngineBlotterTreeItem treeItem , boolean success ) {
		baseItem.stopChildAction( treeItem , success );
		stat.statFinishChildItem( baseItem , treeItem , success );
	}

	public synchronized EngineBlotterActionItem createRootItem( ActionInit action ) {
		EngineBlotterActionItem item = new EngineBlotterActionItem( this , action , null , null , null );
		
		item.createRootItem();
		addItem( item );
		return( item );
	}

	public synchronized EngineBlotterActionItem createBuildItem( EngineBlotterActionItem rootItem , EngineBlotterActionItem baseItem , EngineBlotterTreeItem parentTreeItem , ActionPatch action ) {
		EngineBlotterActionItem item = new EngineBlotterActionItem( this , action , rootItem , baseItem , parentTreeItem );
		
		MetaSourceProject project = action.builder.project;
		item.createBuildItem( project.meta.name , project.NAME , action.builder.TAG , action.logDir , action.logFile );
		addItem( item );
		return( item );
	}

	public synchronized EngineBlotterTreeItem createChildItem( ActionBase action , EngineBlotterActionItem parentBaseItem , EngineBlotterTreeItem parentTreeItem ) {
		EngineBlotterTreeItem item = new EngineBlotterTreeItem( action , parentTreeItem.rootItem , parentTreeItem , null );
		parentTreeItem.addChild( item );
		action.setBlotterItem( parentBaseItem , item );
		return( item );
	}
	
	private void removeItem( EngineBlotterItem item ) {
		item.setRemoved();
		
		if( item.isRootItem() ) {
			try {
				EngineBlotterActionItem rootItem = ( EngineBlotterActionItem )item; 
				ActionInit action = ( ActionInit )rootItem.action;
				if( rootItem.success && action.isDebug() == false )
					action.artefactory.workFolder.removeThis( action );
			}
			catch( Throwable e ) {
				blotter.engine.log( "Clear roots" , e );
			}
		}
	}
	
	private void addItem( EngineBlotterActionItem item ) {
		if( item.isBuildItem() ) {
			String key = "build#" + item.INFO_PRODUCT + "#" + item.INFO_PROJECT;
			EngineBlotterMemo memo = memos.get( key );
			if( memo == null ) {
				memo = new EngineBlotterMemo( this , key );
				memos.put( key , memo );
			}
			
			item.setMemo( memo );
		}
		
		items.put( item.ID , item );
		stat.statAddItem( item );
	}

	
	public synchronized EngineBlotterReleaseItem affectReleaseItem( ActionBase action , ReleaseOperation op , Release release ) {
		String key = getReleaseKey( release );
		
		EngineBlotterReleaseItem item = null;
		if( op == ReleaseOperation.CREATE ) {
			item = new EngineBlotterReleaseItem( this , key );
			item.createReleaseItem( release );
			items.put( item.ID , item );
			notifyItem( action , item , BlotterEvent.BLOTTER_START );
		}
		else {
			item = ( EngineBlotterReleaseItem )items.get( key );
			if( item == null )
				return( null );
			
			if( op == ReleaseOperation.DROP || op == ReleaseOperation.ARCHIVE ) {
				items.remove( item.ID );
				notifyItem( action , item , BlotterEvent.BLOTTER_STOP );
			}
			else
				notifyChildItem( action , item , action.blotterTreeItem , BlotterEvent.BLOTTER_RELEASEACTION );
		}
		
		return( item );
	}

	private String getReleaseKey( Release release ) {
		Meta meta = release.getMeta();
		return( meta.name + "-" + release.RELEASEVER );
	}

	private String getReleaseKey( Dist dist ) {
		return( dist.meta.name + "-" + dist.RELEASEDIR );
	}
	
	public synchronized EngineBlotterReleaseItem findReleaseItem( Dist dist ) {
		String key = getReleaseKey( dist );
		EngineBlotterReleaseItem item = ( EngineBlotterReleaseItem )items.get( key );
		return( item );
	}

}
