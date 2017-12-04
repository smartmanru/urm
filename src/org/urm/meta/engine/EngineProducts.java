package org.urm.meta.engine;

import java.util.HashMap;
import java.util.Map;

import org.urm.action.ActionBase;
import org.urm.engine.Engine;
import org.urm.engine.EngineDB;
import org.urm.engine.EngineSession;
import org.urm.engine.TransactionBase;
import org.urm.engine.action.ActionInit;
import org.urm.engine.properties.PropertySet;
import org.urm.engine.storage.LocalFolder;
import org.urm.engine.storage.MetadataStorage;
import org.urm.meta.EngineLoader;
import org.urm.meta.ProductMeta;
import org.urm.meta._Error;
import org.urm.meta.product.Meta;
import org.urm.meta.product.MetaDatabase;
import org.urm.meta.product.MetaDesign;
import org.urm.meta.product.MetaDistr;
import org.urm.meta.product.MetaEnv;
import org.urm.meta.product.MetaMonitoring;
import org.urm.meta.product.MetaProductSettings;
import org.urm.meta.product.MetaProductVersion;
import org.urm.meta.product.MetaSource;

public class EngineProducts {

	public Engine engine;
	
	private ProductMeta offline;
	private Map<String,ProductMeta> productMeta;
	
	public EngineProducts( Engine engine ) {
		this.engine = engine;
		productMeta = new HashMap<String,ProductMeta>();
	}
	
	public synchronized boolean isProductBroken( String productName ) {
		ProductMeta storage = productMeta.get( productName );
		if( storage == null )
			return( true );
		if( storage.loadFailed )
			return( true );
		return( false );
	}

	public synchronized Meta findSessionProductMetadata( ActionBase action , String productName ) throws Exception {
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
				ProductMeta storage = meta.getStorage( action );
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

	public synchronized void releaseSessionProductMetadata( ActionBase action , Meta meta , boolean deleteMeta ) throws Exception {
		EngineSession session = action.session;
		session.releaseProductMeta( meta );
		ProductMeta storage = meta.getStorage( action );
		storage.releaseSessionMeta( meta );
		
		if( storage.isReferencedBySessions() == false && storage.isPrimary() == false ) {
			storage.meta.deleteObject();
			storage.deleteObject();
		}
		
		meta.deleteObject();
	}
	
	private ProductMeta getMetaStorage( ActionBase action , EngineSession session , String productName ) throws Exception {
		if( session.offline ) {
			if( offline == null )
				offline = new ProductMeta( this , session.productName );
			return( offline );
		}
		
		ProductMeta storage = productMeta.get( productName );
		if( storage == null )
			action.exit1( _Error.UnknownSessionProduct1 , "unknown product=" + session.productName , session.productName );
		
		return( storage );
	}

	private void addProduct( ProductMeta set ) {
		productMeta.put( set.name , set );
	}

	private ProductMeta loadProduct( ActionBase action , String name , boolean savedb ) {
		ProductMeta set = new ProductMeta( this , name );
		set.setPrimary( true );
		
		try {
			MetadataStorage storageMeta = action.artefactory.getMetadataStorage( action , set.meta );
			LocalFolder folder = storageMeta.getMetaFolder( action );
			if( folder.checkExists( action ) )
				set.loadAll( action , storageMeta );
			else
				set.setLoadFailed( action , "metadata folder is missing, product=" + name );
		}
		catch( Throwable e ) {
			action.handle( e );
			action.error( "unable to load metadata, product=" + name );
		}
		
		return( set );
	}
	
	public void unloadProducts() {
		for( ProductMeta storage : productMeta.values() )
			unloadProduct( storage );
		productMeta.clear();
	}
	
	private void unloadProduct( ProductMeta storage ) {
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

	public void importProduct( EngineLoader loader , String productName , boolean includingEnvironments ) throws Exception {
		engine.trace( "reload settings, product=" + productName + " ..." );
		
		EngineDB db = loader.getDatabase();
		db.clearProduct( productName );
		
		ProductMeta storageNew = loadProduct( engine.serverAction , productName , true );
		if( storageNew == null )
			return;
		
		synchronized( this ) {
			ProductMeta storage = productMeta.get( productName );
			if( storage != null )
				unloadProduct( storage );
			addProduct( storageNew );
		}
	}
	
	public ProductMeta createProductMetadata( TransactionBase transaction , AppProduct product ) throws Exception {
		ActionInit action = transaction.getAction();
		
		ProductMeta set = new ProductMeta( this , product.NAME );
		EngineSettings settings = action.getServerSettings();
		set.createInitial( transaction , settings );
		
		return( set );
	}

	public void saveProductMetadata( EngineLoader loader , String productName ) throws Exception {
		ActionBase action = loader.getAction();
		ProductMeta storage = productMeta.get( productName );
		if( storage == null || storage.loadFailed )
			action.exitUnexpectedState();

		MetadataStorage ms = action.artefactory.getMetadataStorage( action , storage.meta );
		storage.saveAll( action , ms );
	}
	
	public void setProductMetadata( TransactionBase transaction , ProductMeta storageNew ) throws Exception {
		ActionBase action = transaction.getAction();
		MetadataStorage ms = action.artefactory.getMetadataStorage( action , storageNew.meta );
		storageNew.saveAll( action , ms );
		
		ProductMeta storageOld = productMeta.get( storageNew.name );
		if( storageOld != null )
			storageOld.setPrimary( false );
		productMeta.put( storageNew.name , storageNew );
		storageNew.setPrimary( true );
	}
	
	public void deleteProductMetadata( TransactionBase transaction , ProductMeta storage ) throws Exception {
		productMeta.remove( storage.name );
	}

	public void loadProducts( EngineLoader loader ) {
		ActionBase action = loader.getAction();
		unloadProducts();
		EngineDirectory directory = loader.getDirectory();
		for( String name : directory.getProductNames() ) {
			ProductMeta product = loadProduct( action , name , false );
			addProduct( product );
		}
	}

	public MetaProductVersion loadVersion( ActionInit action , ProductMeta storageFinal ) throws Exception {
		MetadataStorage storageMeta = action.artefactory.getMetadataStorage( action , storageFinal.meta );
		return( storageFinal.loadVersion( action , storageMeta ) );
	}

	public MetaProductSettings loadProduct( ActionInit action , ProductMeta storageFinal ) throws Exception {
		MetadataStorage storageMeta = action.artefactory.getMetadataStorage( action , storageFinal.meta );
		return( storageFinal.loadProduct( action , storageMeta ) );
	}

	public MetaDistr loadDistr( ActionInit action , ProductMeta storageFinal ) throws Exception {
		MetadataStorage storageMeta = action.artefactory.getMetadataStorage( action , storageFinal.meta );
		return( storageFinal.loadDistr( action , storageMeta ) );
	}
	
	public MetaDatabase loadDatabase( ActionInit action , ProductMeta storageFinal ) throws Exception {
		MetadataStorage storageMeta = action.artefactory.getMetadataStorage( action , storageFinal.meta );
		return( storageFinal.loadDatabase( action , storageMeta ) );
	}
	
	public MetaSource loadSources( ActionInit action , ProductMeta storageFinal ) throws Exception {
		MetadataStorage storageMeta = action.artefactory.getMetadataStorage( action , storageFinal.meta );
		return( storageFinal.loadSources( action , storageMeta ) );
	}
	
	public MetaMonitoring loadMonitoring( ActionInit action , ProductMeta storageFinal ) throws Exception {
		MetadataStorage storageMeta = action.artefactory.getMetadataStorage( action , storageFinal.meta );
		return( storageFinal.loadMonitoring( action , storageMeta ) );
	}

	public MetaEnv loadEnvData( ActionInit action , ProductMeta storageFinal , String envFile , boolean loadProps ) throws Exception {
		MetadataStorage storageMeta = action.artefactory.getMetadataStorage( action , storageFinal.meta );
		MetaEnv env = storageFinal.loadEnvData( action , storageMeta , envFile );
		if( loadProps && env.missingSecretProperties )
			action.exit0( _Error.MissingSecretProperties0 , "operation is unavailable - secret properties are missing" );
		return( env );
	}
	
	public MetaDesign loadDesignData( ActionInit action , ProductMeta storageFinal , String fileName ) throws Exception {
		MetadataStorage storageMeta = action.artefactory.getMetadataStorage( action , storageFinal.meta );
		return( storageFinal.loadDesignData( action , storageMeta , fileName ) );
	}

}
