package org.urm.meta.engine;

import java.util.HashMap;
import java.util.Map;

import org.urm.action.ActionBase;
import org.urm.engine.Engine;
import org.urm.engine.EngineSession;
import org.urm.engine.TransactionBase;
import org.urm.engine.properties.PropertySet;
import org.urm.meta._Error;
import org.urm.meta.env.MetaEnv;
import org.urm.meta.product.Meta;
import org.urm.meta.product.ProductMeta;

public class EngineProducts {

	public Engine engine;
	
	private ProductMeta offline;
	private Map<String,ProductMeta> productMeta;
	private Map<String,ProductMeta> productMetaSkipped;
	
	public EngineProducts( Engine engine ) {
		this.engine = engine;
		productMeta = new HashMap<String,ProductMeta>();
		productMetaSkipped = new HashMap<String,ProductMeta>();
	}
	
	private synchronized void addProduct( ProductMeta set ) {
		productMeta.put( set.name , set );
		productMetaSkipped.remove( set.name );
	}
	
	public synchronized void addProductSkipped( ProductMeta set ) {
		productMetaSkipped.put( set.name , set );
		productMeta.remove( set.name );
	}
	
	public synchronized void addEnv( MetaEnv env ) {
	}
	
	public synchronized void addEnvSkipped( MetaEnv env ) {
	}
	
	public synchronized boolean isProductBroken( String productName ) {
		ProductMeta storage = productMeta.get( productName );
		if( storage == null )
			return( true );
		return( false );
	}

	public synchronized Meta findSessionProductMetadata( ActionBase action , String productName ) {
		EngineSession session = action.session;
		Meta meta = session.findMeta( productName );
		if( meta != null )
			return( meta );
		
		ProductMeta storage = productMeta.get( productName );
		if( storage == null )
			return( null );
		
		meta = new Meta( storage , session );
		engine.trace( "new conf session meta object, id=" + meta.objectId + ", session=" + session.objectId );
		storage.addSessionMeta( meta );
		session.addProductMeta( meta );
		return( meta );
	}
	
	public synchronized ProductMeta findProductStorage( String productName ) {
		return( productMeta.get( productName ) );
	}
	
	public synchronized Meta getSessionProductMetadata( ActionBase action , String productName , boolean primary ) throws Exception {
		EngineSession session = action.session;
		Meta meta = session.findMeta( productName );
		if( meta != null ) {
			if( primary ) {
				ProductMeta storage = meta.getStorage();
				if( !storage.isPrimary() ) {
					ProductMeta storageNew = getMetaStorage( action , session , productName );
					meta.replaceStorage( action , storageNew );
				}
			}
			return( meta );
		}
		
		ProductMeta storage = getMetaStorage( action , session , productName );
		meta = createSessionProductMetadata( action , storage );
		return( meta );
	}

	public synchronized Meta createSessionProductMetadata( ActionBase action , ProductMeta storage ) throws Exception {
		EngineSession session = action.session;
		Meta meta = new Meta( storage , session );
		engine.trace( "new run session meta object, id=" + meta.objectId + ", session=" + session.objectId );
		storage.addSessionMeta( meta );
		session.addProductMeta( meta );
		return( meta );
	}

	public synchronized void releaseSessionProductMetadata( ActionBase action , Meta meta ) throws Exception {
		EngineSession session = action.session;
		session.releaseProductMeta( meta );
		ProductMeta storage = meta.getStorage();
		storage.releaseSessionMeta( meta );
		
		if( storage.isReferencedBySessions() == false && storage.isPrimary() == false ) {
			storage.meta.deleteObject();
			storage.deleteObject();
			storage.deleteEnvObjects();
		}
		
		meta.deleteObject();
	}
	
	private ProductMeta getMetaStorage( ActionBase action , EngineSession session , String productName ) throws Exception {
		if( session.offline ) {
			AppProduct product = action.getProduct( session.productName );
			if( offline == null )
				offline = new ProductMeta( this , product );
			return( offline );
		}
		
		ProductMeta storage = productMeta.get( productName );
		if( storage == null )
			action.exit1( _Error.UnknownSessionProduct1 , "unknown product=" + productName , productName );
		
		return( storage );
	}

	public void unloadProducts() {
		for( ProductMeta storage : productMeta.values() )
			unloadProductData( storage );
		productMeta.clear();
		
		for( ProductMeta storage : productMetaSkipped.values() )
			unloadProductData( storage );
		productMetaSkipped.clear();
	}

	public void unloadProduct( ProductMeta storage ) {
		if( productMeta.get( storage.name ) == storage )
			productMeta.remove( storage.name );
		if( productMetaSkipped.get( storage.name ) == storage )
			productMetaSkipped.remove( storage.name );
		unloadProductData( storage );
	}
	
	private void unloadProductData( ProductMeta storage ) {
		storage.setPrimary( false );
		if( !storage.isReferencedBySessions() ) {
			storage.meta.deleteObject();
			storage.deleteObject();
		}
	}
	
	public void setProductProps( ActionBase action , EngineSettings settings , PropertySet props ) throws Exception {
		props.copyOriginalPropertiesToRaw( settings.getDefaultProductProperties() );
		for( PropertySet set : settings.getBuildModeDefaults() )
			props.copyOriginalPropertiesToRaw( set );
		props.resolveRawProperties();
	}

	public void setProductMetadata( ProductMeta storageNew ) {
		ProductMeta storageOld = productMeta.get( storageNew.name );
		if( storageOld != null )
			storageOld.setPrimary( false );
		addProduct( storageNew );
		storageNew.setPrimary( true );
	}
	
	public void deleteProductMetadata( TransactionBase transaction , ProductMeta storage ) throws Exception {
		unloadProduct( storage );
	}

}
