package org.urm.engine.meta;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.urm.action.ActionBase;
import org.urm.common.ConfReader;
import org.urm.engine.meta.Meta.VarNAMETYPE;
import org.w3c.dom.Node;

public class MetaDistrComponent {

	protected Meta meta;
	MetaDistr dist;

	public String NAME;
	public String UNIT;
	public boolean OBSOLETE;

	Map<String,MetaDistrComponentItem> mapBinaryItems;
	Map<String,MetaDistrComponentItem> mapConfItems;
	List<MetaDistrComponentWS> listWS;
	
	public MetaDistrComponent( Meta meta , MetaDistr dist ) {
		this.meta = meta;
		this.dist = dist;
	}

	public MetaDistrComponent copy( ActionBase action , Meta meta , MetaDistr distr ) throws Exception {
		MetaDistrComponent r = new MetaDistrComponent( meta , distr );
		return( r );
	}
	
	public void load( ActionBase action , Node node ) throws Exception {
		mapBinaryItems = new HashMap<String,MetaDistrComponentItem>();
		mapConfItems = new HashMap<String,MetaDistrComponentItem>();
		listWS = new LinkedList<MetaDistrComponentWS>();
		
		NAME = action.getNameAttr( node , VarNAMETYPE.ALPHANUMDOT );
		UNIT = ConfReader.getAttrValue( node , "unit" );
		OBSOLETE = ConfReader.getBooleanAttrValue( node , "obsolete" , false );
		
		Node[] items = ConfReader.xmlGetChildren( node , "distitem" );
		if( items != null ) {
			for( Node itemNode : items ) {
				MetaDistrComponentItem item = new MetaDistrComponentItem( meta , this );
				item.loadBinary( action , itemNode );
				mapBinaryItems.put( item.binaryItem.KEY , item );
			}
		}
		
		items = ConfReader.xmlGetChildren( node , "confitem" );
		if( items != null ) {
			for( Node itemNode : items ) {
				MetaDistrComponentItem item = new MetaDistrComponentItem( meta , this );
				item.loadConf( action , itemNode );
				mapConfItems.put( item.confItem.KEY , item );
			}
		}
		
		items = ConfReader.xmlGetChildren( node , "webservice" );
		if( items == null )
			return;
		
		for( Node itemNode : items ) {
			MetaDistrComponentWS item = new MetaDistrComponentWS( meta , this );
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
