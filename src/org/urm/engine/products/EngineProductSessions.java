package org.urm.engine.products;

import java.util.HashMap;
import java.util.Map;

import org.urm.action.ActionBase;
import org.urm.engine.Engine;
import org.urm.engine.session.EngineSession;
import org.urm.meta.product.Meta;
import org.urm.meta.product.ProductMeta;

public class EngineProductSessions {

	public Engine engine;
	private EngineProduct ep;

	private Map<EngineSession,EngineProductSessionsMeta> sessionMeta;
	
	public EngineProductSessions( EngineProduct ep ) {
		this.engine = ep.engine;
		this.ep = ep;
		
		sessionMeta = new HashMap<EngineSession,EngineProductSessionsMeta>();
	}
	
	private synchronized EngineProductSessionsMeta getSessionsMeta( EngineSession session ) {
		EngineProductSessionsMeta sm = sessionMeta.get( session );
		if( sm == null ) {
			sm = new EngineProductSessionsMeta( session );
			sessionMeta.put( session , sm );
		}
		return( sm );
	}
	
	public synchronized void addSessionMeta( Meta meta ) {
		EngineProductSessionsMeta sm = getSessionsMeta( meta.session );
		sm.addSessionMeta( meta );
	}
	
	public synchronized void releaseSessionMeta( Meta meta ) {
		EngineProductSessionsMeta sm = getSessionsMeta( meta.session );
		sm.releaseSessionMeta( meta );
	}

	public synchronized boolean isReferencedBySessions( ProductMeta storage ) {
		for( EngineProductSessionsMeta sm : sessionMeta.values() ) {
			if( sm.isReferencedBySessions( storage ) )
				return( true );
		}
		return( false );
	}
	
	public synchronized Meta createSessionProductMetadata( ActionBase action , ProductMeta storage ) {
		EngineSession session = action.session;
		Meta meta = new Meta( engine , ep , storage , session );
		addSessionMeta( meta );
		session.addProductMeta( meta );
		return( meta );
	}

	public synchronized Meta getSessionProductMetadata( ActionBase action , ProductMeta storage , boolean primary ) throws Exception {
		EngineProductRevisions revisions = ep.getRevisions();
		EngineProductSessionsMeta sm = getSessionsMeta( action.session );
		Meta meta = sm.findSessionMeta( storage );
		
		if( meta != null ) {
			if( primary ) {
				storage = meta.getStorage();
				if( !storage.isPrimary() ) {
					ProductMeta storageNew = revisions.getRevision( meta.getId() );
					replaceStorage( action , meta , storageNew );
				}
			}
			return( meta );
		}
		
		storage = revisions.getRevision( storage.ID );
		meta = createSessionProductMetadata( action , storage );
		return( meta );
	}

	public synchronized Meta findSessionMeta( EngineSession session , ProductMeta storage ) {
		EngineProductSessionsMeta sm = getSessionsMeta( session );
		return( sm.findSessionMeta( storage ) );
	}

	public synchronized Meta findSessionProductMetadata( ActionBase action , ProductMeta storage , boolean create ) {
		EngineSession session = action.session;
		Meta meta = findSessionMeta( session , storage );
		if( meta != null )
			return( meta );
		
		if( !create )
			return( null );
		
		if( !storage.isPrimary() )
			return( null );
		
		meta = new Meta( engine , ep , storage , session );
		
		addSessionMeta( meta );
		session.addProductMeta( meta );
		return( meta );
	}
	
	public synchronized void releaseSessionProductMetadata( ActionBase action , Meta meta ) throws Exception {
		EngineSession session = action.session;
		session.releaseProductMeta( meta );
		ProductMeta storage = meta.getStorage();
		releaseSessionMeta( meta );
		
		if( isReferencedBySessions( storage ) == false && storage.isPrimary() == false ) {
			storage.meta.deleteObject();
			storage.deleteObject();
			storage.deleteEnvObjects();
		}
		
		meta.deleteObject();
	}
	
	public void replaceStorage( ActionBase action , Meta meta , ProductMeta storage ) throws Exception {
		releaseSessionProductMetadata( action , meta );
		
		// clear old refs
		meta.setStorage( storage );
		addSessionMeta( meta );
		
		EngineSession session = action.session;
		session.addProductMeta( meta );
	}

}
