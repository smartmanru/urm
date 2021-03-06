package org.urm.action;

import java.util.LinkedList;
import java.util.List;

import org.urm.common.Common;
import org.urm.db.core.DBEnums.*;
import org.urm.engine.dist.ReleaseBuildScopeProject;
import org.urm.engine.dist.ReleaseBuildScopeProjectItem;
import org.urm.engine.dist.ReleaseDistScopeDelivery;
import org.urm.engine.dist.ReleaseDistScopeDeliveryItem;
import org.urm.meta.env.MetaEnvDeployGroup;
import org.urm.meta.env.MetaEnvServer;
import org.urm.meta.env.MetaEnvServerNode;
import org.urm.meta.product.Meta;
import org.urm.meta.product.MetaDatabaseSchema;
import org.urm.meta.product.MetaDistr;
import org.urm.meta.product.MetaDistrBinaryItem;
import org.urm.meta.product.MetaDistrConfItem;
import org.urm.meta.product.MetaDistrDelivery;
import org.urm.meta.product.MetaProductBuildSettings;
import org.urm.meta.product.MetaProductDoc;
import org.urm.meta.product.MetaSourceProject;
import org.urm.meta.product.MetaSourceProjectItem;

public class ActionScopeTarget {

	public ActionScopeSet set;
	public Meta meta;
	
	public DBEnumScopeCategoryType CATEGORY; 
	public String NAME;
	
	public ReleaseBuildScopeProject releaseBuildScopeProject;
	public ReleaseDistScopeDelivery releaseDistScopeDelivery;
	public ReleaseDistScopeDeliveryItem releaseDistScopeDeliveryItem;
	
	public MetaSourceProject sourceProject;
	public MetaDistrDelivery delivery;
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
	
	public static ActionScopeTarget createReleaseSourceProjectTarget( ActionScopeSet set , ReleaseBuildScopeProject releaseProject , boolean specifiedExplicitly ) {
		ActionScopeTarget target = new ActionScopeTarget( set );
		target.NAME = releaseProject.project.NAME;
		target.releaseBuildScopeProject = releaseProject;
		target.sourceProject = releaseProject.project;
		target.itemFull = releaseProject.all;
		target.specifiedExplicitly = specifiedExplicitly;
		return( target );
	}

	public static ActionScopeTarget createReleaseDeliveryTarget( ActionScopeSet set , ReleaseDistScopeDelivery releaseTarget , boolean specifiedExplicitly ) {
		ActionScopeTarget target = new ActionScopeTarget( set );
		target.NAME = releaseTarget.distDelivery.NAME;
		target.releaseDistScopeDelivery = releaseTarget;
		target.delivery = releaseTarget.distDelivery;
		target.itemFull = releaseTarget.all;
		target.specifiedExplicitly = specifiedExplicitly;
		return( target );
	}
	
	public static ActionScopeTarget createReleaseBinaryTarget( ActionScopeSet set , ReleaseDistScopeDeliveryItem releaseItem , boolean specifiedExplicitly ) {
		ActionScopeTarget target = new ActionScopeTarget( set );
		target.NAME = releaseItem.binary.NAME;
		target.releaseDistScopeDeliveryItem = releaseItem;
		target.delivery = releaseItem.distDelivery;
		target.itemFull = true;
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
	
	public static ActionScopeTarget createDeliveryDatabaseTarget( ActionScopeSet set , MetaDistrDelivery delivery , boolean specifiedExplicitly , boolean all ) {
		ActionScopeTarget target = new ActionScopeTarget( set );
		target.delivery = delivery;
		target.NAME = "db." + delivery.NAME;
		target.itemFull = all;
		target.specifiedExplicitly = specifiedExplicitly;
		return( target );
	}
	
	public static ActionScopeTarget createDeliveryDocTarget( ActionScopeSet set , MetaDistrDelivery delivery , boolean specifiedExplicitly , boolean all ) {
		ActionScopeTarget target = new ActionScopeTarget( set );
		target.delivery = delivery;
		target.NAME = "doc." + delivery.NAME;
		target.itemFull = all;
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
			CATEGORY == DBEnumScopeCategoryType.CONFIG ||
			CATEGORY == DBEnumScopeCategoryType.DERIVED ||
			CATEGORY == DBEnumScopeCategoryType.MANUAL )
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
		if( CATEGORY == DBEnumScopeCategoryType.CONFIG )
			return( createProductConfItemTarget( setNew , confItem , specifiedExplicitly ) );
		if( CATEGORY == DBEnumScopeCategoryType.DERIVED )
			return( createProductDerivedDistItemTarget( setNew , derivedItem , specifiedExplicitly ) );
		if( CATEGORY == DBEnumScopeCategoryType.MANUAL )
			return( createProductManualDistItemTarget( setNew , manualItem , specifiedExplicitly ) );
		if( CATEGORY.isSource() ) {
			if( releaseBuildScopeProject != null )
				return( createReleaseSourceProjectTarget( setNew , releaseBuildScopeProject , specifiedExplicitly ) );
			return( createProductSourceProjectTarget( setNew , sourceProject , specifiedExplicitly ) );
		}
		if( CATEGORY == DBEnumScopeCategoryType.ENV )
			return( createEnvServerTarget( setNew , envServer , specifiedExplicitly ) );
		if( CATEGORY == DBEnumScopeCategoryType.DB )
			return( createDeliveryDatabaseTarget( setNew , delivery , specifiedExplicitly , itemFull ) );
		if( CATEGORY == DBEnumScopeCategoryType.DOC )
			return( createDeliveryDocTarget( setNew , delivery , specifiedExplicitly , itemFull ) );
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
		if( releaseBuildScopeProject != null )
			addReleaseProjectItems( action , ITEMS );
		else
			addProjectItemsInternal( action , ITEMS );
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

	private void addReleaseProjectItems( ActionBase action , String[] ITEMS ) throws Exception {
		if( ITEMS == null || ITEMS.length == 0 ) {
			itemFull = true;
			for( ReleaseBuildScopeProjectItem item : releaseBuildScopeProject.getItems() )
				addItem( action , item , false );
			return;
		}
		
		MetaSourceProject project = releaseBuildScopeProject.project;
		for( String itemName : ITEMS ) {
			MetaSourceProjectItem item = project.getItem( itemName );
			
			ReleaseBuildScopeProjectItem releaseItem = releaseBuildScopeProject.findItem( item );
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
	
	public void addDatabaseSchemes( ActionBase action , String[] ITEMS ) throws Exception {
		if( releaseDistScopeDelivery != null )
			addReleaseDatabaseSchemes( action , ITEMS );
		else
			addDatabaseSchemesInternal( action , ITEMS );
	}
	
	private void addDatabaseSchemesInternal( ActionBase action , String[] ITEMS ) throws Exception {
		if( ITEMS == null || ITEMS.length == 0 ) {
			itemFull = true;
			for( MetaDatabaseSchema item : delivery.getDatabaseSchemes() )
				addDatabaseSchema( action , item , false );
			return;
		}
		
		for( String itemName : ITEMS ) {
			MetaDatabaseSchema item = delivery.getSchema( itemName );
			addDatabaseSchema( action , item , true );
		}
	}

	private void addReleaseDatabaseSchemes( ActionBase action , String[] ITEMS ) throws Exception {
		if( ITEMS == null || ITEMS.length == 0 ) {
			itemFull = true;
			for( ReleaseDistScopeDeliveryItem item : releaseDistScopeDelivery.getItems() )
				addItem( action , item , false );
			return;
		}
		
		MetaDistrDelivery delivery = releaseDistScopeDelivery.distDelivery;
		for( String itemName : ITEMS ) {
			MetaDatabaseSchema schema = delivery.getSchema( itemName );
			
			ReleaseDistScopeDeliveryItem releaseItem = releaseDistScopeDelivery.findSchema( schema );
			if( releaseItem != null )
				addItem( action , releaseItem , true );
			else
				action.debug( "scope: ignore non-release item=" + itemName );
		}
	}
	
	public void addDatabaseSchema( ActionBase action , MetaDatabaseSchema schema , boolean specifiedExplicitly ) throws Exception {
		ActionScopeTargetItem scopeItem = ActionScopeTargetItem.createDeliverySchemaTargetItem( this , schema , specifiedExplicitly );
		items.add( scopeItem );
	}
	
	public void addDocs( ActionBase action , String[] ITEMS ) throws Exception {
		if( releaseDistScopeDelivery != null )
			addReleaseDocs( action , ITEMS );
		else
			addDocsInternal( action , ITEMS );
	}
	
	private void addDocsInternal( ActionBase action , String[] ITEMS ) throws Exception {
		if( ITEMS == null || ITEMS.length == 0 ) {
			itemFull = true;
			for( MetaProductDoc item : delivery.getDocs() )
				addDoc( action , item , false );
			return;
		}
		
		for( String itemName : ITEMS ) {
			MetaProductDoc item = delivery.getDoc( itemName );
			addDoc( action , item , true );
		}
	}

	private void addReleaseDocs( ActionBase action , String[] ITEMS ) throws Exception {
		if( ITEMS == null || ITEMS.length == 0 ) {
			itemFull = true;
			for( ReleaseDistScopeDeliveryItem item : releaseDistScopeDelivery.getItems() )
				addItem( action , item , false );
			return;
		}
		
		MetaDistrDelivery delivery = releaseDistScopeDelivery.distDelivery;
		for( String itemName : ITEMS ) {
			MetaProductDoc doc = delivery.getDoc( itemName );
			
			ReleaseDistScopeDeliveryItem releaseItem = releaseDistScopeDelivery.findDoc( doc );
			if( releaseItem != null )
				addItem( action , releaseItem , true );
			else
				action.debug( "scope: ignore non-release item=" + itemName );
		}
	}
	
	public void addDoc( ActionBase action , MetaProductDoc doc , boolean specifiedExplicitly ) throws Exception {
		ActionScopeTargetItem scopeItem = ActionScopeTargetItem.createDeliveryDocTargetItem( this , doc , specifiedExplicitly );
		items.add( scopeItem );
	}
	
	public void addItem( ActionBase action , ReleaseBuildScopeProjectItem item , boolean specifiedExplicitly ) throws Exception {
		ActionScopeTargetItem scopeItem = ActionScopeTargetItem.createReleaseTargetItem( this , item , specifiedExplicitly );
		items.add( scopeItem );
	}
	
	public void addItem( ActionBase action , ReleaseDistScopeDeliveryItem item , boolean specifiedExplicitly ) throws Exception {
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
		
		if( BUILDVERSION.isEmpty() && releaseBuildScopeProject != null ) {
			if( releaseBuildScopeProject.scopeProjectTarget != null )
				BUILDVERSION = releaseBuildScopeProject.scopeProjectTarget.BUILD_VERSION;
			
			if( BUILDVERSION.isEmpty() )
				BUILDVERSION = set.getSetBuildVersion();
		}
		
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
		
		if( BUILDBRANCH.isEmpty() && releaseBuildScopeProject != null ) {
			if( releaseBuildScopeProject.scopeProjectTarget != null )
				BUILDBRANCH = releaseBuildScopeProject.scopeProjectTarget.BUILD_BRANCH;
			
			if( BUILDBRANCH.isEmpty() )
				BUILDBRANCH = set.getSetBuildBranch();
		}
		
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
		
		if( BUILDTAG.isEmpty() && releaseBuildScopeProject != null ) {
			if( releaseBuildScopeProject.scopeProjectTarget != null )
				BUILDTAG = releaseBuildScopeProject.scopeProjectTarget.BUILD_TAG;
			
			if( BUILDTAG.isEmpty() )
				BUILDTAG = set.getSetBuildTag();
		}
		
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
				MetaEnvDeployGroup dg = node.server.sg.env.getDeployGroup( action.context.CTX_DEPLOYGROUP );
				if( node.DEPLOYGROUP == null )
					return( null );
				
				if( dg.ID != node.DEPLOYGROUP ) 
					return( null );
			}
		}
		
		ActionScopeTargetItem scopeItem = ActionScopeTargetItem.createEnvServerNodeTargetItem( this , node , specifiedExplicitly );
		items.add( scopeItem );
		return( scopeItem );
	}

	public boolean isBuildableProject() {
		if( CATEGORY == DBEnumScopeCategoryType.PROJECT && sourceProject.isBuildable() )
			return( true );
		return( false );
	}
	
	public boolean isPrebuiltProject() {
		if( CATEGORY == DBEnumScopeCategoryType.PROJECT && !sourceProject.isBuildable() )
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
