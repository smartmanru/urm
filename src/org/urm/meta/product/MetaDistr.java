package org.urm.meta.product;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.urm.common.Common;
import org.urm.db.core.DBEnums.DBEnumChangeType;
import org.urm.meta.loader.MatchItem;

public class MetaDistr {

	public Meta meta;
	private Map<String,MetaDistrDelivery> mapDeliveries;
	private Map<Integer,MetaDistrDelivery> mapDeliveriesById;
	private Map<String,MetaDistrBinaryItem> mapBinaryItems;
	private Map<Integer,MetaDistrBinaryItem> mapBinaryItemsById;
	private Map<String,MetaDistrConfItem> mapConfItems;
	private Map<Integer,MetaDistrConfItem> mapConfItemsById;
	private Map<String,MetaDistrComponent> mapComps;
	private Map<Integer,MetaDistrComponent> mapCompsById;
	
	public MetaDistr( ProductMeta storage , Meta meta ) {
		this.meta = meta;
		
		mapDeliveries = new HashMap<String,MetaDistrDelivery>();
		mapDeliveriesById = new HashMap<Integer,MetaDistrDelivery>();
		mapBinaryItems = new HashMap<String,MetaDistrBinaryItem>();
		mapBinaryItemsById = new HashMap<Integer,MetaDistrBinaryItem>();
		mapConfItems = new HashMap<String,MetaDistrConfItem>();
		mapConfItemsById = new HashMap<Integer,MetaDistrConfItem>();
		mapComps = new HashMap<String,MetaDistrComponent>();
		mapCompsById = new HashMap<Integer,MetaDistrComponent>();
	}
	
	public MetaDistr copy( Meta rmeta ) throws Exception {
		MetaDistr r = new MetaDistr( rmeta.getStorage() , rmeta );
		MetaDatabase rdb = rmeta.getDatabase();
		MetaDocs rdocs = rmeta.getDocs();
		
		for( MetaDistrDelivery delivery : mapDeliveries.values() ) {
			MetaDistrDelivery rd = delivery.copy( rmeta , r , rdb , rdocs , true );
			r.addDelivery( rd );
		
			for( MetaDistrBinaryItem ritem : rd.getBinaryItems() )
				r.addBinaryItem( ritem );
			
			for( MetaDistrConfItem ritem : rd.getConfItems() )
				r.addConfItem( ritem );
		}
		
		for( MetaDistrComponent item : mapComps.values() ) {
			MetaDistrComponent ritem = item.copy( rmeta , r , true );
			r.addComponent( ritem );
		}
		
		// resolve class references
		for( MetaDistrBinaryItem ritem : r.mapBinaryItems.values() )
			ritem.resolveReferences();
		
		return( r );
	}

	public String[] getDeliveryNames() {
		return( Common.getSortedKeys( mapDeliveries ) );
	}
	
	public MetaDistrComponent[] getComponents() {
		return( mapComps.values().toArray( new MetaDistrComponent[0] ) );
	}
	
	public String[] getComponentNames() {
		return( Common.getSortedKeys( mapComps ) );
	}

	public MetaDistrComponent findComponent( String name ) {
		MetaDistrComponent comp = mapComps.get( name );
		return( comp );
	}
	
	public MetaDistrComponent getComponent( String name ) throws Exception {
		MetaDistrComponent comp = mapComps.get( name );
		if( comp == null )
			Common.exit1( _Error.UnknownDistributiveComponent1 , "unknown distributive component=" + name , name );
		return( comp );
	}
	
	public MetaDistrComponent getComponent( int id ) throws Exception {
		MetaDistrComponent comp = mapCompsById.get( id );
		if( comp == null )
			Common.exit1( _Error.UnknownDistributiveComponent1 , "unknown distributive component=" + id , "" + id );
		return( comp );
	}
	
	public MetaDistrComponent getComponent( MatchItem item ) throws Exception {
		if( item == null )
			return( null );
		if( item.MATCHED )
			return( getComponent( item.FKID ) );
		return( getComponent( item.FKNAME ) );
	}
	
	public String getComponentName( MatchItem item ) throws Exception {
		if( item == null )
			return( "" );
		MetaDistrComponent comp = getComponent( item );
		return( comp.NAME );
	}
	
	public MetaDistrComponent findComponent( MatchItem item ) {
		if( item == null )
			return( null );
		if( item.MATCHED )
			return( mapCompsById.get( item.FKID ) );
		return( mapComps.get( item.FKNAME ) );
	}
	
	public MetaDistrBinaryItem findBinaryItem( String name ) {
		return( mapBinaryItems.get( name ) );
	}
	
	public MetaDistrBinaryItem findBinaryItem( MatchItem item ) {
		if( item == null )
			return( null );
		if( item.MATCHED )
			return( mapBinaryItemsById.get( item.FKID ) );
		return( mapBinaryItems.get( item.FKNAME ) );
	}
	
	public String findBinaryItemName( MatchItem item ) {
		MetaDistrBinaryItem binaryItem = findBinaryItem( item );
		if( item == null )
			return( null );
		return( binaryItem.NAME );
	}
	
	public MetaDistrBinaryItem getBinaryItem( String name ) throws Exception {
		MetaDistrBinaryItem item = mapBinaryItems.get( name );
		if( item == null )
			Common.exit1( _Error.UnknownDistributiveItem1 , "unknown distributive item=" + name , name );
		return( item );
	}

	public Integer getBinaryItemId( String name ) throws Exception {
		if( name.isEmpty() )
			return( null );
		MetaDistrBinaryItem item = getBinaryItem( name );
		return( item.ID );
	}

	public MetaDistrBinaryItem getBinaryItem( int id ) throws Exception {
		MetaDistrBinaryItem item = mapBinaryItemsById.get( id );
		if( item == null )
			Common.exit1( _Error.UnknownDistributiveItem1 , "unknown distributive item=" + id , "" + id );
		return( item );
	}

	public MetaDistrBinaryItem getBinaryItem( MatchItem item ) throws Exception {
		if( item == null )
			return( null );
		if( item.MATCHED )
			return( getBinaryItem( item.FKID ) );
		return( getBinaryItem( item.FKNAME ) );
	}
	
	public String getBinaryItemName( MatchItem item ) throws Exception {
		if( item == null )
			return( "" );
		MetaDistrBinaryItem binaryItem = getBinaryItem( item );
		return( binaryItem.NAME );
	}
	
	public String getBinaryItemName( Integer id ) throws Exception {
		if( id == null )
			return( null );
		MetaDistrBinaryItem item = getBinaryItem( id );
		return( item.NAME );
	}

	public MetaDistrBinaryItem[] getBinaryItems() {
		return( mapBinaryItems.values().toArray( new MetaDistrBinaryItem[0] ) );
	}

	public String[] getManualItemNames() {
		List<String> list = new LinkedList<String>();
		for( MetaDistrBinaryItem item : mapBinaryItems.values() )
			if( item.isManualItem() )
				list.add( item.NAME );
		return( list.toArray( new String[0] ) );
	}
	
	public String[] getDerivedItemNames() {
		List<String> list = new LinkedList<String>();
		for( MetaDistrBinaryItem item : mapBinaryItems.values() )
			if( item.isDerivedItem() )
				list.add( item.NAME );
		return( list.toArray( new String[0] ) );
	}
	
	public String[] getBinaryItemNames() {
		return( Common.getSortedKeys( mapBinaryItems ) );
	}
	
	public MetaDistrConfItem[] getConfItems() {
		return( mapConfItems.values().toArray( new MetaDistrConfItem[0] ) );
	}

	public String[] getConfItemNames() {
		return( Common.getSortedKeys( mapConfItems ) );
	}
	
	public MetaDistrConfItem findConfItem( String name ) {
		return( mapConfItems.get( name ) );
	}

	public MetaDistrConfItem findConfItem( MatchItem item ) {
		if( item == null )
			return( null );
		if( item.MATCHED )
			return( mapConfItemsById.get( item.FKID ) );
		return( mapConfItems.get( item.FKNAME ) );
	}
	
	public MetaDistrConfItem getConfItem( String name ) throws Exception {
		MetaDistrConfItem item = mapConfItems.get( name );
		if( item == null )
			Common.exit1( _Error.UnknownConfigurationItem1 , "unknown configuration item=" + name , name );
		return( item );
	}
	
	public MetaDistrConfItem getConfItem( int id ) throws Exception {
		MetaDistrConfItem item = mapConfItemsById.get( id );
		if( item == null )
			Common.exit1( _Error.UnknownConfigurationItem1 , "unknown configuration item=" + id , "" + id );
		return( item );
	}
	
	public MetaDistrConfItem getConfItem( MatchItem item ) throws Exception {
		if( item == null )
			return( null );
		if( item.MATCHED )
			return( getConfItem( item.FKID ) );
		return( getConfItem( item.FKNAME ) );
	}
	
	public String getConfItemName( MatchItem item ) throws Exception {
		if( item == null )
			return( "" );
		MetaDistrConfItem confItem = getConfItem( item );
		return( confItem.NAME );
	}
	
	public MetaDistrDelivery[] getDeliveries() {
		return( mapDeliveries.values().toArray( new MetaDistrDelivery[0] ) );
	}

	public MetaDistrDelivery[] getDatabaseDeliveries() {
		List<MetaDistrDelivery> list = new LinkedList<MetaDistrDelivery>();
		for( MetaDistrDelivery delivery : mapDeliveries.values() )
			if( delivery.hasDatabaseItems() )
				list.add( delivery );
		return( list.toArray( new MetaDistrDelivery[0] ) );
	}

	public MetaDistrDelivery[] getDocDeliveries() {
		List<MetaDistrDelivery> list = new LinkedList<MetaDistrDelivery>();
		for( MetaDistrDelivery delivery : mapDeliveries.values() )
			if( delivery.hasDocItems() )
				list.add( delivery );
		return( list.toArray( new MetaDistrDelivery[0] ) );
	}

	public MetaDistrDelivery findDelivery( String delivery ) {
		return( mapDeliveries.get( delivery ) );
	}

	public MetaDistrDelivery findDelivery( MatchItem item ) {
		if( item == null )
			return( null );
		if( item.MATCHED )
			return( mapDeliveriesById.get( item.FKID ) );
		return( mapDeliveries.get( item.FKNAME ) );
	}
	
	public MetaDistrDelivery findDeliveryByFolder( String folder ) {
		for( MetaDistrDelivery delivery : mapDeliveries.values() ) {
			if( delivery.FOLDER.equals( folder ) )
				return( delivery );
		}
		return( null );
	}

	public String findDeliveryName( MatchItem item ) {
		MetaDistrDelivery delivery = findDelivery( item );
		if( item == null )
			return( null );
		return( delivery.NAME );
	}
	
	public MetaDistrDelivery getDelivery( String name ) throws Exception {
		MetaDistrDelivery delivery = mapDeliveries.get( name );
		if( delivery == null )
			Common.exit1( _Error.UnknownDelivery1 , "unknown delivery=" + name , name );
		return( delivery );
	}

	public MetaDistrDelivery getDelivery( MatchItem item ) throws Exception {
		if( item == null )
			return( null );
		if( item.MATCHED )
			return( getDelivery( item.FKID ) );
		return( getDelivery( item.FKNAME ) );
	}
	
	public MetaDistrDelivery getDelivery( int id ) throws Exception {
		MetaDistrDelivery delivery = mapDeliveriesById.get( id );
		if( delivery == null )
			Common.exit1( _Error.UnknownDelivery1 , "unknown delivery=" + id , "" + id );
		return( delivery );
	}

	public String getDeliveryName( MatchItem item ) throws Exception {
		if( item == null )
			return( "" );
		MetaDistrDelivery delivery = findDelivery( item );
		if( delivery == null )
			Common.exitUnexpected();
		return( delivery.NAME );
	}
	
	public void addComponent( MetaDistrComponent comp ) {
		mapComps.put( comp.NAME , comp );
		mapCompsById.put( comp.ID , comp );
	}
	
	public void addDelivery( MetaDistrDelivery delivery ) {
		mapDeliveries.put( delivery.NAME , delivery );
		mapDeliveriesById.put( delivery.ID , delivery );
	}

	public void removeDelivery( MetaDistrDelivery delivery ) {
		mapDeliveries.remove( delivery.NAME );
		mapDeliveriesById.remove( delivery.ID );
		
		for( MetaDistrBinaryItem item : delivery.getBinaryItems() )
			removeBinaryItem( item );
		for( MetaDistrConfItem item : delivery.getConfItems() )
			removeConfItem( item );
	}

	public void updateDelivery( MetaDistrDelivery delivery ) throws Exception {
		Common.changeMapKey( mapDeliveries , delivery , delivery.NAME );
	}

	public void addBinaryItem( MetaDistrDelivery delivery , MetaDistrBinaryItem item ) throws Exception {
		delivery.addBinaryItem( item );
		addBinaryItem( item );
	}
	
	public void addConfItem( MetaDistrDelivery delivery , MetaDistrConfItem item ) throws Exception {
		delivery.addConfItem( item );
		addConfItem( item );
	}

	public void addDeliverySchema( MetaDistrDelivery delivery , MetaDatabaseSchema schema , DBEnumChangeType changeType ) throws Exception {
		delivery.addSchema( schema , changeType );
	}

	public void addDeliveryDoc( MetaDistrDelivery delivery , MetaProductDoc doc , DBEnumChangeType changeType ) throws Exception {
		delivery.addDocument( doc , changeType );
	}

	public void addBinaryItem( MetaDistrBinaryItem item ) throws Exception {
		mapBinaryItems.put( item.NAME , item );
		mapBinaryItemsById.put( item.ID , item );
	}
	
	public void updateBinaryItem( MetaDistrBinaryItem item ) throws Exception {
		item.delivery.updateBinaryItem( item );
		Common.changeMapKey( mapBinaryItems , item , item.NAME );
	}
	
	public void addConfItem( MetaDistrConfItem item ) throws Exception {
		mapConfItems.put( item.NAME , item );
		mapConfItemsById.put( item.ID , item );
	}

	public void updateConfItem( MetaDistrConfItem item ) throws Exception {
		item.delivery.updateConfItem( item );
		Common.changeMapKey( mapConfItems , item , item.NAME );
	}
	
	public void removeBinaryItem( MetaDistrDelivery delivery , MetaDistrBinaryItem item ) throws Exception {
		item.delivery.removeBinaryItem( item );
		removeBinaryItem( item );
	}

	private void removeBinaryItem( MetaDistrBinaryItem item ) {
		mapBinaryItems.remove( item.NAME );
		mapBinaryItemsById.remove( item.ID );
	}

	public void changeBinaryItemToManual( MetaDistrBinaryItem item ) throws Exception {
		item.changeProjectToManual();
	}
	
	public void removeConfItem( MetaDistrDelivery delivery , MetaDistrConfItem item ) throws Exception {
		item.delivery.removeConfItem( item );
		removeConfItem( item );
	}
	
	private void removeConfItem( MetaDistrConfItem item ) {
		mapConfItems.remove( item.NAME );
		mapConfItemsById.remove( item.ID );
	}
	
	public void removeDatabaseSchema( MetaDatabaseSchema schema ) throws Exception {
		for( MetaDistrDelivery delivery : mapDeliveries.values() ) {
			if( delivery.findSchema( schema.NAME ) != null )
				delivery.removeSchema( schema );
		}
		for( MetaDistrComponent comp : getComponents() ) {
			MetaDistrComponentItem compItem = comp.findSchemaItem( schema.NAME );
			if( compItem != null )
				comp.removeCompItem( compItem );
		}
	}
	
	public void updateComponent( MetaDistrComponent comp ) throws Exception {
		Common.changeMapKey( mapComps , comp , comp.NAME );
	}
	
	public void removeComponent( MetaDistrComponent item ) throws Exception {
		mapComps.remove( item.NAME );
		mapCompsById.remove( item.ID );
	}

	public void removeUnit( MetaProductUnit unit ) throws Exception {
		for( MetaDistrDelivery delivery : mapDeliveries.values() ) {
			if( Common.equalsIntegers( delivery.UNIT_ID , unit.ID ) )
				delivery.clearUnit();
		}
	}	
	
	public void removeDocument( MetaProductDoc doc ) throws Exception {
		for( MetaDistrDelivery delivery : mapDeliveries.values() ) {
			if( delivery.findDoc( doc.NAME ) != null )
				delivery.removeDoc( doc );
		}
	}	

	public MatchItem matchComponent( String name ) throws Exception {
		if( name == null || name.isEmpty() )
			return( null );
		
		MetaDistrComponent comp = findComponent( name );
		if( comp == null )
			return( new MatchItem( name ) );
		return( new MatchItem( comp.ID ) );
	}
	
	public MatchItem matchBinaryItem( String name ) throws Exception {
		if( name == null || name.isEmpty() )
			return( null );
		
		MetaDistrBinaryItem item = findBinaryItem( name );
		if( item == null )
			return( new MatchItem( name ) );
		return( new MatchItem( item.ID ) );
	}
	
	public MatchItem matchConfItem( String name ) throws Exception {
		if( name == null || name.isEmpty() )
			return( null );
		
		MetaDistrConfItem item = findConfItem( name );
		if( item == null )
			return( new MatchItem( name ) );
		return( new MatchItem( item.ID ) );
	}
	
	public boolean matchComponent( MatchItem item ) throws Exception {
		if( item == null )
			return( true );
		
		MetaDistrComponent comp = null;
		if( item.MATCHED ) {
			comp = getComponent( item.FKID );
			return( true );
		}
		
		comp = findComponent( item.FKNAME );
		if( comp != null ) {
			item.match( comp.ID );
			return( true );
		}
		return( false );
	}
	
	public boolean matchBinaryItem( MatchItem item ) throws Exception {
		if( item == null )
			return( true );
		
		MetaDistrBinaryItem binaryItem = null;
		if( item.MATCHED ) {
			binaryItem = getBinaryItem( item.FKID );
			return( true );
		}
		
		binaryItem = findBinaryItem( item.FKNAME );
		if( binaryItem != null ) {
			item.match( binaryItem.ID );
			return( true );
		}
		return( false );
	}
	
	public boolean matchConfItem( MatchItem item ) throws Exception {
		if( item == null )
			return( true );
		
		MetaDistrConfItem confItem = null;
		if( item.MATCHED ) {
			confItem = getConfItem( item.FKID );
			return( true );
		}
		
		confItem = findConfItem( item.FKNAME );
		if( confItem != null ) {
			item.match( confItem.ID );
			return( true );
		}
		return( false );
	}

	public MatchItem getDeliveryMatchItem( Integer id , String name ) throws Exception {
		if( id == null && name.isEmpty() )
			return( null );
		MetaDistrDelivery delivery = ( id == null )? findDelivery( name ) : getDelivery( id );
		MatchItem match = ( delivery == null )? new MatchItem( name ) : new MatchItem( delivery.ID );
		return( match );
	}
	
	public MatchItem getBinaryMatchItem( Integer id , String name ) throws Exception {
		if( id == null && name.isEmpty() )
			return( null );
		MetaDistrBinaryItem binary = ( id == null )? findBinaryItem( name ) : getBinaryItem( id );
		MatchItem match = ( binary == null )? new MatchItem( name ) : new MatchItem( binary.ID );
		return( match );
	}
	
	public MatchItem getConfMatchItem( Integer id , String name ) throws Exception {
		if( id == null && name.isEmpty() )
			return( null );
		MetaDistrConfItem conf = ( id == null )? findConfItem( name ) : getConfItem( id );
		MatchItem match = ( conf == null )? new MatchItem( name ) : new MatchItem( conf.ID );
		return( match );
	}
	
}
