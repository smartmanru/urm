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
	private Map<String,ReleaseTargetItem> schemaItems;
	private Map<String,ReleaseTargetItem> docItems;
	
	public ReleaseDelivery( Meta meta , Release release , MetaDistrDelivery distDelivery ) {
		this.meta = meta; 
		this.release = release;
		this.distDelivery = distDelivery;
		
		projectItems = new HashMap<String,ReleaseTargetItem>();
		confItems = new HashMap<String,ReleaseTarget>();
		manualItems = new HashMap<String,ReleaseTarget>();
		derivedItems = new HashMap<String,ReleaseTarget>();
		schemaItems = new HashMap<String,ReleaseTargetItem>();
		docItems = new HashMap<String,ReleaseTargetItem>();
	}

	public ReleaseDelivery copy( ActionBase action , Release nr ) throws Exception {
		ReleaseDelivery nx = new ReleaseDelivery( meta , nr , distDelivery );
		
		for( Entry<String,ReleaseTargetItem> entry : projectItems.entrySet() ) {
			ReleaseTargetItem src = entry.getValue();
			ReleaseTarget srcTarget = src.target;
			ReleaseSet srcSet = srcTarget.set;
			ReleaseSet dstSet = nr.getSourceSet( action , srcSet.NAME );
			ReleaseTarget dstTarget = dstSet.getTarget( action , srcTarget.NAME );
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
		
		for( Entry<String,ReleaseTargetItem> entry : schemaItems.entrySet() ) {
			ReleaseTargetItem src = entry.getValue();
			ReleaseTarget srcTarget = src.target;
			ReleaseSet dstSet = nr.getCategorySet( action , srcTarget.set.CATEGORY );
			ReleaseTarget dstTarget = dstSet.getTarget( action , srcTarget.NAME );
			ReleaseTargetItem dst = dstTarget.findItem( src.NAME );
			nx.schemaItems.put( entry.getKey() , dst );
		}
		
		for( Entry<String,ReleaseTargetItem> entry : docItems.entrySet() ) {
			ReleaseTargetItem src = entry.getValue();
			ReleaseTarget srcTarget = src.target;
			ReleaseSet dstSet = nr.getCategorySet( action , srcTarget.set.CATEGORY );
			ReleaseTarget dstTarget = dstSet.getTarget( action , srcTarget.NAME );
			ReleaseTargetItem dst = dstTarget.findItem( src.NAME );
			nx.docItems.put( entry.getKey() , dst );
		}
		
		return( nx );
	}
	
	public void addTargetItem( ActionBase action , ReleaseTargetItem item ) throws Exception {
		if( item.distItem != null ) {
			action.debug( "add delivery binary item: " + distDelivery.NAME + "::" + item.distItem.NAME );
			projectItems.put( item.distItem.NAME , item );
		}
		else
		if( item.schema != null ) {
			action.debug( "add delivery schema item: " + distDelivery.NAME + "::" + item.schema.NAME );
			schemaItems.put( item.schema.NAME , item );
		}
		else
		if( item.doc != null ) {
			action.debug( "add delivery doc item: " + distDelivery.NAME + "::" + item.doc.NAME );
			docItems.put( item.doc.NAME , item );
		}
	}

	public void removeTargetItem( ActionBase action , ReleaseTargetItem item ) throws Exception {
		if( item.distItem != null ) {
			action.debug( "remove delivery binary item: " + distDelivery.NAME + "::" + item.distItem.NAME );
			projectItems.remove( item.distItem.NAME );
		}
		else
		if( item.schema != null ) {
			action.debug( "remove delivery schema item: " + distDelivery.NAME + "::" + item.schema.NAME );
			schemaItems.remove( item.schema.NAME );
		}
		else
		if( item.doc != null ) {
			action.debug( "remove delivery doc item: " + distDelivery.NAME + "::" + item.doc.NAME );
			docItems.remove( item.doc.NAME );
		}
	}

	public void addCategoryTarget( ActionBase action , ReleaseTarget target ) throws Exception {
		if( target.distConfItem != null ) {
			action.debug( "add delivery configuration item: " + distDelivery.NAME + "::" + target.distConfItem.NAME );
			confItems.put( target.distConfItem.NAME , target );
		}
		else 
		if( target.distManualItem != null ) {
			action.debug( "add manual delivery: " + distDelivery.NAME + "::" + target.distManualItem.NAME );
			manualItems.put( target.distManualItem.NAME , target );
		}
		else 
		if( target.distDerivedItem != null ) {
			action.debug( "add derived delivery: " + distDelivery.NAME + "::" + target.distDerivedItem.NAME );
			derivedItems.put( target.distDerivedItem.NAME , target );
		}
		else
			action.exit1( _Error.UnexpectedReleaseSourceType1 , "unexpected type of release source =" + target.NAME , target.NAME );
	}
	
	public void removeCategoryTarget( ActionBase action , ReleaseTarget target ) throws Exception {
		if( target.distConfItem != null ) {
			action.debug( "remove delivery configuration item: " + distDelivery.NAME + "::" + target.distConfItem.NAME );
			confItems.remove( target.distConfItem.NAME );
		}
		else 
		if( target.distManualItem != null ) {
			action.debug( "remove manual delivery: " + distDelivery.NAME + "::" + target.distManualItem.NAME );
			manualItems.remove( target.distManualItem.NAME );
		}
		else 
		if( target.distDerivedItem != null ) {
			action.debug( "remove manual delivery: " + distDelivery.NAME + "::" + target.distDerivedItem.NAME );
			derivedItems.remove( target.distDerivedItem.NAME );
		}
		else
			action.exit1( _Error.UnexpectedReleaseSourceType1 , "unexpected type of release source =" + target.NAME , target.NAME );
	}

	public String[] getDatabaseItemNames() {
		return( Common.getSortedKeys( schemaItems ) );
	}
	
	public ReleaseTargetItem[] getDatabaseItems() {
		return( schemaItems.values().toArray( new ReleaseTargetItem[0] ) );
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

	public String[] getDocItemNames() {
		return( Common.getSortedKeys( docItems ) );
	}
	
	public ReleaseTargetItem[] getDocItems() {
		return( docItems.values().toArray( new ReleaseTargetItem[0] ) );
	}

	public ReleaseTargetItem findDocItem( String name ) {
		return( docItems.get( name ) );
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
	
	public ReleaseTargetItem findDatabaseItem( String name ) {
		return( schemaItems.get( name ) );
	}
	
	public boolean isEmpty() {
		if( projectItems.isEmpty() && confItems.isEmpty() && manualItems.isEmpty() && derivedItems.isEmpty() && schemaItems.isEmpty() && docItems.isEmpty() )
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
		if( !schemaItems.isEmpty() )
			return( true );
		return( false );
	}

	public boolean hasDocItems() {
		if( !docItems.isEmpty() )
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
