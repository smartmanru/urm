package org.urm.engine.products;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.urm.common.Common;
import org.urm.engine.Engine;
import org.urm.meta.product.ProductMeta;

public class EngineProductRevisions {

	public Engine engine;
	public EngineProduct ep;

	private ProductMeta draft;
	private Map<Integer,ProductMeta> productMetaById;

	public EngineProductRevisions( EngineProduct ep ) {
		this.engine = ep.engine;
		this.ep = ep;
		productMetaById = new HashMap<Integer,ProductMeta>();
	}

	public synchronized ProductMeta findRevision( int id ) {
		return( productMetaById.get( id ) );
	}
	
	public synchronized ProductMeta getRevision( int id ) throws Exception {
		ProductMeta storage = productMetaById.get( id );
		if( storage == null )
			Common.exitUnexpected();
		return( storage );
	}
	
	public synchronized void addProductMeta( ProductMeta storage ) {
		if( storage.DRAFT )
			draft = storage;
		productMetaById.put( storage.ID , storage );
	}

	public synchronized void unloadProduct( ProductMeta storage ) {
		if( productMetaById.get( storage.ID ) == storage )
			productMetaById.remove( storage.ID );
	}

	public synchronized ProductMeta findRevision( String name ) {
		for( ProductMeta storage : productMetaById.values() ) {
			if( name.equals( storage.REVISION ) )
				return( storage );
		}
		return( null );
	}

	public synchronized String[] getRevisionNames() {
		List<String> revisions = new LinkedList<String>();
		for( ProductMeta storage : productMetaById.values() )
			revisions.add( storage.REVISION );
		return( Common.getSortedList( revisions ) );
	}
	
	public ProductMeta getDraftRevision() {
		return( draft );
	}

	public synchronized ProductMeta[] getRevisions() {
		return( productMetaById.values().toArray( new ProductMeta[0] ) );
	}
	
	public synchronized void setDraftRevision( ProductMeta storage ) {
		ProductMeta storageOld = productMetaById.get( storage.ID );
		if( storageOld != null )
			storageOld.setPrimary( false );
		
		addProductMeta( storage );
		storage.setPrimary( true );
	}
	
}