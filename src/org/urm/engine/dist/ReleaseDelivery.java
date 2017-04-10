package org.urm.engine.dist;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.urm.action.ActionBase;
import org.urm.common.Common;
import org.urm.meta.product.Meta;
import org.urm.meta.product.MetaDistrDelivery;

public class ReleaseDelivery {

	public Meta meta;
	public Release release;
	public MetaDistrDelivery distDelivery;
	
	private Map<String,ReleaseTargetItem> projectItems;
	private Map<String,ReleaseTarget> confItems;
	private Map<String,ReleaseTarget> manualItems;
	private Map<String,ReleaseTarget> derivedItems;
	private ReleaseTarget dbItem;
	
	public ReleaseDelivery( Meta meta , Release release , MetaDistrDelivery distDelivery ) {
		this.meta = meta; 
		this.release = release;
		this.distDelivery = distDelivery;
		
		projectItems = new HashMap<String,ReleaseTargetItem>();
		confItems = new HashMap<String,ReleaseTarget>();
		manualItems = new HashMap<String,ReleaseTarget>();
		derivedItems = new HashMap<String,ReleaseTarget>();
		dbItem = null;
	}

	public ReleaseDelivery copy( ActionBase action , Release nr ) throws Exception {
		ReleaseDelivery nx = new ReleaseDelivery( meta , nr , distDelivery );
		
		for( Entry<String,ReleaseTargetItem> entry : projectItems.entrySet() ) {
			ReleaseTargetItem src = entry.getValue();
			ReleaseSet srcSet = src.target.set;
			ReleaseSet dstSet = nr.getSourceSet( action , srcSet.NAME );
			ReleaseTarget dstTarget = dstSet.getTarget( action , src.NAME );
			ReleaseTargetItem dst = dstTarget.findItem( src.NAME );
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
		
		for( Entry<String,ReleaseTarget> entry : derivedItems.entrySet() ) {
			ReleaseTarget src = entry.getValue();
			ReleaseSet dstSet = nr.getCategorySet( action , src.set.CATEGORY );
			ReleaseTarget dst = dstSet.getTarget( action , src.NAME );
			nx.derivedItems.put( entry.getKey() , dst );
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
		projectItems.remove( item.distItem.KEY );
	}

	public void addCategoryTarget( ActionBase action , ReleaseTarget target ) throws Exception {
		if( target.distConfItem != null ) {
			action.debug( "add delivery configuration item: " + distDelivery.NAME + "::" + target.distConfItem.KEY );
			confItems.put( target.distConfItem.KEY , target );
		}
		else 
		if( target.distDatabaseItem != null ) {
			if( dbItem != null )
				action.exit0( _Error.DatabaseItemAlreadyAdded0 , "database item is already added to release" );
			
			action.debug( "add database delivery: " + distDelivery.NAME );
			dbItem = target;
		}
		else 
		if( target.distManualItem != null ) {
			action.debug( "add manual delivery: " + distDelivery.NAME + "::" + target.distManualItem.KEY );
			manualItems.put( target.distManualItem.KEY , target );
		}
		else 
		if( target.distDerivedItem != null ) {
			action.debug( "add derived delivery: " + distDelivery.NAME + "::" + target.distDerivedItem.KEY );
			derivedItems.put( target.distDerivedItem.KEY , target );
		}
		else
			action.exit1( _Error.UnexpectedReleaseSourceType1 , "unexpected type of release source =" + target.NAME , target.NAME );
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
		if( target.distDerivedItem != null ) {
			action.debug( "remove manual delivery: " + distDelivery.NAME + "::" + target.distDerivedItem.KEY );
			derivedItems.remove( target.distDerivedItem.KEY );
		}
		else
			action.exit1( _Error.UnexpectedReleaseSourceType1 , "unexpected type of release source =" + target.NAME , target.NAME );
	}

	public ReleaseTarget getDatabaseItem( ActionBase action ) throws Exception {
		return( dbItem );
	}
	
	public ReleaseTarget[] getConfItems() {
		return( confItems.values().toArray( new ReleaseTarget[0] ) );
	}

	public String[] getConfItemNames() {
		return( Common.getSortedKeys( confItems ) );
	}

	public ReleaseTarget findConfItem( String name ) {
		return( confItems.get( name ) );
	}

	public String[] getManualItemNames() {
		return( Common.getSortedKeys( manualItems ) );
	}
	
	public ReleaseTarget[] getManualItems() {
		return( manualItems.values().toArray( new ReleaseTarget[0] ) );
	}

	public ReleaseTarget findManualItem( String name ) {
		return( manualItems.get( name ) );
	}

	public String[] getDerivedItemNames() {
		return( Common.getSortedKeys( derivedItems ) );
	}
	
	public ReleaseTarget[] getDerivedItems() {
		return( derivedItems.values().toArray( new ReleaseTarget[0] ) );
	}

	public ReleaseTarget findDerivedItem( String name ) {
		return( derivedItems.get( name ) );
	}

	public String[] getProjectItemNames() {
		return( Common.getSortedKeys( projectItems ) );
	}
	
	public ReleaseTargetItem[] getProjectItems() {
		return( projectItems.values().toArray( new ReleaseTargetItem[0] ) );
	}
	
	public ReleaseTargetItem findProjectItem( String name ) {
		return( projectItems.get( name ) );
	}
	
	public boolean isEmpty() {
		if( projectItems.isEmpty() && confItems.isEmpty() && manualItems.isEmpty() && derivedItems.isEmpty() && dbItem == null )
			return( true );
		return( false );
	}
	
	public boolean hasProjectItems() {
		if( !projectItems.isEmpty() )
			return( true );
		return( false );
	}
	
	public boolean hasConfItems() {
		if( !confItems.isEmpty() )
			return( true );
		return( false );
	}
	
	public boolean hasDatabaseItems() {
		if( dbItem != null )
			return( true );
		return( false );
	}

	public boolean hasManualItems() {
		if( !manualItems.isEmpty() )
			return( true );
		return( false );
	}
	
	public boolean hasDerivedItems() {
		if( !manualItems.isEmpty() )
			return( true );
		return( false );
	}
	
}
