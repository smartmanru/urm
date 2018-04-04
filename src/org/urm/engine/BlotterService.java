package org.urm.engine;

import java.util.LinkedList;
import java.util.List;

import org.urm.action.ActionBase;
import org.urm.action.codebase.ActionBuild;
import org.urm.action.codebase.ActionPatch;
import org.urm.action.release.ActionAddScope;
import org.urm.action.release.ActionAppendMaster;
import org.urm.action.release.ActionArchiveRelease;
import org.urm.action.release.ActionCopyRelease;
import org.urm.action.release.ActionCreateMaster;
import org.urm.action.release.ActionCreateRelease;
import org.urm.action.release.ActionDeleteRelease;
import org.urm.action.release.ActionDescope;
import org.urm.action.release.ActionFinishRelease;
import org.urm.action.release.ActionCompleteRelease;
import org.urm.action.release.ActionGetCumulative;
import org.urm.action.release.ActionImportRelease;
import org.urm.action.release.ActionModifyRelease;
import org.urm.action.release.ActionReopenRelease;
import org.urm.action.release.ActionSchedulePhase;
import org.urm.action.release.ActionSetScope;
import org.urm.action.release.ActionTouchRelease;
import org.urm.common.Common;
import org.urm.engine.action.ActionInit;
import org.urm.engine.blotter.EngineBlotterActionItem;
import org.urm.engine.blotter.EngineBlotterItem;
import org.urm.engine.blotter.EngineBlotterSet;
import org.urm.engine.blotter.EngineBlotterStat;
import org.urm.engine.blotter.EngineBlotterTreeItem;
import org.urm.engine.events.EngineEventsApp;
import org.urm.engine.events.EngineEventsListener;
import org.urm.engine.events.EngineEventsSubscription;
import org.urm.meta.release.Release;
import org.urm.meta.release.ReleaseRepository.ReleaseOperation;

public class BlotterService {

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
	
	public BlotterService( Engine engine ) {
		this.engine = engine;

		day = Common.getDayNoTime( System.currentTimeMillis() );
		
		blotters = new LinkedList<EngineBlotterSet>(); 
		blotterRoots = addBlotter( BlotterType.BLOTTER_ROOT , "blotter.roots" );
		blotterBuilds = addBlotter( BlotterType.BLOTTER_BUILD , "blotter.builds" );
		blotterReleases = addBlotter( BlotterType.BLOTTER_RELEASE , "blotter.releases" );
		blotterDeploy = addBlotter( BlotterType.BLOTTER_DEPLOY , "blotter.deploy" );
	}

	private EngineBlotterSet addBlotter( BlotterType type , String name ) {
		EventService events = engine.getEvents();
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
			runReleaseAction( xa , success , xa.release , ReleaseOperation.CREATE , "copy distributive from " + xa.src.RELEASEVER + " to " + xa.RELEASEDIR ); 
		}
		else
		if( action instanceof ActionCreateMaster ) {
			ActionCreateMaster xa = ( ActionCreateMaster )action;
			runReleaseAction( xa , success , xa.release , ReleaseOperation.CREATE , "create/copy production master distributive version=" + xa.RELEASEVER ); 
		}
		else
		if( action instanceof ActionCreateRelease ) {
			ActionCreateRelease xa = ( ActionCreateRelease )action;
			runReleaseAction( xa , success , xa.release , ReleaseOperation.CREATE , "create release label=" + xa.RELEASELABEL ); 
		}
		else
		if( action instanceof ActionImportRelease ) {
			ActionImportRelease xa = ( ActionImportRelease )action;
			runReleaseAction( xa , success , xa.release , ReleaseOperation.CREATE , "create release label=" + xa.RELEASELABEL ); 
		}
		else
		if( action instanceof ActionDeleteRelease ) {
			ActionDeleteRelease xa = ( ActionDeleteRelease )action;
			runReleaseAction( xa , success , xa.release , ReleaseOperation.DROP , "drop release version=" + xa.release.RELEASEVER ); 
		}
		else
		if( action instanceof ActionFinishRelease ) {
			ActionFinishRelease xa = ( ActionFinishRelease )action;
			runReleaseAction( xa , success , xa.release , ReleaseOperation.FINISH , "finalize release version=" + xa.release.RELEASEVER ); 
		}
		else
		if( action instanceof ActionReopenRelease ) {
			ActionReopenRelease xa = ( ActionReopenRelease )action;
			runReleaseAction( xa , success , xa.release , ReleaseOperation.REOPEN , "reopen release version=" + xa.release.RELEASEVER ); 
		}
		else
		if( action instanceof ActionCompleteRelease ) {
			ActionCompleteRelease xa = ( ActionCompleteRelease )action;
			runReleaseAction( xa , success , xa.release , ReleaseOperation.COMPLETE , "finalize release version=" + xa.release.RELEASEVER ); 
		}
		else
		if( action instanceof ActionArchiveRelease ) {
			ActionArchiveRelease xa = ( ActionArchiveRelease )action;
			runReleaseAction( xa , success , xa.release , ReleaseOperation.ARCHIVE , "archive release version=" + xa.release.RELEASEVER ); 
		}
		else
		if( action instanceof ActionTouchRelease ) {
			ActionTouchRelease xa = ( ActionTouchRelease )action;
			runReleaseAction( xa , success , xa.release , ReleaseOperation.STATUS , "reload distributive releasedir=" + xa.RELEASELABEL ); 
		}
		else
		if( action instanceof ActionAppendMaster ) {
			ActionAppendMaster xa = ( ActionAppendMaster )action;
			runReleaseAction( xa , success , xa.release , ReleaseOperation.PUT , "append master distributive releasedir=" + xa.release.RELEASEVER ); 
		}
		else
		if( action instanceof ActionSchedulePhase ) {
			ActionSchedulePhase xa = ( ActionSchedulePhase )action;
			runReleaseAction( xa , success , xa.release , ReleaseOperation.PHASE , "phase control distributive releasedir=" + xa.release.RELEASEVER ); 
		}
		else
		if( action instanceof ActionAddScope ) {
			ActionAddScope xa = ( ActionAddScope )action;
			runReleaseAction( xa , success , xa.release , ReleaseOperation.MODIFY , "extend scope of distributive releasedir=" + xa.release.RELEASEVER ); 
		}
		else
		if( action instanceof ActionSetScope ) {
			ActionSetScope xa = ( ActionSetScope )action;
			runReleaseAction( xa , success , xa.release , ReleaseOperation.MODIFY , "set scope of distributive releasedir=" + xa.release.RELEASEVER ); 
		}
		else
		if( action instanceof ActionDescope ) {
			ActionDescope xa = ( ActionDescope )action;
			runReleaseAction( xa , success , xa.release , ReleaseOperation.MODIFY , "reduce scope of distributive releasedir=" + xa.release.RELEASEVER ); 
		}
		else
		if( action instanceof ActionGetCumulative ) {
			ActionGetCumulative xa = ( ActionGetCumulative )action;
			runReleaseAction( xa , success , xa.release , ReleaseOperation.PUT , "put cumulative items to distributive releasedir=" + xa.release.RELEASEVER ); 
		}
		else
		if( action instanceof ActionModifyRelease ) {
			ActionModifyRelease xa = ( ActionModifyRelease )action;
			runReleaseAction( xa , success , xa.release , ReleaseOperation.MODIFY , "modify properties of distributive releasedir=" + xa.release.RELEASEVER ); 
		}
		else
		if( action instanceof ActionBuild ) {
			ActionBuild xa = ( ActionBuild )action;
			if( xa.release != null )
				runReleaseAction( xa , success , xa.release , ReleaseOperation.BUILD , "build release releasedir=" + xa.release.RELEASEVER ); 
		}
	}

	public void runReleaseStatus( ActionBase action , Release release ) {
		try {
			blotterReleases.affectReleaseItem( action , true , ReleaseOperation.STATUS , release );
		}
		catch( Throwable e ) {
			action.log( "change release status in blotter" , e );
		}
	}
	
	private void runReleaseAction( ActionBase action , boolean success , Release release , ReleaseOperation op , String msg ) {
		try {
			if( op != ReleaseOperation.STATUS && release != null )
				blotterReleases.affectReleaseItem( action , success , op , release );
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
	
	public EngineEventsSubscription subscribe( EngineEventsApp app , EngineEventsListener listener , BlotterType type ) {
		EngineBlotterSet set = getBlotterSet( type );
		if( set == null )
			return( null );
		
		return( app.subscribe( set , listener ) );
	}

}
