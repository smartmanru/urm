package org.urm.server.action;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.urm.common.Common;
import org.urm.server.dist.ReleaseTarget;
import org.urm.server.dist.ReleaseTargetItem;
import org.urm.server.meta.MetaDistrBinaryItem;
import org.urm.server.meta.MetaDistrConfItem;
import org.urm.server.meta.MetaDistrDelivery;
import org.urm.server.meta.MetaEnvServer;
import org.urm.server.meta.MetaEnvServerNode;
import org.urm.server.meta.MetaSourceProject;
import org.urm.server.meta.MetaSourceProjectItem;
import org.urm.server.meta.Meta.VarCATEGORY;

public class ActionScopeTarget {

	public ActionScopeSet set;
	public VarCATEGORY CATEGORY; 
	public String NAME;
	public ReleaseTarget releaseTarget;
	public MetaSourceProject sourceProject;
	public MetaDistrDelivery dbDelivery;
	public MetaDistrConfItem confItem;
	public MetaDistrBinaryItem manualItem;
	public MetaEnvServer envServer;
	public boolean dbManualItems = false;
	
	public boolean itemFull = false;
	public boolean associated = false;
	public boolean specifiedExplicitly = false;
	
	List<ActionScopeTargetItem> items = new LinkedList<ActionScopeTargetItem>();
	
	private ActionScopeTarget( ActionScopeSet set ) {
		this.set = set;
		this.CATEGORY = set.CATEGORY;
	}
	
	public static ActionScopeTarget createDatabaseManualTarget( ActionScopeSet set , boolean all ) {
		ActionScopeTarget target = new ActionScopeTarget( set );
		target.NAME = "db.manual";
		target.dbManualItems = true;
		target.itemFull = all; 
		return( target );
	}

	public static ActionScopeTarget createSourceProjectTarget( ActionScopeSet set , MetaSourceProject sourceProject , boolean specifiedExplicitly ) {
		ActionScopeTarget target = new ActionScopeTarget( set );
		target.sourceProject = sourceProject;
		target.NAME = sourceProject.PROJECT;
		target.specifiedExplicitly = specifiedExplicitly;
		return( target );
	}
	
	public static ActionScopeTarget createReleaseSourceProjectTarget( ActionScopeSet set , ReleaseTarget releaseProject , boolean specifiedExplicitly ) {
		ActionScopeTarget target = new ActionScopeTarget( set );
		target.NAME = releaseProject.NAME;
		target.releaseTarget = releaseProject;
		
		target.sourceProject = releaseProject.sourceProject;
		target.dbDelivery = releaseProject.distDatabaseItem;
		target.confItem = releaseProject.distConfItem;
		target.manualItem = releaseProject.distManualItem;
		
		target.itemFull = ( releaseProject.sourceProject == null )? true : releaseProject.ALL;
		target.specifiedExplicitly = specifiedExplicitly;
		return( target );
	}

	public static ActionScopeTarget createDatabaseDeliveryTarget( ActionScopeSet set , MetaDistrDelivery delivery , boolean specifiedExplicitly , boolean all ) {
		ActionScopeTarget target = new ActionScopeTarget( set );
		target.dbDelivery = delivery;
		
		target.NAME = "db." + delivery.NAME;
		target.itemFull = all;
		target.specifiedExplicitly = specifiedExplicitly;
		return( target );
	}
	
	public static ActionScopeTarget createConfItemTarget( ActionScopeSet set , MetaDistrConfItem confItem , boolean specifiedExplicitly ) {
		ActionScopeTarget target = new ActionScopeTarget( set );

		target.confItem = confItem;
		target.NAME = confItem.KEY;
		target.itemFull = true;
		target.specifiedExplicitly = specifiedExplicitly;
		return( target );
	}
	
	public static ActionScopeTarget createManualDistItemTarget( ActionScopeSet set , MetaDistrBinaryItem manualItem , boolean specifiedExplicitly ) {
		ActionScopeTarget target = new ActionScopeTarget( set );

		target.manualItem = manualItem;
		target.NAME = manualItem.KEY;
		target.itemFull = true;
		target.specifiedExplicitly = specifiedExplicitly;
		return( target );
	}
	
	public static ActionScopeTarget createEnvServerTarget( ActionScopeSet set , MetaEnvServer envServer , boolean specifiedExplicitly ) {
		ActionScopeTarget target = new ActionScopeTarget( set );

		target.envServer = envServer;
		target.NAME = envServer.NAME;
		target.specifiedExplicitly = specifiedExplicitly;
		return( target );
	}
	
	public void setAssociated( ActionBase action ) throws Exception {
		associated = true;
	}
	
	public String getScopeInfo( ActionBase action ) throws Exception {
		if( sourceProject != null ) {
			String scope = sourceProject.PROJECT + ":";
			if( itemFull )
				scope += "all";
			else {
				String itemlist = "";
				for( ActionScopeTargetItem item : items )
					itemlist = Common.concat( itemlist , item.distItem.KEY , "," );
				scope += itemlist;
			}
			return( scope );
		}

		return( NAME );
	}

	public void addIndexItems( ActionBase action , String[] ITEMS ) throws Exception {
		for( String ITEM : ITEMS ) {
			ActionScopeTargetItem scopeItem = ActionScopeTargetItem.createScriptIndexTargetItem( ITEM );
			items.add( scopeItem );
		}
	}
	
	public void addProjectItems( ActionBase action , String[] ITEMS ) throws Exception {
		if( releaseTarget != null )
			addReleaseProjectItems( action , ITEMS );
		else
			addSourceProjectItems( action , ITEMS );
	}
	
	private void addSourceProjectItems( ActionBase action , String[] ITEMS ) throws Exception {
		if( ITEMS == null || ITEMS.length == 0 ) {
			itemFull = true;
			for( MetaSourceProjectItem item : sourceProject.getIitemList( action ) )
				addProjectItem( action , item , false );
			return;
		}
		
		for( String itemName : ITEMS ) {
			MetaDistrBinaryItem item = action.meta.distr.getBinaryItem( action , itemName );
			if( item.sourceItem == null )
				action.exit( "unknown distributive item=" + itemName );
			
			MetaSourceProjectItem projectItem = sourceProject.getItem( action , itemName );
			addProjectItem( action , projectItem , true );
		}
	}

	private void addReleaseProjectItems( ActionBase action , String[] ITEMS ) throws Exception {
		if( ITEMS == null || ITEMS.length == 0 ) {
			itemFull = true;
			for( ReleaseTargetItem item : releaseTarget.getItems( action ).values() )
				addItem( action , item , false );
			return;
		}
		
		Map<String,ReleaseTargetItem> releaseItems = releaseTarget.getItems( action );
		for( String itemName : ITEMS ) {
			MetaDistrBinaryItem item = action.meta.distr.getBinaryItem( action , itemName );
			if( item.sourceItem == null )
				action.exit( "unknown distributive item=" + itemName );
			
			ReleaseTargetItem releaseItem = releaseItems.get( itemName );
			if( releaseItem != null )
				addItem( action , releaseItem , true );
			else
				action.info( "ignore non-release item=" + itemName );
		}
	}
	
	public void addProjectItem( ActionBase action , MetaSourceProjectItem item , boolean specifiedExplicitly ) throws Exception {
		ActionScopeTargetItem scopeItem = ActionScopeTargetItem.createSourceProjectTargetItem( item , specifiedExplicitly );
		items.add( scopeItem );
	}
	
	public void addItem( ActionBase action , ReleaseTargetItem item , boolean specifiedExplicitly ) throws Exception {
		ActionScopeTargetItem scopeItem = ActionScopeTargetItem.createReleaseTargetItem( item , specifiedExplicitly );
		items.add( scopeItem );
	}
	
	public List<ActionScopeTargetItem> getItems( ActionBase action ) {
		return( items );
	}
	
	public String getProjectBuildVersion( ActionBase action ) throws Exception {
		String BUILDVERSION = "";
		if( !action.context.CTX_VERSION.isEmpty() )
			BUILDVERSION = action.context.CTX_VERSION;
		
		if( BUILDVERSION.isEmpty() && releaseTarget != null )
			BUILDVERSION = releaseTarget.BUILDVERSION;
		
		if( BUILDVERSION.isEmpty() )
			BUILDVERSION = action.meta.product.CONFIG_APPVERSION;
			
		if( BUILDVERSION.isEmpty() )
			action.exit( "buildByTag: BUILDVERSION not set" );
		
		return( BUILDVERSION );
	}
	
	public String getProjectBuildBranch( ActionBase action ) throws Exception {
		String BUILDBRANCH = "";
		if( !action.context.CTX_BRANCH.isEmpty() )
			BUILDBRANCH = action.context.CTX_BRANCH;
		
		if( BUILDBRANCH.isEmpty() && releaseTarget != null )
			BUILDBRANCH = releaseTarget.BUILDBRANCH;
		
		if( BUILDBRANCH.isEmpty() )
			BUILDBRANCH = action.meta.product.CONFIG_BRANCHNAME;
			
		if( BUILDBRANCH.isEmpty() )
			BUILDBRANCH = sourceProject.PROJECT + "-prod";
		
		return( BUILDBRANCH );
	}

	public String getProjectBuildTag( ActionBase action ) throws Exception {
		String BUILDTAG = "";
		if( !action.context.CTX_TAG.isEmpty() )
			BUILDTAG = action.context.CTX_TAG;
		
		if( BUILDTAG.isEmpty() && releaseTarget != null )
			BUILDTAG = releaseTarget.BUILDTAG;
		
		return( BUILDTAG );
	}

	public void addServerNodes( ActionBase action , List<MetaEnvServerNode> nodes ) throws Exception {
		if( nodes == null || nodes.isEmpty() ) {
			itemFull = true;
			for( MetaEnvServerNode node : envServer.getNodes( action ) )
				addServerNode( action , node , false );
			return;
		}
		
		for( MetaEnvServerNode node : nodes )
			addServerNode( action , node , true );
	}
	
	public ActionScopeTargetItem addServerNode( ActionBase action , MetaEnvServerNode node , boolean specifiedExplicitly ) throws Exception {
		if( !specifiedExplicitly ) {
			// ignore if offline
			if( node.OFFLINE ) {
				if( !action.context.CTX_ALL )
					return( null );
			}
				
			// check matches deploygroup or startgroup
			if( !action.context.CTX_DEPLOYGROUP.isEmpty() ) {
				if( node.DEPLOYGROUP.isEmpty() ) {
					if( !action.context.CTX_DEPLOYGROUP.equals( "default" ) )
						return( null );
				}
				else {
					if( !action.context.CTX_DEPLOYGROUP.equals( node.DEPLOYGROUP ) ) 
					return( null );
				}
			}
		}
		
		ActionScopeTargetItem scopeItem = ActionScopeTargetItem.createEnvServerNodeTargetItem( node , specifiedExplicitly );
		items.add( scopeItem );
		return( scopeItem );
	}
	
}
