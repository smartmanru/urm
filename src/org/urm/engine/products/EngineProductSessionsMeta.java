package org.urm.engine.products;

import java.util.HashMap;
import java.util.Map;

import org.urm.engine.session.EngineSession;
import org.urm.meta.product.Meta;
import org.urm.meta.product.ProductMeta;

public class EngineProductSessionsMeta {

	EngineSession session;
	
	private Map<Integer,Meta> mapMeta;
	
	public EngineProductSessionsMeta( EngineSession session ) {
		this.session = session;
		
		mapMeta = new HashMap<Integer,Meta>();
	}

	public void addSessionMeta( Meta meta ) {
		mapMeta.put( meta.getId() , meta );
	}

	public void releaseSessionMeta( Meta meta ) {
		mapMeta.remove( meta.getId() );
	}

	public Meta findSessionMeta( ProductMeta storage ) {
		return( mapMeta.get( storage.ID ) );
	}

	public boolean isReferencedBySessions( ProductMeta storage ) {
		Meta meta = mapMeta.get( storage.ID );
		if( meta == null )
			return( false );
		if( meta.getStorage() == storage )
			return( true );
		return( false );
	}
	
}
