package org.urm.meta.product;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.urm.action.ActionBase;
import org.urm.common.Common;
import org.urm.common.ConfReader;
import org.urm.common.PropertyController;
import org.urm.engine.EngineTransaction;
import org.urm.engine.TransactionBase;
import org.urm.meta.ProductMeta;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class MetaDistr extends PropertyController {

	protected Meta meta;
	private Map<String,MetaDistrDelivery> mapDeliveries;
	private Map<String,MetaDistrBinaryItem> mapBinaryItems;
	private Map<String,MetaDistrConfItem> mapConfItems;
	private Map<String,MetaDistrComponent> mapComps;
	
	public MetaDistr( ProductMeta storage , MetaProductSettings settings , Meta meta ) {
		super( storage , settings , "distr" );
		this.meta = meta;
		meta.setDistr( this );
		
		mapDeliveries = new HashMap<String,MetaDistrDelivery>();
		mapBinaryItems = new HashMap<String,MetaDistrBinaryItem>();
		mapConfItems = new HashMap<String,MetaDistrConfItem>();
		mapComps = new HashMap<String,MetaDistrComponent>();
	}
	
	@Override
	public String getName() {
		return( "meta-distr" );
	}
	
	@Override
	public boolean isValid() {
		if( super.isLoadFailed() )
			return( false );
		return( true );
	}
	
	@Override
	public void scatterProperties( ActionBase action ) throws Exception {
	}
	
	public MetaDistr copy( ActionBase action , Meta meta , MetaDatabase rdb ) throws Exception {
		MetaProductSettings product = meta.getProductSettings( action );
		MetaDistr r = new MetaDistr( meta.getStorage( action ) , product , meta );
		r.initCopyStarted( this , product.getProperties() );
		for( MetaDistrDelivery delivery : mapDeliveries.values() ) {
			MetaDistrDelivery rd = delivery.copy( action , meta , r , rdb );
			r.mapDeliveries.put( rd.NAME , rd );
		}
		
		for( MetaDistrBinaryItem item : mapBinaryItems.values() ) {
			MetaDistrDelivery rd = r.getDelivery( action , item.delivery.NAME );
			MetaDistrBinaryItem ritem = rd.getBinaryItem( action , item.KEY );
			r.mapBinaryItems.put( ritem.KEY , ritem );
		}
		
		for( MetaDistrConfItem item : mapConfItems.values() ) {
			MetaDistrDelivery rd = r.getDelivery( action , item.delivery.NAME );
			MetaDistrConfItem ritem = rd.getConfItem( action , item.KEY );
			r.mapConfItems.put( ritem.KEY , ritem );
		}
		
		for( MetaDistrComponent item : mapComps.values() ) {
			MetaDistrComponent ritem = item.copy( action , meta , r );
			r.mapComps.put( ritem.NAME , ritem );
		}
		
		r.resolveReferences( action );
		r.initFinished();
		return( r );
	}
	
	public void createDistr( TransactionBase transaction ) throws Exception {
		if( !super.initCreateStarted( null ) )
			return;
		
		super.initFinished();
	}
	
	public void load( ActionBase action , MetaDatabase db , Node root ) throws Exception {
		MetaProductSettings product = meta.getProductSettings( action );
		if( !super.initCreateStarted( product.getProperties() ) )
			return;

		loadDeliveries( action , db , ConfReader.xmlGetPathNode( root , "deliveries" ) );
		loadComponents( action , ConfReader.xmlGetPathNode( root , "components" ) );
		
		super.initFinished();
	}
	
	public void loadDeliveries( ActionBase action , MetaDatabase db , Node node ) throws Exception {
		if( node == null )
			return;
		
		Node[] items = ConfReader.xmlGetChildren( node , "delivery" );
		if( items == null )
			return;
		
		for( Node deliveryNode : items ) {
			MetaDistrDelivery item = new MetaDistrDelivery( meta , this , db );
			item.load( action , deliveryNode );
			mapDeliveries.put( item.NAME , item );
			for( MetaDistrBinaryItem binaryItem : item.getBinaryItems() )
				mapBinaryItems.put( binaryItem.KEY , binaryItem );
			for( MetaDistrConfItem confItem : item.getConfItems() )
				mapConfItems.put( confItem.KEY , confItem );
		}
		
		resolveReferences( action );
	}
	
	private void resolveReferences( ActionBase action ) throws Exception {
		for( MetaDistrBinaryItem item : mapBinaryItems.values() )
			item.resolveReferences( action );
	}

	public void loadComponents( ActionBase action , Node node ) throws Exception {
		mapComps = new HashMap<String,MetaDistrComponent>();
		if( node == null )
			return;
		
		Node[] items = ConfReader.xmlGetChildren( node , "component" );
		if( items == null )
			return;
		
		for( Node compNode : items ) {
			MetaDistrComponent item = new MetaDistrComponent( meta , this );
			item.load( action , compNode );
			mapComps.put( item.NAME , item );
		}
	}
	
	public void save( ActionBase action , Document doc , Element root ) throws Exception {
		super.saveAsElements( doc , root , false );
		Element deliveries = Common.xmlCreateElement( doc , root , "deliveries" );
		saveDeliveries( action , doc , deliveries );
		Element components = Common.xmlCreateElement( doc , root , "components" );
		saveComponents( action , doc , components );
	}

	private void saveDeliveries( ActionBase action , Document doc , Element root ) throws Exception {
		for( MetaDistrDelivery delivery : mapDeliveries.values() ) {
			Element deliveryElement = Common.xmlCreateElement( doc , root , "delivery" );
			delivery.save( action , doc , deliveryElement );
		}		
	}
	
	private void saveComponents( ActionBase action , Document doc , Element root ) throws Exception {
		for( MetaDistrComponent item : mapComps.values() ) {
			Element compElement = Common.xmlCreateElement( doc , root , "component" );
			item.save( action , doc , compElement );
		}
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

	public MetaDistrComponent findComponent( String KEY ) {
		MetaDistrComponent comp = mapComps.get( KEY );
		return( comp );
	}
	
	public MetaDistrComponent getComponent( ActionBase action , String KEY ) throws Exception {
		MetaDistrComponent comp = mapComps.get( KEY );
		if( comp == null )
			action.exit1( _Error.UnknownDistributiveComponent1 , "unknown distributive component=" + KEY , KEY );
		return( comp );
	}
	
	public MetaDistrBinaryItem findBinaryItem( String KEY ) {
		return( mapBinaryItems.get( KEY ) );
	}
	
	public MetaDistrBinaryItem getBinaryItem( ActionBase action , String KEY ) throws Exception {
		MetaDistrBinaryItem item = mapBinaryItems.get( KEY );
		if( item == null )
			action.exit1( _Error.UnknownDistributiveItem1 , "unknown distributive item=" + KEY , KEY );
		return( item );
	}

	public MetaDistrBinaryItem[] getBinaryItems() {
		return( mapBinaryItems.values().toArray( new MetaDistrBinaryItem[0] ) );
	}

	public String[] getManualItemNames() {
		List<String> list = new LinkedList<String>();
		for( MetaDistrBinaryItem item : mapBinaryItems.values() )
			if( item.isManualItem() )
				list.add( item.KEY );
		return( list.toArray( new String[0] ) );
	}
	
	public String[] getDerivedItemNames() {
		List<String> list = new LinkedList<String>();
		for( MetaDistrBinaryItem item : mapBinaryItems.values() )
			if( item.isDerivedItem() )
				list.add( item.KEY );
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
	
	public MetaDistrConfItem findConfItem( String KEY ) {
		return( mapConfItems.get( KEY ) );
	}

	public MetaDistrConfItem getConfItem( ActionBase action , String KEY ) throws Exception {
		MetaDistrConfItem item = mapConfItems.get( KEY );
		if( item == null )
			action.exit1( _Error.UnknownConfigurationItem1 , "unknown configuration item=" + KEY , KEY );
		return( item );
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

	public MetaDistrDelivery findDelivery( String DELIVERY ) {
		return( mapDeliveries.get( DELIVERY ) );
	}

	public MetaDistrDelivery findDeliveryByFolder( String folder ) {
		for( MetaDistrDelivery delivery : mapDeliveries.values() ) {
			if( delivery.FOLDER.equals( folder ) )
				return( delivery );
		}
		return( null );
	}
	
	public MetaDistrDelivery getDelivery( ActionBase action , String DELIVERY ) throws Exception {
		MetaDistrDelivery delivery = mapDeliveries.get( DELIVERY );
		if( delivery == null )
			action.exit1( _Error.UnknownDelivery1 , "unknown delivery=" + DELIVERY , DELIVERY );
		return( delivery );
	}

	public void createDelivery( EngineTransaction transaction , MetaDistrDelivery delivery ) throws Exception {
		mapDeliveries.put( delivery.NAME , delivery );
	}

	public void deleteDelivery( EngineTransaction transaction , MetaDistrDelivery delivery ) throws Exception {
		delivery.deleteAllItems( transaction );
		mapDeliveries.remove( delivery.NAME );
	}

	public void modifyDelivery( EngineTransaction transaction , MetaDistrDelivery delivery ) throws Exception {
		for( Entry<String,MetaDistrDelivery> entry : mapDeliveries.entrySet() ) {
			if( entry.getValue() == delivery ) {
				mapDeliveries.remove( entry.getKey() );
				break;
			}
		}
		
		mapDeliveries.put( delivery.NAME , delivery );
	}

	public void createDistrBinaryItem( EngineTransaction transaction , MetaDistrDelivery delivery , MetaDistrBinaryItem item ) throws Exception {
		delivery.createBinaryItem( transaction , item );
		mapBinaryItems.put( item.KEY , item );
	}
	
	public void createDistrConfItem( EngineTransaction transaction , MetaDistrDelivery delivery , MetaDistrConfItem item ) throws Exception {
		delivery.createConfItem( transaction , item );
		mapConfItems.put( item.KEY , item );
	}

	public void deleteBinaryItem( EngineTransaction transaction , MetaDistrBinaryItem item ) throws Exception {
		item.delivery.deleteBinaryItem( transaction , item );
		mapBinaryItems.remove( item.KEY );
	}

	public void changeBinaryItemProjectToManual( EngineTransaction transaction , MetaDistrBinaryItem item ) throws Exception {
		item.changeProjectToManual( transaction );
	}
	
	public void deleteConfItem( EngineTransaction transaction , MetaDistrConfItem item ) throws Exception {
		item.delivery.deleteConfItem( transaction , item );
		mapConfItems.remove( item.KEY );
	}
	
	public void deleteDatabaseSchema( EngineTransaction transaction , MetaDatabaseSchema schema ) throws Exception {
		for( MetaDistrDelivery delivery : mapDeliveries.values() ) {
			if( delivery.findSchema( schema.SCHEMA ) != null )
				delivery.deleteSchema( transaction , schema );
		}
		for( MetaDistrComponent comp : getComponents() ) {
			MetaDistrComponentItem compItem = comp.findSchemaItem( schema.SCHEMA );
			if( compItem != null )
				comp.removeCompItem( transaction , compItem );
		}
	}
	
	public void createDistrComponent( EngineTransaction transaction , MetaDistrComponent item ) throws Exception {
		mapComps.put( item.NAME , item );
	}
	
	public void modifyDistrComponent( EngineTransaction transaction , MetaDistrComponent item ) throws Exception {
	}
	
	public void deleteDistrComponent( EngineTransaction transaction , MetaDistrComponent item ) throws Exception {
		mapComps.remove( item.NAME );
	}
	
}
