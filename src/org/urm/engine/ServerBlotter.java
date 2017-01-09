package org.urm.engine;

import java.util.LinkedList;
import java.util.List;

import org.urm.action.ActionBase;
import org.urm.action.build.ActionPatch;
import org.urm.common.Common;
import org.urm.engine.action.ActionInit;
import org.urm.meta.product.MetaSourceProject;

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
	
	protected ServerBlotterSet blotterRoots;
	protected ServerBlotterSet blotterBuilds;
	protected ServerBlotterSet blotterReleases;
	protected ServerBlotterSet blotterDeploy;
	private List<ServerBlotterSet> blotters;
	
	public ServerBlotter( ServerEngine engine ) {
		this.engine = engine;

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
			ServerBlotterItem item = createRootItem( ( ActionInit )action );
			notifyItem( item , action , BlotterEvent.BLOTTER_START );
		}
		else
		if( action instanceof ActionPatch ) {
			ServerBlotterItem item = createBuildItem( ( ActionPatch )action );
			notifyItem( item , action , BlotterEvent.BLOTTER_START );
		}
		else {
			if( action.parent == null )
				Common.exitUnexpected();
			
			ServerBlotterItem item = action.parent.blotterItem;
			if( item == null )
				Common.exitUnexpected();
			
			ServerBlotterSet set = item.blotterSet;
			set.startChildAction( item , action );
			notifyItem( item , action , BlotterEvent.BLOTTER_STARTCHILD );
		}
	}
	
	public void stopAction( ActionBase action , boolean success ) throws Exception {
		if( action.blotterItem == null )
			Common.exitUnexpected();

		ServerBlotterItem item = action.blotterItem;
		if( item.action == action ) {
			item.stopAction( success );
			finishItem( item );
			notifyItem( item , action , BlotterEvent.BLOTTER_STOP );
		}
		else {
			ServerBlotterSet set = item.blotterSet;
			set.stopChildAction( item , action , success );
			notifyItem( item , action , BlotterEvent.BLOTTER_STOPCHILD );
		}
	}

	private ServerBlotterItem createRootItem( ActionInit action ) {
		ServerBlotterItem item = new ServerBlotterItem( blotterRoots , action );
		
		item.createRootItem();
		blotterRoots.addItem( item );
		return( item );
	}

	private ServerBlotterItem createBuildItem( ActionPatch action ) {
		ServerBlotterItem item = new ServerBlotterItem( blotterBuilds , action );
		
		MetaSourceProject project = action.builder.project;
		item.createBuildItem( project.meta.name , project.NAME , action.builder.TAG );
		blotterBuilds.addItem( item );
		return( item );
	}

	private void notifyItem( ServerBlotterItem item , ActionBase action , BlotterEvent event ) {
		ServerBlotterSet set = item.blotterSet;
		set.notifyItem( item , action , event );
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
