package ru.egov.urm.meta;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import ru.egov.urm.ConfReader;
import ru.egov.urm.action.ActionBase;
import ru.egov.urm.storage.MetadataStorage;

public class MetaDistr {

	boolean loaded = false;

	Metadata meta;
	private Map<String,MetaDistrDelivery> mapDeliveries;
	private Map<String,MetaDistrBinaryItem> mapBinaryItems;
	private Map<String,MetaDistrConfItem> mapConfItems;
	private Map<String,MetaDistrComponent> mapComps;
	
	public MetaDatabase database;
	
	public MetaDistr( Metadata meta ) {
		this.meta = meta;
	}
	
	public void load( ActionBase action , MetadataStorage storage ) throws Exception {
		if( loaded )
			return;

		loaded = true;
		
		// read xml
		String file = storage.getDistrFile( action );
		
		action.debug( "read distributive definition file " + file + "..." );
		Document doc = ConfReader.readXmlFile( action , file );
		loadDatabase( action , ConfReader.xmlGetPathNode( action , doc.getDocumentElement() , "distributive/database" ) );
		loadDeliveries( action , ConfReader.xmlGetPathNode( action , doc.getDocumentElement() , "distributive" ) );
		loadComponents( action , ConfReader.xmlGetPathNode( action , doc.getDocumentElement() , "deployment" ) );
	}
	
	public void loadDatabase( ActionBase action , Node node ) throws Exception {
		database = new MetaDatabase( meta );
		if( node != null )
			database.load( action , node );
	}

	public void loadDeliveries( ActionBase action , Node node ) throws Exception {
		mapDeliveries = new HashMap<String,MetaDistrDelivery>();
		mapBinaryItems = new HashMap<String,MetaDistrBinaryItem>();
		mapConfItems = new HashMap<String,MetaDistrConfItem>();
		if( node == null )
			return;
		
		Node[] items = ConfReader.xmlGetChildren( action , node , "delivery" );
		if( items == null )
			return;
		
		for( Node deliveryNode : items ) {
			MetaDistrDelivery item = new MetaDistrDelivery( meta );
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
		
		Node[] items = ConfReader.xmlGetChildren( action , node , "component" );
		if( items == null )
			return;
		
		for( Node compNode : items ) {
			MetaDistrComponent item = new MetaDistrComponent( meta );
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
