package org.urm.dist;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.urm.meta.MetaDistrDelivery;
import org.urm.meta.Metadata;
import org.urm.server.action.ActionBase;

public class ReleaseDelivery {

	Metadata meta;
	public Release release;
	public MetaDistrDelivery distDelivery;
	
	private Map<String,ReleaseTargetItem> projectItems;
	private Map<String,ReleaseTarget> confItems;
	private Map<String,ReleaseTarget> manualItems;
	private ReleaseTarget dbItem;
	
	public ReleaseDelivery( Metadata meta , Release release , MetaDistrDelivery distDelivery ) {
		this.meta = meta; 
		this.release = release;
		this.distDelivery = distDelivery;
		
		projectItems = new HashMap<String,ReleaseTargetItem>();
		confItems = new HashMap<String,ReleaseTarget>();
		manualItems = new HashMap<String,ReleaseTarget>();
		dbItem = null;
	}

	public ReleaseDelivery copy( ActionBase action , Release nr ) throws Exception {
		ReleaseDelivery nx = new ReleaseDelivery( meta , nr , distDelivery );
		
		for( Entry<String,ReleaseTargetItem> entry : projectItems.entrySet() ) {
			ReleaseTargetItem src = entry.getValue();
			ReleaseSet srcSet = src.target.set;
			ReleaseSet dstSet = nr.getSourceSet( action , srcSet.NAME );
			ReleaseTarget dstTarget = dstSet.getTarget( action , src.NAME );
			ReleaseTargetItem dst = dstTarget.getItem( action , src.NAME );
			nx.projectItems.put( entry.getKey() , dst );
		}
		
		for( Entry<String,ReleaseTarget> entry : confItems.entrySet() ) {
			ReleaseTarget src = entry.getValue();
			ReleaseSet dstSet = nr.getCategorySet( action , src.set.CATEGORY );
			ReleaseTarget dst = dstSet.getTarget( action , src.NAME );
			nx.confItems.put( entry.getKey() , dst );
		}
		
		for( Entry<String,ReleaseTarget> entry : manualItems.entrySet() ) {
			ReleaseTarget src = entry.getValue();
			ReleaseSet dstSet = nr.getCategorySet( action , src.set.CATEGORY );
			ReleaseTarget dst = dstSet.getTarget( action , src.NAME );
			nx.manualItems.put( entry.getKey() , dst );
		}
		
		nx.dbItem = dbItem;
		
		return( nx );
	}
	
	public void addTargetItem( ActionBase action , ReleaseTargetItem item ) throws Exception {
		action.debug( "add delivery binary item: " + distDelivery.NAME + "::" + item.distItem.KEY );
		projectItems.put( item.distItem.KEY , item );
	}

	public void removeTargetItem( ActionBase action , ReleaseTargetItem item ) throws Exception {
		action.debug( "remove delivery binary item: " + distDelivery.NAME + "::" + item.distItem.KEY );
		projectItems.remove( item.sourceItem.ITEMNAME );
	}

	public void addCategoryTarget( ActionBase action , ReleaseTarget target ) throws Exception {
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
	
	public void removeCategoryTarget( ActionBase action , ReleaseTarget target ) throws Exception {
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

	public ReleaseTarget getDatabaseItem( ActionBase action ) throws Exception {
		return( dbItem );
	}
	
	public Map<String,ReleaseTarget> getConfItems( ActionBase action ) throws Exception {
		return( confItems );
	}
	
	public Map<String,ReleaseTarget> getManualItems( ActionBase action ) throws Exception {
		return( manualItems );
	}
	
	public Map<String,ReleaseTargetItem> getProjectItems( ActionBase action ) throws Exception {
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
