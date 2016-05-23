package ru.egov.urm.meta;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import ru.egov.urm.action.ActionBase;

public class MetaReleaseDelivery {

	Metadata meta;
	public MetaRelease release;
	public MetaDistrDelivery distDelivery;
	
	private Map<String,MetaReleaseTargetItem> projectItems;
	private Map<String,MetaReleaseTarget> confItems;
	private Map<String,MetaReleaseTarget> manualItems;
	private MetaReleaseTarget dbItem;
	
	public MetaReleaseDelivery( Metadata meta , MetaRelease release , MetaDistrDelivery distDelivery ) {
		this.meta = meta; 
		this.release = release;
		this.distDelivery = distDelivery;
		
		projectItems = new HashMap<String,MetaReleaseTargetItem>();
		confItems = new HashMap<String,MetaReleaseTarget>();
		manualItems = new HashMap<String,MetaReleaseTarget>();
		dbItem = null;
	}

	public MetaReleaseDelivery copy( ActionBase action , MetaRelease nr ) throws Exception {
		MetaReleaseDelivery nx = new MetaReleaseDelivery( meta , nr , distDelivery );
		
		for( Entry<String,MetaReleaseTargetItem> entry : projectItems.entrySet() ) {
			MetaReleaseTargetItem src = entry.getValue();
			MetaReleaseSet srcSet = src.target.set;
			MetaReleaseSet dstSet = nr.getSourceSet( action , srcSet.NAME );
			MetaReleaseTarget dstTarget = dstSet.getTarget( action , src.NAME );
			MetaReleaseTargetItem dst = dstTarget.getItem( action , src.NAME );
			nx.projectItems.put( entry.getKey() , dst );
		}
		
		for( Entry<String,MetaReleaseTarget> entry : confItems.entrySet() ) {
			MetaReleaseTarget src = entry.getValue();
			MetaReleaseSet dstSet = nr.getCategorySet( action , src.set.CATEGORY );
			MetaReleaseTarget dst = dstSet.getTarget( action , src.NAME );
			nx.confItems.put( entry.getKey() , dst );
		}
		
		for( Entry<String,MetaReleaseTarget> entry : manualItems.entrySet() ) {
			MetaReleaseTarget src = entry.getValue();
			MetaReleaseSet dstSet = nr.getCategorySet( action , src.set.CATEGORY );
			MetaReleaseTarget dst = dstSet.getTarget( action , src.NAME );
			nx.manualItems.put( entry.getKey() , dst );
		}
		
		nx.dbItem = dbItem;
		
		return( nx );
	}
	
	public void addTargetItem( ActionBase action , MetaReleaseTargetItem item ) throws Exception {
		action.debug( "add delivery binary item: " + distDelivery.NAME + "::" + item.distItem.KEY );
		projectItems.put( item.distItem.KEY , item );
	}

	public void removeTargetItem( ActionBase action , MetaReleaseTargetItem item ) throws Exception {
		action.debug( "remove delivery binary item: " + distDelivery.NAME + "::" + item.distItem.KEY );
		projectItems.remove( item.sourceItem.ITEMNAME );
	}

	public void addCategoryTarget( ActionBase action , MetaReleaseTarget target ) throws Exception {
		if( target.distConfItem != null ) {
			action.debug( "add delivery configuration item: " + distDelivery.NAME + "::" + target.distConfItem.KEY );
			confItems.put( target.distConfItem.KEY , target );
		}
		else 
		if( target.distDatabaseItem != null ) {
			if( dbItem != null )
				action.exit( "database item is already added to release" );
			
			action.debug( "add database delivery: " + distDelivery.NAME );
			dbItem = target;
		}
		else 
		if( target.distManualItem != null ) {
			action.debug( "add manual delivery: " + distDelivery.NAME + "::" + target.distManualItem.KEY );
			manualItems.put( target.distManualItem.KEY , target );
		}
		else
			action.exit( "unexpected type of release source =" + target.NAME );
	}
	
	public void removeCategoryTarget( ActionBase action , MetaReleaseTarget target ) throws Exception {
		if( target.distConfItem != null ) {
			action.debug( "remove delivery configuration item: " + distDelivery.NAME + "::" + target.distConfItem.KEY );
			confItems.remove( target.distConfItem.KEY );
		}
		else 
		if( target.distDatabaseItem != null ) {
			action.debug( "remove database delivery: " + distDelivery.NAME );
			dbItem = null;
		}
		else 
		if( target.distManualItem != null ) {
			action.debug( "remove manual delivery: " + distDelivery.NAME + "::" + target.distManualItem.KEY );
			manualItems.remove( target.distManualItem.KEY );
		}
		else
			action.exit( "unexpected type of release source =" + target.NAME );
	}

	public MetaReleaseTarget getDatabaseItem( ActionBase action ) throws Exception {
		return( dbItem );
	}
	
	public Map<String,MetaReleaseTarget> getConfItems( ActionBase action ) throws Exception {
		return( confItems );
	}
	
	public Map<String,MetaReleaseTarget> getManualItems( ActionBase action ) throws Exception {
		return( manualItems );
	}
	
	public Map<String,MetaReleaseTargetItem> getProjectItems( ActionBase action ) throws Exception {
		return( projectItems );
	}
	
	public boolean isEmpty() {
		if( projectItems.isEmpty() && confItems.isEmpty() && manualItems.isEmpty() && dbItem == null )
			return( true );
		return( false );
	}
	
	public boolean hasProjectItems( ActionBase action ) throws Exception {
		if( !projectItems.isEmpty() )
			return( true );
		return( false );
	}
	
	public boolean hasConfItems( ActionBase action ) throws Exception {
		if( !confItems.isEmpty() )
			return( true );
		return( false );
	}
	
	public boolean hasDatabaseItems( ActionBase action ) throws Exception {
		if( dbItem != null )
			return( true );
		return( false );
	}

	public boolean hasManualItems( ActionBase action ) throws Exception {
		if( !manualItems.isEmpty() )
			return( true );
		return( false );
	}
	
}
