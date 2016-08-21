package org.urm.server.meta;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.urm.common.ConfReader;
import org.urm.server.ServerRegistry;
import org.urm.server.action.ActionBase;
import org.w3c.dom.Node;

public class MetaDistr {

	private boolean loaded;
	public boolean loadFailed;

	protected Meta meta;
	private Map<String,MetaDistrDelivery> mapDeliveries;
	private Map<String,MetaDistrBinaryItem> mapBinaryItems;
	private Map<String,MetaDistrConfItem> mapConfItems;
	private Map<String,MetaDistrComponent> mapComps;
	
	public MetaDistr( Meta meta ) {
		this.meta = meta;
		loaded = false;
		loadFailed = false;
	}
	
	public MetaDistr copy( ActionBase action , Meta meta ) throws Exception {
		return( null );
	}
	
	public void setLoadFailed() {
		loadFailed = true;
	}
	
	public void createInitial( ActionBase action , ServerRegistry registry ) throws Exception {
	}
	
	public void load( ActionBase action , Node root ) throws Exception {
		if( loaded )
			return;

		loaded = true;
		loadDeliveries( action , ConfReader.xmlGetPathNode( root , "distributive" ) );
		loadComponents( action , ConfReader.xmlGetPathNode( root , "deployment" ) );
	}
	
	public void loadDeliveries( ActionBase action , Node node ) throws Exception {
		mapDeliveries = new HashMap<String,MetaDistrDelivery>();
		mapBinaryItems = new HashMap<String,MetaDistrBinaryItem>();
		mapConfItems = new HashMap<String,MetaDistrConfItem>();
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
	
	public MetaDistrComponent getComponent( ActionBase action , String KEY ) throws Exception {
		MetaDistrComponent comp = mapComps.get( KEY );
		if( comp == null )
			action.exit( "unknown component=" + KEY );
		return( comp );
	}
	
	public MetaDistrBinaryItem findBinaryItem( ActionBase action , String KEY ) throws Exception {
		return( mapBinaryItems.get( KEY ) );
	}
	
	public MetaDistrBinaryItem getBinaryItem( ActionBase action , String KEY ) throws Exception {
		MetaDistrBinaryItem item = mapBinaryItems.get( KEY );
		if( item == null )
			action.exit( "unknown distributive item=" + KEY );
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
			action.exit( "unknown configuration item=" + KEY );
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
			action.exit( "unknown delivery=" + DELIVERY );
		return( delivery );
	}

}
