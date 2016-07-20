package org.urm.server.meta;

import java.util.HashMap;
import java.util.Map;

import org.urm.common.Common;
import org.urm.common.ConfReader;
import org.w3c.dom.Node;

public class MetaEngineSystem {

	public MetaEngine engine;
	public Map<String,MetaEngineProduct> mapProducts;
	
	public String NAME;
	
	public MetaEngineSystem( MetaEngine engine ) {
		this.engine = engine;
		mapProducts = new HashMap<String,MetaEngineProduct>();
	}
	
	public void load( Node node ) throws Exception {
		NAME = ConfReader.getAttrValue( node , "name" );
		
		Node[] items = ConfReader.xmlGetChildren( node , "product" );
		if( items == null )
			return;
		
		for( Node itemNode : items ) {
			MetaEngineProduct item = new MetaEngineProduct( this );
			item.load( itemNode );
			mapProducts.put( item.NAME , item );
		}
	}
	
	public String[] getProducts() throws Exception {
		return( Common.getSortedKeys( mapProducts ) );
	}

	public MetaEngineProduct getProduct( String key ) {
		return( mapProducts.get( key ) );
	}
	
}
