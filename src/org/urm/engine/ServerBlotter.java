package org.urm.engine;

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
	
	private ServerBlotterSet blotterRoots;
	private ServerBlotterSet blotterBuilds;
	private ServerBlotterSet blotterReleases;
	private ServerBlotterSet blotterDeploy;
	
	public ServerBlotter( ServerEngine engine ) {
		this.engine = engine;

		ServerEvents events = engine.getEvents();
		blotterRoots = new ServerBlotterSet( this , BlotterType.BLOTTER_ROOT , events , "blotter.roots" );
		blotterBuilds = new ServerBlotterSet( this , BlotterType.BLOTTER_BUILD , events , "blotter.builds" );
		blotterReleases = new ServerBlotterSet( this , BlotterType.BLOTTER_RELEASE , events , "blotter.releases" );
		blotterDeploy = new ServerBlotterSet( this , BlotterType.BLOTTER_DEPLOY , events , "blotter.deploy" );
	}
	
	public ServerBlotterItem[] getBlotterItems( BlotterType type ) {
		ServerBlotterSet set = getBlotterSet( type );
		if( set == null )
			return( new ServerBlotterItem[0] );
		return( set.getItems() ); 
	}
	
	public ServerBlotterStat getBlotterStatistics( BlotterType type ) {
		ServerBlotterSet set = getBlotterSet( type );
		if( set == null )
			return( null );
		return( set.getStatistics() ); 
	}
	
	public ServerBlotterSet getBlotterSet( BlotterType type ) {
		if( type == BlotterType.BLOTTER_ROOT )
			return( blotterRoots );
		if( type == BlotterType.BLOTTER_BUILD )
			return( blotterBuilds );
		if( type == BlotterType.BLOTTER_RELEASE )
			return( blotterReleases );
		if( type == BlotterType.BLOTTER_DEPLOY )
			return( blotterDeploy );
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
			notifyItem( item , action , BlotterEvent.BLOTTER_STOP );
			finishItem( item );
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
