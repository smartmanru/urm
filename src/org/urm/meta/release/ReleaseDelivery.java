package org.urm.meta.release;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.urm.action.ActionBase;
import org.urm.common.Common;
import org.urm.engine.dist._Error;
import org.urm.meta.product.Meta;
import org.urm.meta.product.MetaDistrBinaryItem;
import org.urm.meta.product.MetaDistrDelivery;

public class ReleaseDelivery {

	public Meta meta;
	public Release release;
	public MetaDistrDelivery distDelivery;
	
	private Map<String,ReleaseScopeItem> projectItems;
	private Map<String,ReleaseScopeTarget> confItems;
	private Map<String,ReleaseScopeTarget> manualItems;
	private Map<String,ReleaseScopeTarget> derivedItems;
	private Map<String,ReleaseScopeItem> schemaItems;
	private Map<String,ReleaseScopeItem> docItems;
	
	public ReleaseDelivery( Meta meta , Release release , MetaDistrDelivery distDelivery ) {
		this.meta = meta; 
		this.release = release;
		this.distDelivery = distDelivery;
		
		projectItems = new HashMap<String,ReleaseScopeItem>();
		confItems = new HashMap<String,ReleaseScopeTarget>();
		manualItems = new HashMap<String,ReleaseScopeTarget>();
		derivedItems = new HashMap<String,ReleaseScopeTarget>();
		schemaItems = new HashMap<String,ReleaseScopeItem>();
		docItems = new HashMap<String,ReleaseScopeItem>();
	}

	public ReleaseDelivery copy( ActionBase action , Release nr ) throws Exception {
		ReleaseDelivery nx = new ReleaseDelivery( meta , nr , distDelivery );
		
		for( Entry<String,ReleaseScopeItem> entry : projectItems.entrySet() ) {
			ReleaseScopeItem src = entry.getValue();
			ReleaseScopeTarget srcTarget = src.target;
			ReleaseScopeSet srcSet = srcTarget.set;
			ReleaseScopeSet dstSet = nr.getSourceSet( action , srcSet.NAME );
			ReleaseScopeTarget dstTarget = dstSet.getTarget( action , srcTarget.NAME );
			ReleaseScopeItem dst = dstTarget.findItem( src.NAME );
			nx.projectItems.put( entry.getKey() , dst );
		}
		
		for( Entry<String,ReleaseScopeTarget> entry : confItems.entrySet() ) {
			ReleaseScopeTarget src = entry.getValue();
			ReleaseScopeSet dstSet = nr.getCategorySet( action , src.set.CATEGORY );
			ReleaseScopeTarget dst = dstSet.getTarget( action , src.NAME );
			nx.confItems.put( entry.getKey() , dst );
		}
		
		for( Entry<String,ReleaseScopeTarget> entry : manualItems.entrySet() ) {
			ReleaseScopeTarget src = entry.getValue();
			ReleaseScopeSet dstSet = nr.getCategorySet( action , src.set.CATEGORY );
			ReleaseScopeTarget dst = dstSet.getTarget( action , src.NAME );
			nx.manualItems.put( entry.getKey() , dst );
		}
		
		for( Entry<String,ReleaseScopeTarget> entry : derivedItems.entrySet() ) {
			ReleaseScopeTarget src = entry.getValue();
			ReleaseScopeSet dstSet = nr.getCategorySet( action , src.set.CATEGORY );
			ReleaseScopeTarget dst = dstSet.getTarget( action , src.NAME );
			nx.derivedItems.put( entry.getKey() , dst );
		}
		
		for( Entry<String,ReleaseScopeItem> entry : schemaItems.entrySet() ) {
			ReleaseScopeItem src = entry.getValue();
			ReleaseScopeTarget srcTarget = src.target;
			ReleaseScopeSet dstSet = nr.getCategorySet( action , srcTarget.set.CATEGORY );
			ReleaseScopeTarget dstTarget = dstSet.getTarget( action , srcTarget.NAME );
			ReleaseScopeItem dst = dstTarget.findItem( src.NAME );
			nx.schemaItems.put( entry.getKey() , dst );
		}
		
		for( Entry<String,ReleaseScopeItem> entry : docItems.entrySet() ) {
			ReleaseScopeItem src = entry.getValue();
			ReleaseScopeTarget srcTarget = src.target;
			ReleaseScopeSet dstSet = nr.getCategorySet( action , srcTarget.set.CATEGORY );
			ReleaseScopeTarget dstTarget = dstSet.getTarget( action , srcTarget.NAME );
			ReleaseScopeItem dst = dstTarget.findItem( src.NAME );
			nx.docItems.put( entry.getKey() , dst );
		}
		
		return( nx );
	}
	
	public void addTargetItem( ActionBase action , ReleaseScopeItem item ) throws Exception {
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

	public void removeTargetItem( ActionBase action , ReleaseScopeItem item ) throws Exception {
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

	public void addCategoryTarget( ActionBase action , ReleaseScopeTarget target ) throws Exception {
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
	
	public void removeCategoryTarget( ActionBase action , ReleaseScopeTarget target ) throws Exception {
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
	
	public ReleaseScopeItem[] getDatabaseItems() {
		return( schemaItems.values().toArray( new ReleaseScopeItem[0] ) );
	}

	public ReleaseScopeTarget[] getConfItems() {
		return( confItems.values().toArray( new ReleaseScopeTarget[0] ) );
	}

	public String[] getConfItemNames() {
		return( Common.getSortedKeys( confItems ) );
	}

	public ReleaseScopeTarget findConfItem( String name ) {
		return( confItems.get( name ) );
	}

	public String[] getManualItemNames() {
		return( Common.getSortedKeys( manualItems ) );
	}
	
	public ReleaseScopeTarget[] getManualItems() {
		return( manualItems.values().toArray( new ReleaseScopeTarget[0] ) );
	}

	public ReleaseScopeTarget findManualItem( String name ) {
		return( manualItems.get( name ) );
	}

	public String[] getDocItemNames() {
		return( Common.getSortedKeys( docItems ) );
	}
	
	public ReleaseScopeItem[] getDocItems() {
		return( docItems.values().toArray( new ReleaseScopeItem[0] ) );
	}

	public ReleaseScopeItem findDocItem( String name ) {
		return( docItems.get( name ) );
	}

	public String[] getDerivedItemNames() {
		return( Common.getSortedKeys( derivedItems ) );
	}
	
	public ReleaseScopeTarget[] getDerivedItems() {
		return( derivedItems.values().toArray( new ReleaseScopeTarget[0] ) );
	}

	public ReleaseScopeTarget findDerivedItem( String name ) {
		return( derivedItems.get( name ) );
	}

	public String[] getProjectItemNames() {
		return( Common.getSortedKeys( projectItems ) );
	}
	
	public ReleaseScopeItem[] getProjectItems() {
		return( projectItems.values().toArray( new ReleaseScopeItem[0] ) );
	}
	
	public ReleaseScopeItem findProjectItem( String name ) {
		return( projectItems.get( name ) );
	}
	
	public ReleaseScopeItem findDatabaseItem( String name ) {
		return( schemaItems.get( name ) );
	}

	public MetaDistrBinaryItem[] getBinaryItems() {
		Map<String,MetaDistrBinaryItem> map = new HashMap<String,MetaDistrBinaryItem>();
		for( ReleaseScopeItem item : projectItems.values() )
			map.put( item.sourceItem.distItem.NAME , item.sourceItem.distItem );
		for( ReleaseScopeTarget item : manualItems.values() )
			map.put( item.distManualItem.NAME , item.distManualItem );
		for( ReleaseScopeTarget item : derivedItems.values() )
			map.put( item.distDerivedItem.NAME , item.distDerivedItem );
		
		List<MetaDistrBinaryItem> list = new LinkedList<MetaDistrBinaryItem>();
		for( String name : Common.getSortedKeys( map ) )
			list.add( map.get( name ) );
		
		return( list.toArray( new MetaDistrBinaryItem[0] ) );
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
