package org.urm.action;

import java.util.LinkedList;
import java.util.List;

import org.urm.common.Common;
import org.urm.engine.dist.ReleaseTarget;
import org.urm.engine.dist.ReleaseTargetItem;
import org.urm.meta.product.Meta;
import org.urm.meta.product.MetaDistr;
import org.urm.meta.product.MetaDistrBinaryItem;
import org.urm.meta.product.MetaDistrConfItem;
import org.urm.meta.product.MetaDistrDelivery;
import org.urm.meta.product.MetaEnvServer;
import org.urm.meta.product.MetaEnvServerNode;
import org.urm.meta.product.MetaProductBuildSettings;
import org.urm.meta.product.MetaSourceProject;
import org.urm.meta.product.MetaSourceProjectItem;
import org.urm.meta.Types.*;

public class ActionScopeTarget {

	public ActionScopeSet set;
	public Meta meta;
	
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
		this.meta = set.meta;
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
		target.NAME = sourceProject.NAME;
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
			String scope = sourceProject.NAME + ":";
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
			ActionScopeTargetItem scopeItem = ActionScopeTargetItem.createScriptIndexTargetItem( this , ITEM );
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
			for( MetaSourceProjectItem item : sourceProject.getItems() )
				addProjectItem( action , item , false );
			return;
		}
		
		MetaDistr distr = meta.getDistr( action );
		for( String itemName : ITEMS ) {
			MetaDistrBinaryItem item = distr.getBinaryItem( action , itemName );
			if( item.sourceProjectItem == null )
				action.exit1( _Error.UnknownDistributiveItem1 , "unknown distributive item=" + itemName , itemName );
			
			MetaSourceProjectItem projectItem = sourceProject.getItem( action , itemName );
			addProjectItem( action , projectItem , true );
		}
	}

	private void addReleaseProjectItems( ActionBase action , String[] ITEMS ) throws Exception {
		if( ITEMS == null || ITEMS.length == 0 ) {
			itemFull = true;
			for( ReleaseTargetItem item : releaseTarget.getItems() )
				addItem( action , item , false );
			return;
		}
		
		MetaSourceProject project = releaseTarget.sourceProject;
		for( String itemName : ITEMS ) {
			MetaSourceProjectItem item = project.getItem( action , itemName );
			
			ReleaseTargetItem releaseItem = releaseTarget.findProjectItem( item );
			if( releaseItem != null )
				addItem( action , releaseItem , true );
			else
				action.info( "scope: ignore non-release item=" + itemName );
		}
	}
	
	public void addProjectItem( ActionBase action , MetaSourceProjectItem item , boolean specifiedExplicitly ) throws Exception {
		ActionScopeTargetItem scopeItem = ActionScopeTargetItem.createSourceProjectTargetItem( this , item , item.distItem , specifiedExplicitly );
		items.add( scopeItem );
	}
	
	public void addItem( ActionBase action , ReleaseTargetItem item , boolean specifiedExplicitly ) throws Exception {
		ActionScopeTargetItem scopeItem = ActionScopeTargetItem.createReleaseTargetItem( this , item , specifiedExplicitly );
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
		
		if( BUILDVERSION.isEmpty() ) {
			MetaProductBuildSettings build = action.getBuildSettings( meta );
			BUILDVERSION = build.CONFIG_APPVERSION;
		}
			
		if( BUILDVERSION.isEmpty() )
			action.exit0( _Error.BuildVersionNotSet0 , "buildByTag: BUILDVERSION not set" );
		
		return( BUILDVERSION );
	}
	
	public String getProjectBuildBranch( ActionBase action ) throws Exception {
		String BUILDBRANCH = "";
		if( !action.context.CTX_BRANCH.isEmpty() )
			BUILDBRANCH = action.context.CTX_BRANCH;
		
		if( BUILDBRANCH.isEmpty() && releaseTarget != null )
			BUILDBRANCH = releaseTarget.BUILDBRANCH;
		
		if( BUILDBRANCH.isEmpty() ) {
			MetaProductBuildSettings build = action.getBuildSettings( meta );
			BUILDBRANCH = build.CONFIG_BRANCHNAME;
		}
			
		if( BUILDBRANCH.isEmpty() )
			BUILDBRANCH = sourceProject.NAME + "-prod";
		
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
			for( MetaEnvServerNode node : envServer.getNodes() )
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
				if( !action.context.CTX_ALL ) {
					action.trace( "scope: ignore offline node=" + node.POS );
					return( null );
				}
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
		
		ActionScopeTargetItem scopeItem = ActionScopeTargetItem.createEnvServerNodeTargetItem( this , node , specifiedExplicitly );
		items.add( scopeItem );
		return( scopeItem );
	}

	public boolean isBuildableProject() {
		if( CATEGORY == VarCATEGORY.PROJECT && sourceProject.isBuildable() )
			return( true );
		return( false );
	}
	
	public boolean isPrebuiltProject() {
		if( CATEGORY == VarCATEGORY.PROJECT && !sourceProject.isBuildable() )
			return( true );
		return( false );
	}
	
}
