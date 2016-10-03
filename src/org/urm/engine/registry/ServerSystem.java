package org.urm.engine.registry;

import java.util.HashMap;
import java.util.Map;

import org.urm.action.ActionBase;
import org.urm.common.Common;
import org.urm.common.ConfReader;
import org.urm.engine.ServerObject;
import org.urm.engine.ServerTransaction;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class ServerSystem extends ServerObject {

	public ServerDirectory directory;
	public Map<String,ServerProduct> mapProducts;
	
	public String NAME;
	public String DESC;
	public boolean OFFLINE;
	
	public ServerSystem( ServerDirectory directory ) {
		super( directory );
		this.directory = directory;
		mapProducts = new HashMap<String,ServerProduct>();
	}

	public void createSystem( ServerTransaction transaction , String name , String desc ) {
		this.NAME = name;
		this.DESC = desc;
		this.OFFLINE = true;
	}
	
	public ServerSystem copy( ServerDirectory nd ) {
		ServerSystem r = new ServerSystem( nd );
		r.NAME = NAME;
		r.DESC = DESC;
		r.OFFLINE = OFFLINE;
		
		for( ServerProduct product : mapProducts.values() ) {
			ServerProduct rp = product.copy( nd , r );
			r.mapProducts.put( rp.NAME , rp );
		}
		return( r );
	}
	
	public void load( Node node ) throws Exception {
		NAME = ConfReader.getAttrValue( node , "name" );
		DESC = ConfReader.getAttrValue( node , "desc" );
		OFFLINE = ConfReader.getBooleanAttrValue( node , "offline" , true );
		
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

	public void modifySystem( ServerTransaction transaction ) throws Exception {
	}

	public void addProduct( ServerTransaction transaction , ServerProduct product ) throws Exception {
		mapProducts.put( product.NAME , product );
	}
	
	public void removeProduct( ServerTransaction transaction , ServerProduct product ) throws Exception {
		mapProducts.remove( product.NAME );
	}

	public boolean isOffline() {
		return( OFFLINE );
	}
	
	public boolean isBroken( ActionBase action ) {
		for( ServerProduct product : mapProducts.values() ) {
			if( product.isBroken( action ) )
				return( true );
		}
		return( false );
	}

	public void save( Document doc , Element root ) throws Exception {
		Common.xmlSetElementAttr( doc , root , "name" , NAME );
		Common.xmlSetElementAttr( doc , root , "desc" , DESC );
		Common.xmlSetElementAttr( doc , root , "offline" , Common.getBooleanValue( OFFLINE ) );
		
		for( String productName : getProducts() ) {
			ServerProduct product = getProduct( productName );
			Element elementProduct = Common.xmlCreateElement( doc , root , "product" );
			product.save( doc , elementProduct );
		}
	}
	
}
