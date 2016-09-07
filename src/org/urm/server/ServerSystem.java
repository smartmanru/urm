package org.urm.server;

import java.util.HashMap;
import java.util.Map;

import org.urm.common.Common;
import org.urm.common.ConfReader;
import org.w3c.dom.Node;

public class ServerSystem {

	public ServerDirectory directory;
	public Map<String,ServerProduct> mapProducts;
	
	public String NAME;
	public String DESC;
	
	public ServerSystem( ServerDirectory directory ) {
		this.directory = directory;
		mapProducts = new HashMap<String,ServerProduct>();
	}
	
	public ServerSystem copy( ServerDirectory nd ) {
		ServerSystem r = new ServerSystem( nd );
		r.NAME = NAME;
		r.DESC = DESC;
		
		for( ServerProduct product : mapProducts.values() ) {
			ServerProduct rp = product.copy( nd , r );
			r.mapProducts.put( rp.NAME , rp );
		}
		return( r );
	}
	
	public void load( Node node ) throws Exception {
		NAME = ConfReader.getAttrValue( node , "name" );
		DESC = ConfReader.getAttrValue( node , "desc" );
		
		Node[] items = ConfReader.xmlGetChildren( node , "product" );
		if( items == null )
			return;
		
		for( Node itemNode : items ) {
			ServerProduct item = new ServerProduct( directory , this );
			item.load( itemNode );
			mapProducts.put( item.NAME , item );
		}
	}
	
	public String[] getProducts() throws Exception {
		return( Common.getSortedKeys( mapProducts ) );
	}

	public ServerProduct getProduct( String key ) {
		return( mapProducts.get( key ) );
	}

	public void modifySystem( ServerTransaction transaction , ServerSystem systemNew ) throws Exception {
		DESC = systemNew.DESC;
	}

	public void addProduct( ServerTransaction transaction , ServerProduct product ) throws Exception {
		mapProducts.put( product.NAME , product );
	}
	
	public void removeProduct( ServerTransaction transaction , ServerProduct product ) throws Exception {
		mapProducts.remove( product.NAME );
	}
	
}
