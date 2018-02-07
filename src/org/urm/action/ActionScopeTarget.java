package org.urm.action;

import java.util.LinkedList;
import java.util.List;

import org.urm.common.Common;
import org.urm.engine.dist.ReleaseTarget;
import org.urm.engine.dist.ReleaseTargetItem;
import org.urm.meta.env.MetaEnvServer;
import org.urm.meta.env.MetaEnvServerNode;
import org.urm.meta.product.Meta;
import org.urm.meta.product.MetaDatabase;
import org.urm.meta.product.MetaDatabaseSchema;
import org.urm.meta.product.MetaDistr;
import org.urm.meta.product.MetaDistrBinaryItem;
import org.urm.meta.product.MetaDistrConfItem;
import org.urm.meta.product.MetaDistrDelivery;
import org.urm.meta.product.MetaProductBuildSettings;
import org.urm.meta.product.MetaSourceProject;
import org.urm.meta.product.MetaSourceProjectItem;
import org.urm.meta.Types;
import org.urm.meta.Types.*;

public class ActionScopeTarget {

	public ActionScopeSet set;
	public Meta meta;
	
	public EnumScopeCategory CATEGORY; 
	public String NAME;
	public ReleaseTarget releaseTarget;
	public MetaSourceProject sourceProject;
	public MetaDistrDelivery dbDelivery;
	
	public MetaDistrConfItem confItem;
	public MetaDistrBinaryItem manualItem;
	public MetaDistrBinaryItem derivedItem;
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
	
	public static ActionScopeTarget createReleaseDatabaseManualTarget( ActionScopeSet set , boolean all ) {
		ActionScopeTarget target = new ActionScopeTarget( set );
		target.NAME = "db.manual";
		target.dbManualItems = true;
		target.itemFull = all; 
		return( target );
	}

	public static ActionScopeTarget createProductSourceProjectTarget( ActionScopeSet set , MetaSourceProject sourceProject , boolean specifiedExplicitly ) {
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
	
	public static ActionScopeTarget createProductConfItemTarget( ActionScopeSet set , MetaDistrConfItem confItem , boolean specifiedExplicitly ) {
		ActionScopeTarget target = new ActionScopeTarget( set );

		target.confItem = confItem;
		target.NAME = confItem.NAME;
		target.itemFull = true;
		target.specifiedExplicitly = specifiedExplicitly;
		return( target );
	}
	
	public static ActionScopeTarget createProductManualDistItemTarget( ActionScopeSet set , MetaDistrBinaryItem manualItem , boolean specifiedExplicitly ) {
		ActionScopeTarget target = new ActionScopeTarget( set );

		target.manualItem = manualItem;
		target.NAME = manualItem.NAME;
		target.itemFull = true;
		target.specifiedExplicitly = specifiedExplicitly;
		return( target );
	}
	
	public static ActionScopeTarget createProductDerivedDistItemTarget( ActionScopeSet set , MetaDistrBinaryItem derivedItem , boolean specifiedExplicitly ) {
		ActionScopeTarget target = new ActionScopeTarget( set );

		target.derivedItem = derivedItem;
		target.NAME = derivedItem.NAME;
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
	
	public boolean isLeafTarget() {
		if( dbManualItems ||
			CATEGORY == EnumScopeCategory.CONFIG ||
			CATEGORY == EnumScopeCategory.DERIVED ||
			CATEGORY == EnumScopeCategory.MANUAL )
			return( true );
		return( false );
	}

	public boolean isEmpty() {
		if( isLeafTarget() )
			return( false );
		return( items.isEmpty() );
	}
	
	public ActionScopeTarget copy( ActionScopeSet setNew ) {
		if( dbManualItems )
			return( createReleaseDatabaseManualTarget( setNew , itemFull ) );
		if( CATEGORY == EnumScopeCategory.CONFIG )
			return( createProductConfItemTarget( setNew , confItem , specifiedExplicitly ) );
		if( CATEGORY == EnumScopeCategory.DERIVED )
			return( createProductDerivedDistItemTarget( setNew , derivedItem , specifiedExplicitly ) );
		if( CATEGORY == EnumScopeCategory.MANUAL )
			return( createProductManualDistItemTarget( setNew , manualItem , specifiedExplicitly ) );
		if( Types.isSourceCategory( CATEGORY ) ) {
			if( releaseTarget != null )
				return( createReleaseSourceProjectTarget( setNew , releaseTarget , specifiedExplicitly ) );
			return( createProductSourceProjectTarget( setNew , sourceProject , specifiedExplicitly ) );
		}
		if( CATEGORY == EnumScopeCategory.ENV )
			return( createEnvServerTarget( setNew , envServer , specifiedExplicitly ) );
		if( CATEGORY == EnumScopeCategory.DB )
			return( createDatabaseDeliveryTarget( setNew , dbDelivery , specifiedExplicitly , itemFull ) );
		return( null );
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
					itemlist = Common.concat( itemlist , item.distItem.NAME , "," );
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
			addProjectItemsInternal( action , ITEMS );
	}
	
	public void addDatabaseSchemes( ActionBase action , String[] ITEMS ) throws Exception {
		if( releaseTarget != null )
			addReleaseDatabaseSchemes( action , ITEMS );
		else
			addDatabaseSchemesInternal( action , ITEMS );
	}
	
	private void addProjectItemsInternal( ActionBase action , String[] ITEMS ) throws Exception {
		if( ITEMS == null || ITEMS.length == 0 ) {
			itemFull = true;
			for( MetaSourceProjectItem item : sourceProject.getItems() )
				addProjectItem( action , item , false );
			return;
		}
		
		MetaDistr distr = meta.getDistr();
		for( String itemName : ITEMS ) {
			MetaDistrBinaryItem item = distr.getBinaryItem( itemName );
			if( item.sourceProjectItem == null )
				action.exit1( _Error.UnknownDistributiveItem1 , "unknown distributive item=" + itemName , itemName );
			
			MetaSourceProjectItem projectItem = sourceProject.getItem( itemName );
			addProjectItem( action , projectItem , true );
		}
	}

	private void addDatabaseSchemesInternal( ActionBase action , String[] ITEMS ) throws Exception {
		if( ITEMS == null || ITEMS.length == 0 ) {
			itemFull = true;
			for( MetaDatabaseSchema item : dbDelivery.getDatabaseSchemes() )
				addDatabaseSchema( action , item , false );
			return;
		}
		
		MetaDatabase db = dbDelivery.db;
		for( String itemName : ITEMS ) {
			MetaDatabaseSchema item = db.getSchema( itemName );
			addDatabaseSchema( action , item , true );
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
			MetaSourceProjectItem item = project.getItem( itemName );
			
			ReleaseTargetItem releaseItem = releaseTarget.findProjectItem( item );
			if( releaseItem != null )
				addItem( action , releaseItem , true );
			else
				action.debug( "scope: ignore non-release item=" + itemName );
		}
	}
	
	private void addReleaseDatabaseSchemes( ActionBase action , String[] ITEMS ) throws Exception {
		if( ITEMS == null || ITEMS.length == 0 ) {
			itemFull = true;
			for( ReleaseTargetItem item : releaseTarget.getItems() )
				addItem( action , item , false );
			return;
		}
		
		MetaDistrDelivery delivery = releaseTarget.distDatabaseDelivery;
		for( String itemName : ITEMS ) {
			MetaDatabaseSchema item = delivery.getSchema( itemName );
			
			ReleaseTargetItem releaseItem = releaseTarget.findDatabaseSchema( item );
			if( releaseItem != null )
				addItem( action , releaseItem , true );
			else
				action.debug( "scope: ignore non-release item=" + itemName );
		}
	}
	
	public void addProjectItem( ActionBase action , MetaSourceProjectItem item , boolean specifiedExplicitly ) throws Exception {
		ActionScopeTargetItem scopeItem = ActionScopeTargetItem.createSourceProjectTargetItem( this , item , item.distItem , specifiedExplicitly );
		items.add( scopeItem );
	}
	
	public void addDatabaseSchema( ActionBase action , MetaDatabaseSchema schema , boolean specifiedExplicitly ) throws Exception {
		ActionScopeTargetItem scopeItem = ActionScopeTargetItem.createDeliverySchemaTargetItem( this , schema , specifiedExplicitly );
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

	public void addServerNodes( ActionBase action , MetaEnvServerNode[] nodes ) throws Exception {
		if( nodes == null || nodes.length == 0 ) {
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
		if( CATEGORY == EnumScopeCategory.PROJECT && sourceProject.isBuildable() )
			return( true );
		return( false );
	}
	
	public boolean isPrebuiltProject() {
		if( CATEGORY == EnumScopeCategory.PROJECT && !sourceProject.isBuildable() )
			return( true );
		return( false );
	}

	public void createMinusTarget( ActionBase action , ActionScopeTarget targetAdd , ActionScopeTarget targetRemove ) throws Exception {
		for( ActionScopeTargetItem item : targetAdd.items )
			createMinusTargetItem( action , item , targetRemove );
	}	

	public void createMinusTargetItem( ActionBase action , ActionScopeTargetItem itemAdd , ActionScopeTarget targetRemove ) throws Exception {
		ActionScopeTargetItem itemRemove = null;
		if( targetRemove != null )
			itemRemove = targetRemove.findSimilarItem( action , itemAdd );
		
		if( itemRemove != null )
			return;
		
		ActionScopeTargetItem itemNew = itemAdd.copy( this );
		items.add( itemNew );
	}

	public ActionScopeTargetItem findSimilarItem( ActionBase action , ActionScopeTargetItem sample ) throws Exception {
		for( ActionScopeTargetItem item : items ) {
			if( item.isSimilarItem( action , sample ) )
				return( item );
		}
		return( null );
	}
	
}
