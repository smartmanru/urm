package org.urm.meta.engine;

import java.util.HashMap;
import java.util.Map;

import org.urm.action.ActionBase;
import org.urm.common.Common;
import org.urm.common.ConfReader;
import org.urm.engine.EngineTransaction;
import org.urm.meta.EngineObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class System extends EngineObject {

	public EngineDirectory directory;
	public Map<String,Product> mapProducts;
	
	public int ID;
	public String NAME;
	public String DESC;
	public boolean OFFLINE;
	public int CV;
	
	public System( EngineDirectory directory ) {
		super( directory );
		this.directory = directory;
		mapProducts = new HashMap<String,Product>();
	}

	@Override
	public String getName() {
		return( NAME );
	}
	
	public void createSystem( EngineTransaction transaction , String name , String desc ) {
		this.NAME = name;
		this.DESC = desc;
		this.OFFLINE = true;
	}
	
	public void setOffline( EngineTransaction transaction , boolean OFFLINE ) {
		this.OFFLINE = OFFLINE;
	}
	
	public System copy( EngineDirectory nd ) {
		System r = new System( nd );
		r.NAME = NAME;
		r.DESC = DESC;
		r.OFFLINE = OFFLINE;
		
		for( Product product : mapProducts.values() ) {
			Product rp = product.copy( nd , r );
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
			Product item = new Product( directory , this );
			item.load( itemNode );
			mapProducts.put( item.NAME , item );
		}
	}
	
	public String[] getProductNames() {
		return( Common.getSortedKeys( mapProducts ) );
	}

	public Product[] getProducts() {
		return( mapProducts.values().toArray( new Product[0] ) );
	}

	public Product findProduct( String key ) {
		return( mapProducts.get( key ) );
	}

	public void modifySystem( EngineTransaction transaction , String name , String desc ) throws Exception {
		NAME = name;
		DESC = desc;
	}

	public void addProduct( EngineTransaction transaction , Product product ) throws Exception {
		mapProducts.put( product.NAME , product );
	}
	
	public void removeProduct( EngineTransaction transaction , Product product ) throws Exception {
		mapProducts.remove( product.NAME );
	}

	public boolean isOffline() {
		return( OFFLINE );
	}

	public boolean isBroken( ActionBase action ) {
		for( Product product : mapProducts.values() ) {
			if( product.isBroken( action ) )
				return( true );
		}
		return( false );
	}

	public void save( Document doc , Element root ) throws Exception {
		Common.xmlSetElementAttr( doc , root , "name" , NAME );
		Common.xmlSetElementAttr( doc , root , "desc" , DESC );
		Common.xmlSetElementAttr( doc , root , "offline" , Common.getBooleanValue( OFFLINE ) );
		
		for( String productName : getProductNames() ) {
			Product product = findProduct( productName );
			Element elementProduct = Common.xmlCreateElement( doc , root , "product" );
			product.save( doc , elementProduct );
		}
	}
	
}
