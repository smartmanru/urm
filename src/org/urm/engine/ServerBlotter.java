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
	
	private List<ServerBlotterItem> listRoots;
	private List<ServerBlotterItem> listBuilds;
	private List<ServerBlotterItem> listReleases;
	private List<ServerBlotterItem> listDeploy;
	
	public ServerBlotter( ServerEngine engine ) {
		this.engine = engine;
		
		listRoots = new LinkedList<ServerBlotterItem>();
		listBuilds = new LinkedList<ServerBlotterItem>();
		listReleases = new LinkedList<ServerBlotterItem>();
		listDeploy = new LinkedList<ServerBlotterItem>();
	}
	
	public synchronized ServerBlotterItem[] getRootItems() {
		return( listRoots.toArray( new ServerBlotterItem[0] ) ); 
	}
	
	public synchronized ServerBlotterItem[] getBuildItems() {
		return( listBuilds.toArray( new ServerBlotterItem[0] ) ); 
	}
	
	public synchronized ServerBlotterItem[] getReleaseItems() {
		return( listReleases.toArray( new ServerBlotterItem[0] ) ); 
	}
	
	public synchronized ServerBlotterItem[] getDeployItems() {
		return( listDeploy.toArray( new ServerBlotterItem[0] ) ); 
	}
	
	public void startAction( ActionBase action ) throws Exception {
		if( action instanceof ActionInit ) {
			ServerBlotterItem item = createRootItem( ( ActionInit )action );
			notifyRootItem( item , BlotterEvent.BLOTTER_START );
		}
		else
		if( action instanceof ActionPatch ) {
			ServerBlotterItem item = createBuildItem( ( ActionPatch )action );
			notifyBuildItem( item , BlotterEvent.BLOTTER_START );
		}
		else {
			if( action.parent == null )
				Common.exitUnexpected();
			
			ServerBlotterItem item = action.parent.blotterItem;
			if( item == null )
				Common.exitUnexpected();
			
			item.startChildAction( action );
			if( item.isRootItem() )
				notifyRootItem( item , BlotterEvent.BLOTTER_STARTCHILD );
			else
			if( item.isBuildItem() )
				notifyBuildItem( item , BlotterEvent.BLOTTER_STARTCHILD );
			else
			if( item.isReleaseItem() )
				notifyReleaseItem( item , BlotterEvent.BLOTTER_STARTCHILD );
			else
			if( item.isDeployItem() )
				notifyDeployItem( item , BlotterEvent.BLOTTER_STARTCHILD );
			else
				Common.exitUnexpected();
		}
	}
	
	public void stopAction( ActionBase action , boolean success ) throws Exception {
		if( action.blotterItem == null )
			Common.exitUnexpected();

		ServerBlotterItem item = action.blotterItem;
		if( item.action == action ) {
			item.stopAction( success );
			if( item.isRootItem() ) {
				notifyRootItem( item , BlotterEvent.BLOTTER_STOP );
				removeRootItem( item );
			}
			else
			if( item.isBuildItem() ) {
				notifyBuildItem( item , BlotterEvent.BLOTTER_STOP );
				removeBuildItem( item );
			}
			else
			if( item.isReleaseItem() ) {
				notifyReleaseItem( item , BlotterEvent.BLOTTER_STOP );
				removeReleaseItem( item );
			}
			else
			if( item.isDeployItem() ) {
				notifyDeployItem( item , BlotterEvent.BLOTTER_STOP );
				removeDeployItem( item );
			}
		}
		else {
			if( item.isRootItem() )
				notifyRootItem( item , BlotterEvent.BLOTTER_STOPCHILD );
			else
			if( item.isBuildItem() )
				notifyBuildItem( item , BlotterEvent.BLOTTER_STOPCHILD );
			else
			if( item.isReleaseItem() )
				notifyReleaseItem( item , BlotterEvent.BLOTTER_STOPCHILD );
			else
			if( item.isDeployItem() )
				notifyDeployItem( item , BlotterEvent.BLOTTER_STOPCHILD );
			else
				Common.exitUnexpected();
			item.stopChildAction( action , success );
		}
	}

	private synchronized ServerBlotterItem createRootItem( ActionInit action ) {
		ServerBlotterItem item = new ServerBlotterItem( this , BlotterType.BLOTTER_ROOT , action );
		
		item.createRootItem();
		listRoots.add( item );
		return( item );
	}

	private synchronized ServerBlotterItem createBuildItem( ActionPatch action ) {
		ServerBlotterItem item = new ServerBlotterItem( this , BlotterType.BLOTTER_BUILD , action );
		
		MetaSourceProject project = action.builder.project;
		item.createBuildItem( project.meta.name , project.NAME , action.builder.TAG );
		listBuilds.add( item );
		return( item );
	}

	private void notifyRootItem( ServerBlotterItem item , BlotterEvent event ) {
	}
	
	private void notifyBuildItem( ServerBlotterItem item , BlotterEvent event ) {
	}
	
	private void notifyReleaseItem( ServerBlotterItem item , BlotterEvent event ) {
	}
	
	private void notifyDeployItem( ServerBlotterItem item , BlotterEvent event ) {
	}
	
	private synchronized void removeRootItem( ServerBlotterItem item ) {
		listRoots.remove( item );
	}
	
	private synchronized void removeBuildItem( ServerBlotterItem item ) {
		listBuilds.remove( item );
	}
	
	private synchronized void removeReleaseItem( ServerBlotterItem item ) {
		listReleases.remove( item );
	}
	
	private synchronized void removeDeployItem( ServerBlotterItem item ) {
		listDeploy.remove( item );
	}
	
}
