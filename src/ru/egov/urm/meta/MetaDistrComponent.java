package ru.egov.urm.meta;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Node;

import ru.egov.urm.ConfReader;
import ru.egov.urm.run.ActionBase;

public class MetaDistrComponent {

	Metadata meta;

	public String NAME;
	public String UNIT;
	public boolean OBSOLETE;

	Map<String,MetaDistrComponentItem> mapBinaryItems;
	Map<String,MetaDistrComponentItem> mapConfItems;
	List<MetaDistrComponentWS> listWS;
	
	public MetaDistrComponent( Metadata meta ) {
		this.meta = meta;
	}

	public void load( ActionBase action , Node node ) throws Exception {
		mapBinaryItems = new HashMap<String,MetaDistrComponentItem>();
		mapConfItems = new HashMap<String,MetaDistrComponentItem>();
		listWS = new LinkedList<MetaDistrComponentWS>();
		
		NAME = ConfReader.getNameAttr( action , node );
		UNIT = ConfReader.getAttrValue( action , node , "unit" );
		OBSOLETE = ConfReader.getBooleanAttrValue( action , node , "obsolete" , false );
		
		Node[] items = ConfReader.xmlGetChildren( action , node , "distitem" );
		if( items != null ) {
			for( Node itemNode : items ) {
				MetaDistrComponentItem item = new MetaDistrComponentItem( meta );
				item.loadBinary( action , itemNode );
				mapBinaryItems.put( item.binaryItem.KEY , item );
			}
		}
		
		items = ConfReader.xmlGetChildren( action , node , "confitem" );
		if( items != null ) {
			for( Node itemNode : items ) {
				MetaDistrComponentItem item = new MetaDistrComponentItem( meta );
				item.loadConf( action , itemNode );
				mapConfItems.put( item.confItem.KEY , item );
			}
		}
		
		items = ConfReader.xmlGetChildren( action , node , "webservice" );
		if( items == null )
			return;
		
		for( Node itemNode : items ) {
			MetaDistrComponentWS item = new MetaDistrComponentWS( meta );
			item.load( action , itemNode );
			listWS.add( item );
		}
	}
	
	public boolean hasWebServices( ActionBase action ) throws Exception {
		if( listWS.isEmpty() )
			return( false );
		return( true );
	}

	public List<MetaDistrComponentWS> getWebServices( ActionBase action ) throws Exception {
		return( listWS );
	}

	public boolean hasBinaryItems( ActionBase action ) throws Exception {
		if( mapBinaryItems.isEmpty() )
			return( false );
		return( true );
	}

	public boolean hasConfItems( ActionBase action ) throws Exception {
		if( mapConfItems.isEmpty() )
			return( false );
		return( true );
	}

	public Map<String,MetaDistrComponentItem> getBinaryItems( ActionBase action ) throws Exception {
		return( mapBinaryItems );
	}
	
	public Map<String,MetaDistrComponentItem> getConfItems( ActionBase action ) throws Exception {
		return( mapConfItems );
	}
	
}
