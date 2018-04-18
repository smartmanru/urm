package org.urm.engine.data;

import java.util.HashMap;
import java.util.Map;

import org.urm.action.ActionBase;
import org.urm.common.Common;
import org.urm.engine.DataService;
import org.urm.engine.Engine;
import org.urm.engine.products.EngineProduct;
import org.urm.engine.products.EngineProductRevisions;
import org.urm.engine.transaction.TransactionBase;
import org.urm.meta.engine.AppProduct;
import org.urm.meta.product.Meta;
import org.urm.meta.product.ProductMeta;

public class EngineProducts {

	public Engine engine;
	private DataService data;
	
	Map<String,EngineProduct> products;
	Map<Integer,EngineProduct> productsById;
	
	public EngineProducts( Engine engine , DataService data ) {
		this.engine = engine;
		this.data = data;
		
		products = new HashMap<String,EngineProduct>();
		productsById = new HashMap<Integer,EngineProduct>();
	}

	private EngineProduct findEngineProduct( String productName ) {
		AppProduct product = findProduct( productName );
		return( findEngineProduct( product ) );
	}

	private EngineProduct getEngineProduct( ProductMeta storage ) throws Exception {
		EngineProduct ep = findEngineProduct( storage );
		if( ep == null )
			Common.exitUnexpected();
		return( ep );
	}
	
	private EngineProduct findEngineProduct( ProductMeta storage ) {
		AppProduct product = findProduct( storage.NAME );
		return( findEngineProduct( product ) );
	}

	public EngineProduct getEngineProduct( AppProduct product ) throws Exception {
		EngineProduct ep = findEngineProduct( product );
		if( ep == null )
			Common.exitUnexpected();
		return( ep );
	}
	
	public synchronized EngineProduct findEngineProduct( AppProduct product ) {
		EngineProduct ep = products.get( product.ID );
		if( ep == null ) {
			ep = new EngineProduct( data , this , product );
			products.put( product.NAME , ep );
			productsById.put( product.ID , ep );
		}
		return( ep );
	}
	
	public void addProductMeta( ProductMeta storage ) throws Exception {
		EngineProduct ep = findEngineProduct( storage );
		ep.addProductMeta( storage );
	}
	
	public synchronized void addProductSkipped( ProductMeta storage ) throws Exception {
		EngineProduct ep = getEngineProduct( storage );
		ep.addProductMeta( storage );
		ep.setSkipped();
	}
	
	public AppProduct getProduct( int id ) throws Exception {
		EngineDirectory directory = data.getDirectory();
		return( directory.getProduct( id ) );
	}
	
	public AppProduct findProduct( int id ) {
		EngineDirectory directory = data.getDirectory();
		return( directory.findProduct( id ) );
	}
	
	public AppProduct findProduct( String name ) {
		EngineDirectory directory = data.getDirectory();
		return( directory.findProduct( name ) );
	}
	
	public ProductMeta findProductRevision( String productName , String revision ) {
		EngineProduct ep = findEngineProduct( productName );
		return( ep.findRevision( revision ) );
	}
	
	public synchronized void unloadProducts() {
		for( EngineProduct ep : products.values() )
			ep.unloadProduct();
	}

	public synchronized void unloadProduct( ProductMeta storage ) {
		EngineProduct ep = findEngineProduct( storage );
		ep.unloadProduct( storage );
	}
	
	public void setProductDraft( AppProduct product , ProductMeta storage ) {
		EngineProduct ep = findEngineProduct( product );
		ep.setDraftRevision( storage );
	}
	
	public void deleteProductMetadata( TransactionBase transaction , ProductMeta storage ) throws Exception {
		unloadProduct( storage );
	}

	public Meta reloadProductMetadata( ActionBase action , Meta meta ) throws Exception {
		EngineProduct ep = meta.getEngineProduct();
		return( ep.getSessionMeta( action , meta.getStorage() , true ) );
	}

	public Meta findProductMetadata( ActionBase action , int metaId ) {
		for( EngineProduct ep : products.values() ) {
			EngineProductRevisions revisions = ep.getRevisions();
			ProductMeta storage = revisions.findRevision( metaId );
			if( storage != null )
				return( ep.findSessionMeta( action , storage , true ) );
		}
		return( null );
	}
	
}
