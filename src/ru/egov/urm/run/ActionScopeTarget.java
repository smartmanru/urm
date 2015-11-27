package ru.egov.urm.run;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import ru.egov.urm.Common;
import ru.egov.urm.meta.MetaDistrBinaryItem;
import ru.egov.urm.meta.MetaDistrConfItem;
import ru.egov.urm.meta.MetaDistrDelivery;
import ru.egov.urm.meta.MetaEnvServer;
import ru.egov.urm.meta.MetaEnvServerNode;
import ru.egov.urm.meta.MetaSourceProject;
import ru.egov.urm.meta.MetaSourceProjectItem;
import ru.egov.urm.meta.MetaReleaseTargetItem;
import ru.egov.urm.meta.MetaReleaseTarget;
import ru.egov.urm.meta.Metadata.VarCATEGORY;

public class ActionScopeTarget {

	public ActionScopeSet set;
	public VarCATEGORY CATEGORY; 
	public String NAME;
	public MetaReleaseTarget releaseTarget;
	public MetaSourceProject sourceProject;
	public MetaDistrDelivery dbDelivery;
	public MetaDistrConfItem confItem;
	public MetaDistrBinaryItem manualItem;
	public MetaEnvServer envServer;
	public boolean itemFull = false;
	public boolean associated = false;
	public boolean specifiedExplicitly;
	
	List<ActionScopeTargetItem> items = new LinkedList<ActionScopeTargetItem>();
	
	public ActionScopeTarget( ActionScopeSet set , MetaSourceProject sourceProject , boolean specifiedExplicitly ) {
		this.set = set;
		this.CATEGORY = set.CATEGORY;
		this.sourceProject = sourceProject;
		this.NAME = sourceProject.PROJECT;
		this.specifiedExplicitly = specifiedExplicitly;
	}
	
	public ActionScopeTarget( ActionScopeSet set , MetaReleaseTarget releaseProject , boolean specifiedExplicitly ) {
		this.set = set;
		this.CATEGORY = set.CATEGORY;
		this.NAME = releaseProject.NAME;
		this.releaseTarget = releaseProject;
		
		this.sourceProject = releaseProject.sourceProject;
		this.dbDelivery = releaseProject.distDatabaseItem;
		this.confItem = releaseProject.distConfItem;
		this.manualItem = releaseProject.distManualItem;
		
		this.itemFull = ( releaseProject.sourceProject == null )? true : releaseProject.ALL;
		this.specifiedExplicitly = specifiedExplicitly;
	}

	public ActionScopeTarget( ActionScopeSet set , MetaDistrDelivery delivery , boolean specifiedExplicitly ) {
		this.set = set;
		this.CATEGORY = set.CATEGORY;
		this.dbDelivery = delivery;
		
		this.NAME = delivery.NAME;
		this.itemFull = true;
		this.specifiedExplicitly = specifiedExplicitly;
	}
	
	public ActionScopeTarget( ActionScopeSet set , MetaDistrConfItem confItem , boolean specifiedExplicitly ) {
		this.set = set;
		this.CATEGORY = set.CATEGORY;

		this.confItem = confItem;
		this.NAME = confItem.KEY;
		this.itemFull = true;
		this.specifiedExplicitly = specifiedExplicitly;
	}
	
	public ActionScopeTarget( ActionScopeSet set , MetaDistrBinaryItem manualItem , boolean specifiedExplicitly ) {
		this.set = set;
		this.CATEGORY = set.CATEGORY;

		this.manualItem = manualItem;
		this.NAME = manualItem.KEY;
		this.itemFull = true;
		this.specifiedExplicitly = specifiedExplicitly;
	}
	
	public ActionScopeTarget( ActionScopeSet set , MetaEnvServer envServer , boolean specifiedExplicitly ) {
		this.set = set;
		this.CATEGORY = set.CATEGORY;

		this.envServer = envServer;
		this.NAME = envServer.NAME;
		this.specifiedExplicitly = specifiedExplicitly;
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
			for( MetaReleaseTargetItem item : releaseTarget.getItems( action ).values() )
				addItem( action , item , false );
			return;
		}
		
		Map<String,MetaReleaseTargetItem> releaseItems = releaseTarget.getItems( action );
		for( String itemName : ITEMS ) {
			MetaDistrBinaryItem item = action.meta.distr.getBinaryItem( action , itemName );
			if( item.sourceItem == null )
				action.exit( "unknown distributive item=" + itemName );
			
			MetaReleaseTargetItem releaseItem = releaseItems.get( itemName );
			if( releaseItem != null )
				addItem( action , releaseItem , true );
			else
				action.log( "ignore non-release item=" + itemName );
		}
	}
	
	public void addProjectItem( ActionBase action , MetaSourceProjectItem item , boolean specifiedExplicitly ) throws Exception {
		ActionScopeTargetItem scopeItem = new ActionScopeTargetItem( item , specifiedExplicitly );
		items.add( scopeItem );
	}
	
	public void addItem( ActionBase action , MetaReleaseTargetItem item , boolean specifiedExplicitly ) throws Exception {
		ActionScopeTargetItem scopeItem = new ActionScopeTargetItem( item , specifiedExplicitly );
		items.add( scopeItem );
	}
	
	public List<ActionScopeTargetItem> getItems( ActionBase action ) {
		return( items );
	}
	
	public String getProjectBuildVersion( ActionBase action ) throws Exception {
		String BUILDVERSION = "";
		if( !action.options.OPT_VERSION.isEmpty() )
			BUILDVERSION = action.options.OPT_VERSION;
		
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
		if( !action.options.OPT_BRANCH.isEmpty() )
			BUILDBRANCH = action.options.OPT_BRANCH;
		
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
		if( !action.options.OPT_TAG.isEmpty() )
			BUILDTAG = action.options.OPT_TAG;
		
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
				if( !action.options.OPT_ALL )
					return( null );
			}
				
			// check matches deploygroup or startgroup
			if( !action.options.OPT_DEPLOYGROUP.isEmpty() ) {
				if( node.DEPLOYGROUP.isEmpty() ) {
					if( !action.options.OPT_DEPLOYGROUP.equals( "default" ) )
						return( null );
				}
				else {
					if( !action.options.OPT_DEPLOYGROUP.equals( node.DEPLOYGROUP ) ) 
					return( null );
				}
			}
		}
		
		ActionScopeTargetItem scopeItem = new ActionScopeTargetItem( node , specifiedExplicitly );
		items.add( scopeItem );
		return( scopeItem );
	}
	
}
