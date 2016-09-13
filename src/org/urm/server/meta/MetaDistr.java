package org.urm.server.meta;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.urm.common.ConfReader;
import org.urm.common.PropertyController;
import org.urm.server.action.ActionBase;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class MetaDistr extends PropertyController {

	protected Meta meta;
	private Map<String,MetaDistrDelivery> mapDeliveries;
	private Map<String,MetaDistrBinaryItem> mapBinaryItems;
	private Map<String,MetaDistrConfItem> mapConfItems;
	private Map<String,MetaDistrComponent> mapComps;
	
	public MetaDistr( Meta meta ) {
		super( "distr" );
		this.meta = meta;
		meta.setDistr( this );
		
		mapDeliveries = new HashMap<String,MetaDistrDelivery>();
		mapBinaryItems = new HashMap<String,MetaDistrBinaryItem>();
		mapConfItems = new HashMap<String,MetaDistrConfItem>();
		mapComps = new HashMap<String,MetaDistrComponent>();
	}
	
	@Override
	public boolean isValid() {
		if( super.isLoadFailed() )
			return( false );
		return( true );
	}
	
	public MetaDistr copy( ActionBase action , Meta meta ) throws Exception {
		MetaDistr r = new MetaDistr( meta );
		super.initCopyStarted( this , meta.product.getProperties() );
		for( MetaDistrDelivery delivery : mapDeliveries.values() ) {
			MetaDistrDelivery rd = delivery.copy( action , meta , r );
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
		
		super.initFinished();
		return( r );
	}
	
	public void create( ActionBase action ) throws Exception {
		if( !super.initCreateStarted( null ) )
			return;
		
		super.initFinished();
	}
	
	public void load( ActionBase action , Node root ) throws Exception {
		if( super.initCreateStarted( meta.product.getProperties() ) )
			return;

		loadDeliveries( action , ConfReader.xmlGetPathNode( root , "distributive" ) );
		loadComponents( action , ConfReader.xmlGetPathNode( root , "deployment" ) );
		
		super.initFinished();
	}
	
	public void loadDeliveries( ActionBase action , Node node ) throws Exception {
		if( node == null )
			return;
		
		Node[] items = ConfReader.xmlGetChildren( node , "delivery" );
		if( items == null )
			return;
		
		for( Node deliveryNode : items ) {
			MetaDistrDelivery item = new MetaDistrDelivery( meta , this );
			item.load( action , deliveryNode );
			mapDeliveries.put( item.NAME , item );
			mapBinaryItems.putAll( item.getBinaryItems( action ) );
			mapConfItems.putAll( item.getConfigurationItems( action ) );
		}
		
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
		if( !super.isLoaded() )
			return;

		properties.saveAsElements( doc , root );
		saveDeliveries( action , doc , root );
		saveComponents( action , doc , root );
	}

	private void saveDeliveries( ActionBase action , Document doc , Element root ) throws Exception {
	}
	
	private void saveComponents( ActionBase action , Document doc , Element root ) throws Exception {
	}
	
	public MetaDistrComponent getComponent( ActionBase action , String KEY ) throws Exception {
		MetaDistrComponent comp = mapComps.get( KEY );
		if( comp == null )
			action.exit1( _Error.UnknownDistributiveComponent1 , "unknown distributive component=" + KEY , KEY );
		return( comp );
	}
	
	public MetaDistrBinaryItem findBinaryItem( ActionBase action , String KEY ) throws Exception {
		return( mapBinaryItems.get( KEY ) );
	}
	
	public MetaDistrBinaryItem getBinaryItem( ActionBase action , String KEY ) throws Exception {
		MetaDistrBinaryItem item = mapBinaryItems.get( KEY );
		if( item == null )
			action.exit1( _Error.UnknownDistributiveItem1 , "unknown distributive item=" + KEY , KEY );
		return( item );
	}

	public Map<String,MetaDistrBinaryItem> getBinaryItems( ActionBase action ) throws Exception {
		return( mapBinaryItems );
	}
	
	public Map<String,MetaDistrConfItem> getConfItems( ActionBase action ) throws Exception {
		return( mapConfItems );
	}
	
	public MetaDistrConfItem findConfItem( ActionBase action , String KEY ) throws Exception {
		return( mapConfItems.get( KEY ) );
	}

	public MetaDistrConfItem getConfItem( ActionBase action , String KEY ) throws Exception {
		MetaDistrConfItem item = mapConfItems.get( KEY );
		if( item == null )
			action.exit1( _Error.UnknownConfigurationItem1 , "unknown configuration item=" + KEY , KEY );
		return( item );
	}
	
	public Map<String,MetaDistrDelivery> getDeliveries( ActionBase action ) throws Exception {
		return( mapDeliveries );
	}

	public List<MetaDistrDelivery> getDatabaseDeliveries( ActionBase action ) throws Exception {
		List<MetaDistrDelivery> list = new LinkedList<MetaDistrDelivery>();
		for( MetaDistrDelivery delivery : meta.distr.getDeliveries( action ).values() )
			if( delivery.hasDatabaseItems( action ) )
				list.add( delivery );
		return( list );
	}
	
	public MetaDistrDelivery getDelivery( ActionBase action , String DELIVERY ) throws Exception {
		MetaDistrDelivery delivery = mapDeliveries.get( DELIVERY );
		if( delivery == null )
			action.exit1( _Error.UnknownDelivery1 , "unknown delivery=" + DELIVERY , DELIVERY );
		return( delivery );
	}

}
