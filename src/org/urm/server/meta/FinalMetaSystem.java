package org.urm.server.meta;

import java.util.HashMap;
import java.util.Map;

import org.urm.common.Common;
import org.urm.common.ConfReader;
import org.urm.server.ServerTransaction;
import org.w3c.dom.Node;

public class FinalMetaSystem {

	public FinalRegistry registry;
	public Map<String,FinalMetaProduct> mapProducts;
	
	public String NAME;
	public String DESC;
	
	public FinalMetaSystem( FinalRegistry engine ) {
		this.registry = engine;
		mapProducts = new HashMap<String,FinalMetaProduct>();
	}
	
	public void load( Node node ) throws Exception {
		NAME = ConfReader.getAttrValue( node , "name" );
		DESC = ConfReader.getAttrValue( node , "desc" );
		
		Node[] items = ConfReader.xmlGetChildren( node , "product" );
		if( items == null )
			return;
		
		for( Node itemNode : items ) {
			FinalMetaProduct item = new FinalMetaProduct( registry , this );
			item.load( itemNode );
			mapProducts.put( item.NAME , item );
		}
	}
	
	public String[] getProducts() throws Exception {
		return( Common.getSortedKeys( mapProducts ) );
	}

	public FinalMetaProduct getProduct( String key ) {
		return( mapProducts.get( key ) );
	}

	public void modifySystem( ServerTransaction transaction , FinalMetaSystem systemNew ) throws Exception {
		DESC = systemNew.DESC;
	}
	
}