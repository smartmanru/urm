package org.urm.meta;

import java.util.HashMap;
import java.util.Map;

import org.urm.action.ActionBase;
import org.urm.common.Common;
import org.urm.db.DBConnection;
import org.urm.db.core.DBCoreData;
import org.urm.db.system.DBSystemData;
import org.urm.engine.Engine;
import org.urm.engine.EngineDB;
import org.urm.engine.EngineSession;
import org.urm.engine.TransactionBase;
import org.urm.engine.action.ActionInit;
import org.urm.engine.properties.PropertySet;
import org.urm.engine.storage.LocalFolder;
import org.urm.engine.storage.MetadataStorage;
import org.urm.meta.engine.EngineBase;
import org.urm.meta.engine.EngineBuilders;
import org.urm.meta.engine.EngineDirectory;
import org.urm.meta.engine.EngineInfrastructure;
import org.urm.meta.engine.MirrorRepository;
import org.urm.meta.engine.EngineMirrors;
import org.urm.meta.engine.EngineMonitoring;
import org.urm.meta.engine.Product;
import org.urm.meta.engine.EngineRegistry;
import org.urm.meta.engine.EngineReleaseLifecycles;
import org.urm.meta.engine.EngineResources;
import org.urm.meta.engine.EngineSettings;
import org.urm.meta.product.Meta;
import org.urm.meta.product.MetaDatabase;
import org.urm.meta.product.MetaDesign;
import org.urm.meta.product.MetaDistr;
import org.urm.meta.product.MetaEnv;
import org.urm.meta.product.MetaMonitoring;
import org.urm.meta.product.MetaProductSettings;
import org.urm.meta.product.MetaProductVersion;
import org.urm.meta.product.MetaSource;

public class EngineLoader {

	public Engine engine;
	
	private EngineDB db;
	private EngineSettings settings;
	private EngineRegistry registry;
	private EngineBase base;
	private EngineInfrastructure infra;
	private EngineReleaseLifecycles lifecycles;
	private EngineMonitoring mon;
	private ProductMeta offline;
	private Map<String,ProductMeta> productMeta;

	public int CV;
	
	public EngineLoader( Engine engine ) {
		this.engine = engine;
		
		db = new EngineDB( this );
		settings = new EngineSettings( this );
		registry = new EngineRegistry( this ); 
		base = new EngineBase( this ); 
		infra = new EngineInfrastructure( this ); 
		lifecycles = new EngineReleaseLifecycles( this ); 
		mon = new EngineMonitoring( this ); 
		productMeta = new HashMap<String,ProductMeta>();
	}

	public void init() throws Exception {
		db.init();
		init( false );
	}
	
	private void init( boolean savedb ) throws Exception {
		DBConnection connection = null;
		try {
			connection = db.getConnection( engine.serverAction );
			CV = DBCoreData.getCurrentCoreVersion( connection );
			
			if( savedb ) {
				CV = CV + 1;
				DBCoreData.setNextCoreVersion( connection , CV );
			}
			
			loadServerSettings( connection , savedb );
			loadRegistry( connection , savedb );
			loadBase();
			loadInfrastructure();
			loadReleaseLifecycles();
			loadMonitoring();
			
			if( savedb ) {
				connection.close( true );
				engine.trace( "successfully saved server metadata version=" + CV );
			}
		}
		catch( Throwable e ) {
			engine.log( "init" , e );
			if( savedb ) {
				connection.close( false );
				engine.trace( "unable to save server metadata version=" + CV );
			}
			Common.exitUnexpected();
		}
	}
	
	public LocalFolder getServerHomeFolder( ActionInit action ) throws Exception {
		LocalFolder folder = action.getLocalFolder( engine.execrc.installPath );
		return( folder );
	}

	public LocalFolder getServerSettingsFolder( ActionInit action ) throws Exception {
		String path = Common.getPath( engine.execrc.installPath , "etc" );
		LocalFolder folder = action.getLocalFolder( path );
		return( folder );
	}

	public LocalFolder getProductHomeFolder( ActionInit action , String productName ) throws Exception {
		ProductMeta set = productMeta.get( productName );
		if( set == null )
			return( null );
		MetadataStorage storageMeta = action.artefactory.getMetadataStorage( action , set.meta );
		LocalFolder folder = storageMeta.getHomeFolder( action );
		return( folder );
	}

	private String getServerBaseFile() throws Exception {
		String path = Common.getPath( engine.execrc.installPath , "etc" );
		String propertyFile = Common.getPath( path , "base.xml" );
		return( propertyFile );
	}

	private void loadBase() throws Exception {
		String baseFile = getServerBaseFile();
		base.load( baseFile , engine.execrc );
	}

	private String getServerInfrastructureFile() throws Exception {
		String path = Common.getPath( engine.execrc.installPath , "etc" );
		String propertyFile = Common.getPath( path , "networks.xml" );
		return( propertyFile );
	}

	private String getServerReleaseLifecyclesFile() throws Exception {
		String path = Common.getPath( engine.execrc.installPath , "etc" );
		String propertyFile = Common.getPath( path , "lifecycles.xml" );
		return( propertyFile );
	}

	private void loadInfrastructure() throws Exception {
		String infraFile = getServerInfrastructureFile();
		infra.load( infraFile , engine.execrc );
	}

	private void loadReleaseLifecycles() throws Exception {
		String lcFile = getServerReleaseLifecyclesFile();
		lifecycles.load( lcFile , engine.execrc );
	}

	private String getServerMonitoringFile() throws Exception {
		String path = Common.getPath( engine.execrc.installPath , "etc" );
		String propertyFile = Common.getPath( path , "monitoring.xml" );
		return( propertyFile );
	}

	private void loadMonitoring() throws Exception {
		String monFile = getServerMonitoringFile();
		mon.load( monFile , engine.execrc );
	}

	private String getServerRegistryFile() throws Exception {
		String path = Common.getPath( engine.execrc.installPath , "etc" );
		String propertyFile = Common.getPath( path , "registry.xml" );
		return( propertyFile );
	}

	private void loadRegistry( DBConnection c , boolean savedb ) throws Exception {
		String registryFile = getServerRegistryFile();
		registry.load( registryFile , c , savedb );
	}

	private String getServerSettingsFile() {
		String path = Common.getPath( engine.execrc.installPath , "etc" );
		String propertyFile = Common.getPath( path , "server.xml" );
		return( propertyFile );
	}
	
	public void loadServerSettings( DBConnection c , boolean savedb ) throws Exception {
		String propertyFile = getServerSettingsFile();
		settings.load( propertyFile , c , savedb );
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

	public synchronized void releaseSessionProductMetadata( ActionBase action , Meta meta ) throws Exception {
		releaseSessionProductMetadata( action , meta , false );
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

	public void loadServerProducts( ActionInit action ) {
		clearServerProducts();
		for( String name : registry.directory.getProducts() ) {
			ProductMeta set = loadServerProduct( action , name , false );
			addServerProduct( set );
		}
	}

	private void addServerProduct( ProductMeta set ) {
		productMeta.put( set.name , set );
	}
	
	private ProductMeta loadServerProduct( ActionInit action , String name , boolean savedb ) {
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
	
	public void clearServerProducts() {
		for( ProductMeta storage : productMeta.values() )
			clearServerProduct( storage );
		productMeta.clear();
	}
	
	private void clearServerProduct( ProductMeta storage ) {
		storage.setPrimary( false );
		if( !storage.isReferencedBySessions() ) {
			storage.meta.deleteObject();
			storage.deleteObject();
		}
	}
	
	public void setProductProps( ActionInit action , PropertySet props ) throws Exception {
		props.copyOriginalPropertiesToRaw( settings.getDefaultProductProperties() );
		for( PropertySet set : settings.getBuildModeDefaults() )
			props.copyOriginalPropertiesToRaw( set );
		props.resolveRawProperties();
	}

	public void saveRegistry( TransactionBase transaction ) throws Exception {
		String propertyFile = getServerRegistryFile();
		registry.save( transaction.getAction() , propertyFile , engine.execrc );
	}
	
	public void setResources( TransactionBase transaction , EngineResources resourcesNew ) throws Exception {
		registry.setResources( transaction , resourcesNew );
	}

	public void setBuilders( TransactionBase transaction , EngineBuilders buildersNew ) throws Exception {
		registry.setBuilders( transaction , buildersNew );
	}

	public void setDirectory( TransactionBase transaction , EngineDirectory directoryNew ) throws Exception {
		registry.setDirectory( transaction , directoryNew );
	}

	public void setMirrors( TransactionBase transaction , EngineMirrors mirrorsNew ) throws Exception {
		registry.setMirrors( transaction , mirrorsNew );
	}

	public void saveBase( TransactionBase transaction ) throws Exception {
		String propertyFile = getServerBaseFile();
		base.save( transaction.getAction() , propertyFile , engine.execrc );
	}

	public void saveInfrastructure( TransactionBase transaction ) throws Exception {
		String propertyFile = getServerInfrastructureFile();
		infra.save( transaction.getAction() , propertyFile , engine.execrc );
	}

	public void saveReleaseLifecycles( TransactionBase transaction ) throws Exception {
		String propertyFile = getServerReleaseLifecyclesFile();
		lifecycles.save( transaction.getAction() , propertyFile , engine.execrc );
	}

	public void saveMonitoring( TransactionBase transaction ) throws Exception {
		String propertyFile = getServerMonitoringFile();
		mon.save( transaction.getAction() , propertyFile , engine.execrc );
	}

	public EngineSettings getServerSettings() {
		synchronized( engine ) {
			return( settings );
		}
	}

	public EngineRegistry getRegistry() {
		synchronized( engine ) {
			return( registry );
		}
	}

	public EngineInfrastructure getInfrastructure() {
		synchronized( engine ) {
			return( infra );
		}
	}

	public EngineReleaseLifecycles getReleaseLifecycles() {
		synchronized( engine ) {
			return( lifecycles );
		}
	}

	public EngineMonitoring getMonitoring() {
		synchronized( engine ) {
			return( mon );
		}
	}

	public EngineBase getServerBase() {
		synchronized( engine ) {
			return( base );
		}
	}

	public EngineDB getDatabase() {
		synchronized( engine ) {
			return( db );
		}
	}
	
	public void setServerSettings( TransactionBase transaction , EngineSettings settingsNew ) throws Exception {
		String propertyFile = getServerSettingsFile();
		settingsNew.save( propertyFile , engine.execrc );
		settings = settingsNew;
	}

	public ProductMeta createProductMetadata( TransactionBase transaction , EngineDirectory directoryNew , Product product ) throws Exception {
		ActionInit action = transaction.getAction();
		
		ProductMeta set = new ProductMeta( this , product.NAME );
		EngineSettings settings = action.getServerSettings();
		set.createInitial( transaction , settings , directoryNew );
		
		return( set );
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

	public void rereadMirror( MirrorRepository repo ) throws Exception {
		if( repo.isServer() ) 
			reloadCore();
		else
		if( repo.isProductMeta() )
			reloadProduct( repo.PRODUCT );
	}
	
	private void reloadCore() throws Exception {
		engine.trace( "reload server core settings ..." );
		
		db.init();
		
		clearServer();
		db.clearServer();

		registry = new EngineRegistry( this ); 
		base = new EngineBase( this ); 
		settings = new EngineSettings( this );
		infra = new EngineInfrastructure( this ); 
		lifecycles = new EngineReleaseLifecycles( this ); 
		mon = new EngineMonitoring( this );
		
		init( true );
	}

	private void clearServer() throws Exception {
		registry.deleteObject(); 
		base.deleteObject(); 
		settings.deleteObject();
		infra.deleteObject(); 
		lifecycles.deleteObject(); 
		mon.deleteObject();
		
		DBConnection connection = null;
		try {
			connection = db.getConnection( engine.serverAction );
			DBSystemData.dropSystemData( connection );
			DBCoreData.dropCoreData( connection );
			connection.close( true );
			engine.trace( "successfully deleted current server metadata, version=" + CV );
		}
		catch( Throwable e ) {
			engine.log( "init" , e );
			connection.close( false );
			engine.trace( "unable to delete current server metadata, version=" + CV );
			Common.exitUnexpected();
		}
	}
	
	private void reloadProduct( String productName ) throws Exception {
		engine.trace( "reload settings, product=" + productName + " ..." );
		
		db.init();
		db.clearProduct( productName );
		
		ProductMeta storageNew = loadServerProduct( engine.serverAction , productName , true );
		if( storageNew == null )
			return;
		
		ProductMeta storage = productMeta.get( productName );
		synchronized( this ) {
			if( storage != null )
				clearServerProduct( storage );
			addServerProduct( storageNew );
		}
	}
	
}
