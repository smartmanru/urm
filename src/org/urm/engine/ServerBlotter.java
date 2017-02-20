package org.urm.engine;

import java.util.LinkedList;
import java.util.List;

import org.urm.action.ActionBase;
import org.urm.action.build.ActionGetBinary;
import org.urm.action.build.ActionGetManual;
import org.urm.action.build.ActionPatch;
import org.urm.action.conf.ActionGetConf;
import org.urm.action.database.ActionGetDB;
import org.urm.action.monitor.ActionMonitorTop;
import org.urm.action.release.ActionAddScope;
import org.urm.action.release.ActionCopyRelease;
import org.urm.action.release.ActionCreateProd;
import org.urm.action.release.ActionCreateRelease;
import org.urm.action.release.ActionDeleteRelease;
import org.urm.action.release.ActionDescope;
import org.urm.action.release.ActionFinishRelease;
import org.urm.action.release.ActionForceCloseRelease;
import org.urm.action.release.ActionGetCumulative;
import org.urm.action.release.ActionModifyRelease;
import org.urm.action.release.ActionReopenRelease;
import org.urm.common.Common;
import org.urm.engine.action.ActionInit;
import org.urm.engine.dist.Dist;
import org.urm.engine.dist.DistRepository;
import org.urm.engine.dist.DistRepository.DistOperation;
import org.urm.engine.dist.DistRepositoryItem;
import org.urm.meta.product.Meta;

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
		BLOTTER_STOPCHILD ,
		BLOTTER_RELEASEACTION
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

		day = Common.getDayNoTime( System.currentTimeMillis() );
		
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
	
	public void start( ActionInit action ) throws Exception {
		for( ServerBlotterSet set : blotters )
			set.start( action );
	}
	
	public void runHouseKeeping( long time ) {
		long timeDay = Common.getDayNoTime( time );
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
	
	public ServerBlotterItem getBlotterItem( BlotterType type , String ID ) {
		ServerBlotterSet set = getBlotterSet( type );
		if( set == null )
			return( null );
		return( set.getItem( ID ) ); 
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

		if( action instanceof ActionMonitorTop )
			return;
		
		if( action.parent.blotterTreeItem == null )
			return;
		
		ServerBlotterActionItem rootItem = action.parent.blotterRootItem;
		ServerBlotterTreeItem parentTreeItem = action.parent.blotterTreeItem;
		ServerBlotterActionItem parentBaseItem = getBaseItem( rootItem , parentTreeItem );

		if( action instanceof ActionPatch ) {
			ServerBlotterActionItem baseItem = blotterBuilds.createBuildItem( rootItem , parentBaseItem , parentTreeItem , ( ActionPatch )action );
			parentTreeItem.addChild( baseItem.treeItem );
			startChildAction( rootItem , parentBaseItem , baseItem.treeItem );
			notifyItem( baseItem , BlotterEvent.BLOTTER_START );
			return;
		}

		ServerBlotterTreeItem treeItem = blotterRoots.createChildItem( action , parentBaseItem , parentTreeItem );
		startChildAction( rootItem , parentBaseItem , treeItem );
	}

	private void startChildAction( ServerBlotterActionItem rootItem , ServerBlotterActionItem baseItem , ServerBlotterTreeItem treeItem ) {
		blotterRoots.startChildAction( rootItem , treeItem );
		notifyChildItem( rootItem , treeItem , BlotterEvent.BLOTTER_STARTCHILD );
		
		if( baseItem != rootItem ) {
			ServerBlotterSet set = baseItem.blotterSet;
			set.startChildAction( baseItem , treeItem );
			notifyChildItem( baseItem , treeItem , BlotterEvent.BLOTTER_STARTCHILD );
		}
	}
	
	private ServerBlotterActionItem getBaseItem( ServerBlotterActionItem rootItem , ServerBlotterTreeItem treeItem ) {
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

		// action tree
		ServerBlotterTreeItem treeItem = action.blotterTreeItem;
		ServerBlotterActionItem rootItem = treeItem.rootItem;
		ServerBlotterActionItem baseItem = treeItem.baseItem;
		treeItem.stopAction( success );

		if( baseItem != null ) {
			baseItem.stopAction( success );
			finishItem( baseItem );
			notifyItem( baseItem , BlotterEvent.BLOTTER_STOP );
			
			if( baseItem != rootItem )
				stopChildAction( rootItem , baseItem.parent , treeItem , success );
		}
		else {
			baseItem = getBaseItem( rootItem , treeItem );
			stopChildAction( rootItem , baseItem , treeItem , success );
		}
		
		// release blotter
		if( action instanceof ActionCopyRelease ) {
			ActionCopyRelease xa = ( ActionCopyRelease )action;
			runDistAction( xa , success , xa.src.meta , xa.dst , DistOperation.CREATE , "copy distributive from " + xa.src.RELEASEDIR + " to " + xa.dst.RELEASEDIR ); 
		}
		else
		if( action instanceof ActionCreateProd ) {
			ActionCreateProd xa = ( ActionCreateProd )action;
			runDistAction( xa , success , xa.meta , xa.dist , DistOperation.CREATE , "create complete production distributive version=" + xa.RELEASEVER ); 
		}
		else
		if( action instanceof ActionCreateRelease ) {
			ActionCreateRelease xa = ( ActionCreateRelease )action;
			runDistAction( xa , success , xa.meta , xa.dist , DistOperation.CREATE , "create distributive releaselabel=" + xa.RELEASELABEL ); 
		}
		else
		if( action instanceof ActionDeleteRelease ) {
			ActionDeleteRelease xa = ( ActionDeleteRelease )action;
			runDistAction( xa , success , xa.dist.meta , xa.dist , DistOperation.DROP , "drop distributive releasedir=" + xa.dist.RELEASEDIR ); 
		}
		else
		if( action instanceof ActionFinishRelease ) {
			ActionFinishRelease xa = ( ActionFinishRelease )action;
			runDistAction( xa , success , xa.dist.meta , xa.dist , DistOperation.FINISH , "finalize distributive releasedir=" + xa.dist.RELEASEDIR ); 
		}
		else
		if( action instanceof ActionReopenRelease ) {
			ActionReopenRelease xa = ( ActionReopenRelease )action;
			runDistAction( xa , success , xa.dist.meta , xa.dist , DistOperation.REOPEN , "reopen distributive releasedir=" + xa.dist.RELEASEDIR ); 
		}
		else
		if( action instanceof ActionAddScope ) {
			ActionAddScope xa = ( ActionAddScope )action;
			runDistAction( xa , success , xa.dist.meta , xa.dist , DistOperation.MODIFY , "extend scope of distributive releasedir=" + xa.dist.RELEASEDIR ); 
		}
		else
		if( action instanceof ActionDescope ) {
			ActionFinishRelease xa = ( ActionFinishRelease )action;
			runDistAction( xa , success , xa.dist.meta , xa.dist , DistOperation.MODIFY , "reduce scope of distributive releasedir=" + xa.dist.RELEASEDIR ); 
		}
		else
		if( action instanceof ActionForceCloseRelease ) {
			ActionForceCloseRelease xa = ( ActionForceCloseRelease )action;
			runDistAction( xa , success , xa.dist.meta , xa.dist , DistOperation.MODIFY , "close distributive releasedir=" + xa.dist.RELEASEDIR ); 
		}
		else
		if( action instanceof ActionGetCumulative ) {
			ActionGetCumulative xa = ( ActionGetCumulative )action;
			runDistAction( xa , success , xa.dist.meta , xa.dist , DistOperation.PUT , "put cumulative items to distributive releasedir=" + xa.dist.RELEASEDIR ); 
		}
		else
		if( action instanceof ActionModifyRelease ) {
			ActionModifyRelease xa = ( ActionModifyRelease )action;
			runDistAction( xa , success , xa.dist.meta , xa.dist , DistOperation.MODIFY , "modify properties of distributive releasedir=" + xa.dist.RELEASEDIR ); 
		}
		else
		if( action instanceof ActionGetBinary ) {
			ActionGetCumulative xa = ( ActionGetCumulative )action;
			runDistAction( xa , success , xa.dist.meta , xa.dist , DistOperation.PUT , "put binary items to distributive releasedir=" + xa.dist.RELEASEDIR ); 
		}
		else
		if( action instanceof ActionGetConf ) {
			ActionGetCumulative xa = ( ActionGetCumulative )action;
			runDistAction( xa , success , xa.dist.meta , xa.dist , DistOperation.PUT , "put configuration items to distributive releasedir=" + xa.dist.RELEASEDIR ); 
		}
		else
		if( action instanceof ActionGetManual ) {
			ActionGetCumulative xa = ( ActionGetCumulative )action;
			runDistAction( xa , success , xa.dist.meta , xa.dist , DistOperation.PUT , "put manual items to distributive releasedir=" + xa.dist.RELEASEDIR ); 
		}
		else
		if( action instanceof ActionGetDB ) {
			ActionGetCumulative xa = ( ActionGetCumulative )action;
			runDistAction( xa , success , xa.dist.meta , xa.dist , DistOperation.PUT , "put database items to distributive releasedir=" + xa.dist.RELEASEDIR ); 
		}
	}

	private void runDistAction( ActionBase action , boolean success , Meta meta , Dist dist , DistOperation op , String msg ) {
		try {
			DistRepository repo = action.artefactory.getDistRepository( action , meta );
			DistRepositoryItem distItem = repo.addDistAction( action , success , dist , op , msg );
			if( distItem == null )
				return;
			
			blotterReleases.affectReleaseItem( action , success , op , distItem );
		}
		catch( Throwable e ) {
			action.log( "add release action to blotter" , e );
		}
	}
	
	private void stopChildAction( ServerBlotterActionItem rootItem , ServerBlotterActionItem baseItem , ServerBlotterTreeItem treeItem , boolean success ) {
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
	
	private void finishItem( ServerBlotterActionItem item ) {
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
