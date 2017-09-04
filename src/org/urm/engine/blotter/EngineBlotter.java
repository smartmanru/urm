package org.urm.engine.blotter;

import java.util.LinkedList;
import java.util.List;

import org.urm.action.ActionBase;
import org.urm.action.build.ActionBuild;
import org.urm.action.build.ActionGetBinary;
import org.urm.action.build.ActionGetManual;
import org.urm.action.build.ActionPatch;
import org.urm.action.conf.ActionGetConf;
import org.urm.action.database.ActionGetDB;
import org.urm.action.monitor.ActionMonitorTop;
import org.urm.action.release.ActionAddScope;
import org.urm.action.release.ActionAppendProd;
import org.urm.action.release.ActionArchiveRelease;
import org.urm.action.release.ActionCopyRelease;
import org.urm.action.release.ActionCreateProd;
import org.urm.action.release.ActionCreateRelease;
import org.urm.action.release.ActionDeleteRelease;
import org.urm.action.release.ActionDescope;
import org.urm.action.release.ActionFinishRelease;
import org.urm.action.release.ActionCompleteRelease;
import org.urm.action.release.ActionForceCloseRelease;
import org.urm.action.release.ActionGetCumulative;
import org.urm.action.release.ActionModifyRelease;
import org.urm.action.release.ActionReopenRelease;
import org.urm.action.release.ActionSchedulePhase;
import org.urm.action.release.ActionSetScope;
import org.urm.action.release.ActionTouchRelease;
import org.urm.common.Common;
import org.urm.engine.Engine;
import org.urm.engine.action.ActionInit;
import org.urm.engine.dist.Dist;
import org.urm.engine.dist.DistRepository;
import org.urm.engine.dist.DistRepository.DistOperation;
import org.urm.engine.events.ServerEvents;
import org.urm.engine.events.ServerEventsApp;
import org.urm.engine.events.ServerEventsListener;
import org.urm.engine.events.ServerEventsSubscription;
import org.urm.engine.dist.DistRepositoryItem;
import org.urm.meta.product.Meta;

public class EngineBlotter {

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
	
	public Engine engine;
	
	public long day;
	
	protected EngineBlotterSet blotterRoots;
	protected EngineBlotterSet blotterBuilds;
	protected EngineBlotterSet blotterReleases;
	protected EngineBlotterSet blotterDeploy;
	private List<EngineBlotterSet> blotters;
	
	public EngineBlotter( Engine engine ) {
		this.engine = engine;

		day = Common.getDayNoTime( System.currentTimeMillis() );
		
		blotters = new LinkedList<EngineBlotterSet>(); 
		blotterRoots = addBlotter( BlotterType.BLOTTER_ROOT , "blotter.roots" );
		blotterBuilds = addBlotter( BlotterType.BLOTTER_BUILD , "blotter.builds" );
		blotterReleases = addBlotter( BlotterType.BLOTTER_RELEASE , "blotter.releases" );
		blotterDeploy = addBlotter( BlotterType.BLOTTER_DEPLOY , "blotter.deploy" );
	}

	private EngineBlotterSet addBlotter( BlotterType type , String name ) {
		ServerEvents events = engine.getEvents();
		EngineBlotterSet set = new EngineBlotterSet( this , type , events , name );
		blotters.add( set );
		return( set );
	}
	
	public void init() {
		for( EngineBlotterSet set : blotters )
			set.init();
	}
	
	public void clear() {
		for( EngineBlotterSet set : blotters )
			set.clear();
	}
	
	public void start( ActionInit action ) throws Exception {
		for( EngineBlotterSet set : blotters )
			set.start( action );
	}
	
	public void runHouseKeeping( long time ) {
		long timeDay = Common.getDayNoTime( time );
		if( timeDay == day )
			return;
		
		day = timeDay;
		for( EngineBlotterSet set : blotters )
			set.houseKeeping( time );
	}
	
	public EngineBlotterItem[] getBlotterItems( BlotterType type , boolean includeFinished ) {
		EngineBlotterSet set = getBlotterSet( type );
		if( set == null )
			return( new EngineBlotterItem[0] );
		return( set.getItems( includeFinished ) ); 
	}
	
	public EngineBlotterItem getBlotterItem( BlotterType type , String ID ) {
		EngineBlotterSet set = getBlotterSet( type );
		if( set == null )
			return( null );
		return( set.getItem( ID ) ); 
	}
	
	public EngineBlotterStat getBlotterStatistics( BlotterType type ) {
		EngineBlotterSet set = getBlotterSet( type );
		if( set == null )
			return( null );
		return( set.getStatistics() ); 
	}
	
	public EngineBlotterSet getBlotterSet( BlotterType type ) {
		for( EngineBlotterSet set : blotters ) {
			if( set.type == type )
				return( set );
		}
		return( null );
	}
	
	public void startAction( ActionBase action ) throws Exception {
		if( action instanceof ActionInit ) {
			EngineBlotterItem item = blotterRoots.createRootItem( ( ActionInit )action );
			notifyItem( item , BlotterEvent.BLOTTER_START );
			return;
		}

		if( action instanceof ActionMonitorTop )
			return;
		
		if( action.parent.blotterTreeItem == null )
			return;
		
		EngineBlotterActionItem rootItem = action.parent.blotterRootItem;
		EngineBlotterTreeItem parentTreeItem = action.parent.blotterTreeItem;
		EngineBlotterActionItem parentBaseItem = getBaseItem( rootItem , parentTreeItem );

		if( action instanceof ActionPatch ) {
			EngineBlotterActionItem baseItem = blotterBuilds.createBuildItem( rootItem , parentBaseItem , parentTreeItem , ( ActionPatch )action );
			parentTreeItem.addChild( baseItem.treeItem );
			startChildAction( rootItem , parentBaseItem , baseItem.treeItem );
			notifyItem( baseItem , BlotterEvent.BLOTTER_START );
			return;
		}

		EngineBlotterTreeItem treeItem = blotterRoots.createChildItem( action , parentBaseItem , parentTreeItem );
		startChildAction( rootItem , parentBaseItem , treeItem );
	}

	private void startChildAction( EngineBlotterActionItem rootItem , EngineBlotterActionItem baseItem , EngineBlotterTreeItem treeItem ) {
		blotterRoots.startChildAction( rootItem , treeItem );
		notifyChildItem( rootItem , treeItem , BlotterEvent.BLOTTER_STARTCHILD );
		
		if( baseItem != rootItem ) {
			EngineBlotterSet set = baseItem.blotterSet;
			set.startChildAction( baseItem , treeItem );
			notifyChildItem( baseItem , treeItem , BlotterEvent.BLOTTER_STARTCHILD );
		}
	}
	
	private EngineBlotterActionItem getBaseItem( EngineBlotterActionItem rootItem , EngineBlotterTreeItem treeItem ) {
		EngineBlotterTreeItem parentItem = treeItem;
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
		EngineBlotterTreeItem treeItem = action.blotterTreeItem;
		EngineBlotterActionItem rootItem = treeItem.rootItem;
		EngineBlotterActionItem baseItem = treeItem.baseItem;
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
			runDistAction( xa , success , xa.src.meta , xa.dst , DistOperation.CREATE , "copy distributive from " + xa.src.RELEASEDIR + " to " + xa.RELEASEDST ); 
		}
		else
		if( action instanceof ActionCreateProd ) {
			ActionCreateProd xa = ( ActionCreateProd )action;
			runDistAction( xa , success , xa.meta , xa.dist , DistOperation.CREATE , "create/copy production master distributive version=" + xa.RELEASEVER ); 
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
		if( action instanceof ActionCompleteRelease ) {
			ActionCompleteRelease xa = ( ActionCompleteRelease )action;
			runDistAction( xa , success , xa.dist.meta , xa.dist , DistOperation.COMPLETE , "finalize distributive releasedir=" + xa.dist.RELEASEDIR ); 
		}
		else
		if( action instanceof ActionArchiveRelease ) {
			ActionArchiveRelease xa = ( ActionArchiveRelease )action;
			runDistAction( xa , success , xa.dist.meta , xa.dist , DistOperation.ARCHIVE , "archive distributive releasedir=" + xa.dist.RELEASEDIR ); 
		}
		else
		if( action instanceof ActionTouchRelease ) {
			ActionTouchRelease xa = ( ActionTouchRelease )action;
			runDistAction( xa , success , xa.meta , xa.dist , DistOperation.STATUS , "reload distributive releasedir=" + xa.RELEASELABEL ); 
		}
		else
		if( action instanceof ActionAppendProd ) {
			ActionAppendProd xa = ( ActionAppendProd )action;
			runDistAction( xa , success , xa.dist.meta , xa.dist , DistOperation.PUT , "append master distributive releasedir=" + xa.dist.RELEASEDIR ); 
		}
		else
		if( action instanceof ActionSchedulePhase ) {
			ActionSchedulePhase xa = ( ActionSchedulePhase )action;
			runDistAction( xa , success , xa.dist.meta , xa.dist , DistOperation.PHASE , "phase control distributive releasedir=" + xa.dist.RELEASEDIR ); 
		}
		else
		if( action instanceof ActionAddScope ) {
			ActionAddScope xa = ( ActionAddScope )action;
			runDistAction( xa , success , xa.dist.meta , xa.dist , DistOperation.MODIFY , "extend scope of distributive releasedir=" + xa.dist.RELEASEDIR ); 
		}
		else
		if( action instanceof ActionSetScope ) {
			ActionSetScope xa = ( ActionSetScope )action;
			runDistAction( xa , success , xa.dist.meta , xa.dist , DistOperation.MODIFY , "set scope of distributive releasedir=" + xa.dist.RELEASEDIR ); 
		}
		else
		if( action instanceof ActionDescope ) {
			ActionDescope xa = ( ActionDescope )action;
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
		if( action instanceof ActionBuild ) {
			ActionBuild xa = ( ActionBuild )action;
			if( xa.dist != null )
				runDistAction( xa , success , xa.dist.meta , xa.dist , DistOperation.BUILD , "build release releasedir=" + xa.dist.RELEASEDIR ); 
		}
		else
		if( action instanceof ActionGetBinary ) {
			ActionGetBinary xa = ( ActionGetBinary )action;
			runDistAction( xa , success , xa.targetRelease.meta , xa.targetRelease , DistOperation.PUT , "put binary items to distributive releasedir=" + xa.targetRelease.RELEASEDIR ); 
		}
		else
		if( action instanceof ActionGetConf ) {
			ActionGetConf xa = ( ActionGetConf )action;
			runDistAction( xa , success , xa.dist.meta , xa.dist , DistOperation.PUT , "put configuration items to distributive releasedir=" + xa.dist.RELEASEDIR ); 
		}
		else
		if( action instanceof ActionGetManual ) {
			ActionGetManual xa = ( ActionGetManual )action;
			runDistAction( xa , success , xa.dist.meta , xa.dist , DistOperation.PUT , "put manual items to distributive releasedir=" + xa.dist.RELEASEDIR ); 
		}
		else
		if( action instanceof ActionGetDB ) {
			ActionGetDB xa = ( ActionGetDB )action;
			runDistAction( xa , success , xa.dist.meta , xa.dist , DistOperation.PUT , "put database items to distributive releasedir=" + xa.dist.RELEASEDIR ); 
		}
	}

	public void runDistStatus( ActionBase action , Meta meta , Dist dist ) {
		try {
			DistRepository repo = action.artefactory.getDistRepository( action , meta );
			DistRepositoryItem distItem = repo.findRunItem( action , dist );
			if( distItem == null )
				return;
			
			blotterReleases.affectReleaseItem( action , true , DistOperation.STATUS , distItem );
		}
		catch( Throwable e ) {
			action.log( "change release status in blotter" , e );
		}
	}
	
	private void runDistAction( ActionBase action , boolean success , Meta meta , Dist dist , DistOperation op , String msg ) {
		try {
			DistRepositoryItem distItem = null;
			DistRepository repo = action.artefactory.getDistRepository( action , meta );
			if( op != DistOperation.STATUS )
				distItem = repo.addDistAction( action , success , dist , op , msg );
			else {
				if( dist != null )
					distItem = repo.findRunItem( action , dist );
			}
			
			if( distItem == null )
				return;
			
			blotterReleases.affectReleaseItem( action , success , op , distItem );
		}
		catch( Throwable e ) {
			action.log( "add release action to blotter" , e );
		}
	}
	
	private void stopChildAction( EngineBlotterActionItem rootItem , EngineBlotterActionItem baseItem , EngineBlotterTreeItem treeItem , boolean success ) {
		blotterRoots.stopChildAction( rootItem , treeItem , success );
		notifyChildItem( rootItem , treeItem , BlotterEvent.BLOTTER_STOPCHILD );
		
		if( baseItem != rootItem ) {
			EngineBlotterSet set = baseItem.blotterSet;
			set.stopChildAction( baseItem , treeItem , success );
			notifyChildItem( baseItem , treeItem , BlotterEvent.BLOTTER_STOPCHILD );
		}
	}
	
	private void notifyItem( EngineBlotterItem item , BlotterEvent event ) {
		EngineBlotterSet set = item.blotterSet;
		set.notifyItem( item , event );
	}
	
	private void notifyChildItem( EngineBlotterItem baseItem , EngineBlotterTreeItem treeItem , BlotterEvent event ) {
		EngineBlotterSet set = baseItem.blotterSet;
		set.notifyChildItem( baseItem , treeItem , event );
	}
	
	private void finishItem( EngineBlotterActionItem item ) {
		EngineBlotterSet set = item.blotterSet;
		set.finishItem( item );
	}
	
	public ServerEventsSubscription subscribe( ServerEventsApp app , ServerEventsListener listener , BlotterType type ) {
		EngineBlotterSet set = getBlotterSet( type );
		if( set == null )
			return( null );
		
		return( app.subscribe( set , listener ) );
	}

}
