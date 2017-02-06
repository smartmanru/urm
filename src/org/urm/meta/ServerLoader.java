package org.urm.meta;

import java.util.HashMap;
import java.util.Map;

import org.urm.action.ActionBase;
import org.urm.common.Common;
import org.urm.common.PropertySet;
import org.urm.engine.ServerEngine;
import org.urm.engine.ServerSession;
import org.urm.engine.TransactionBase;
import org.urm.engine.action.ActionInit;
import org.urm.engine.storage.LocalFolder;
import org.urm.engine.storage.MetadataStorage;
import org.urm.meta.engine.ServerBase;
import org.urm.meta.engine.ServerBuilders;
import org.urm.meta.engine.ServerDirectory;
import org.urm.meta.engine.ServerInfrastructure;
import org.urm.meta.engine.ServerMirrors;
import org.urm.meta.engine.ServerMonitoring;
import org.urm.meta.engine.ServerProduct;
import org.urm.meta.engine.ServerRegistry;
import org.urm.meta.engine.ServerReleaseLifecycles;
import org.urm.meta.engine.ServerResources;
import org.urm.meta.engine.ServerSettings;
import org.urm.meta.product.Meta;
import org.urm.meta.product.MetaDatabase;
import org.urm.meta.product.MetaDesign;
import org.urm.meta.product.MetaDistr;
import org.urm.meta.product.MetaEnv;
import org.urm.meta.product.MetaMonitoring;
import org.urm.meta.product.MetaProductSettings;
import org.urm.meta.product.MetaProductVersion;
import org.urm.meta.product.MetaSource;

public class ServerLoader {

	public ServerEngine engine;
	
	private ServerSettings settings;
	private ServerRegistry registry;
	private ServerBase base;
	private ServerInfrastructure infra;
	private ServerReleaseLifecycles lifecycles;
	private ServerMonitoring mon;
	private ServerProductMeta offline;
	private Map<String,ServerProductMeta> productMeta;
	
	public ServerLoader( ServerEngine engine ) {
		this.engine = engine;
		
		settings = new ServerSettings( this );
		registry = new ServerRegistry( this ); 
		base = new ServerBase( this ); 
		infra = new ServerInfrastructure( this ); 
		lifecycles = new ServerReleaseLifecycles( this ); 
		mon = new ServerMonitoring( this ); 
		productMeta = new HashMap<String,ServerProductMeta>();
	}
	
	public void init() throws Exception {
		loadRegistry();
		loadBase();
		loadInfrastructure();
		loadReleaseLifecycles();
		
		if( !engine.execrc.isStandalone() ) {
			loadServerSettings();
			loadMonitoring();
		}
	}
	
	public void reloadCore() throws Exception {
		registry = new ServerRegistry( this ); 
		base = new ServerBase( this ); 
		settings = new ServerSettings( this );
		infra = new ServerInfrastructure( this ); 
		lifecycles = new ServerReleaseLifecycles( this ); 
		mon = new ServerMonitoring( this ); 
		init();
	}

	public void reloadProduct( String productName ) throws Exception {
		ServerProductMeta storageNew = loadServerProduct( engine.serverAction , productName );
		if( storageNew == null )
			return;
		
		ServerProductMeta storage = productMeta.get( productName );
		synchronized( this ) {
			if( storage != null )
				clearServerProduct( storage );
			addServerProduct( storageNew );
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
		ServerProductMeta set = productMeta.get( productName );
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

	private void loadRegistry() throws Exception {
		String propertyFile = getServerRegistryFile();
		registry.load( propertyFile , engine.execrc );
	}

	private String getServerSettingsFile() {
		String path = Common.getPath( engine.execrc.installPath , "etc" );
		String propertyFile = Common.getPath( path , "server.xml" );
		return( propertyFile );
	}
	
	public void loadServerSettings() throws Exception {
		String propertyFile = getServerSettingsFile();
		settings.load( propertyFile , engine.execrc );
	}

	public synchronized boolean isProductBroken( String productName ) {
		ServerProductMeta storage = productMeta.get( productName );
		if( storage == null )
			return( true );
		if( storage.loadFailed )
			return( true );
		return( false );
	}

	public synchronized Meta findSessionProductMetadata( ActionBase action , String productName ) throws Exception {
		ServerSession session = action.session;
		Meta meta = session.findMeta( productName );
		if( meta != null )
			return( meta );
		
		ServerProductMeta storage = productMeta.get( productName );
		if( storage == null )
			return( null );
		
		meta = new Meta( storage , session );
		engine.trace( "new conf session meta object, id=" + meta.objectId + ", session=" + session.objectId );
		storage.addSessionMeta( meta );
		session.addProductMeta( meta );
		return( meta );
	}
	
	public synchronized ServerProductMeta findProductStorage( String productName ) {
		return( productMeta.get( productName ) );
	}
	
	public synchronized Meta getSessionProductMetadata( ActionBase action , String productName , boolean primary ) throws Exception {
		ServerSession session = action.session;
		Meta meta = session.findMeta( productName );
		if( meta != null ) {
			if( primary ) {
				ServerProductMeta storage = meta.getStorage( action );
				if( !storage.isPrimary() ) {
					ServerProductMeta storageNew = getMetaStorage( action , session , productName );
					meta.replaceStorage( action , storageNew );
				}
			}
			return( meta );
		}
		
		ServerProductMeta storage = getMetaStorage( action , session , productName );
		meta = createSessionProductMetadata( action , storage );
		return( meta );
	}

	public synchronized Meta createSessionProductMetadata( ActionBase action , ServerProductMeta storage ) throws Exception {
		ServerSession session = action.session;
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
		ServerSession session = action.session;
		session.releaseProductMeta( meta );
		ServerProductMeta storage = meta.getStorage( action );
		storage.releaseSessionMeta( meta );
		
		if( storage.isReferencedBySessions() == false && storage.isPrimary() == false ) {
			storage.meta.deleteObject();
			storage.deleteObject();
		}
		
		meta.deleteObject();
	}
	
	private ServerProductMeta getMetaStorage( ActionBase action , ServerSession session , String productName ) throws Exception {
		if( session.offline ) {
			if( offline == null )
				offline = new ServerProductMeta( this , session.productName );
			return( offline );
		}
		
		ServerProductMeta storage = productMeta.get( productName );
		if( storage == null )
			action.exit1( _Error.UnknownSessionProduct1 , "unknown product=" + session.productName , session.productName );
		
		return( storage );
	}

	public MetaProductVersion loadVersion( ActionInit action , ServerProductMeta storageFinal ) throws Exception {
		MetadataStorage storageMeta = action.artefactory.getMetadataStorage( action , storageFinal.meta );
		return( storageFinal.loadVersion( action , storageMeta ) );
	}

	public MetaProductSettings loadProduct( ActionInit action , ServerProductMeta storageFinal ) throws Exception {
		MetadataStorage storageMeta = action.artefactory.getMetadataStorage( action , storageFinal.meta );
		return( storageFinal.loadProduct( action , storageMeta ) );
	}

	public MetaDistr loadDistr( ActionInit action , ServerProductMeta storageFinal ) throws Exception {
		MetadataStorage storageMeta = action.artefactory.getMetadataStorage( action , storageFinal.meta );
		return( storageFinal.loadDistr( action , storageMeta ) );
	}
	
	public MetaDatabase loadDatabase( ActionInit action , ServerProductMeta storageFinal ) throws Exception {
		MetadataStorage storageMeta = action.artefactory.getMetadataStorage( action , storageFinal.meta );
		return( storageFinal.loadDatabase( action , storageMeta ) );
	}
	
	public MetaSource loadSources( ActionInit action , ServerProductMeta storageFinal ) throws Exception {
		MetadataStorage storageMeta = action.artefactory.getMetadataStorage( action , storageFinal.meta );
		return( storageFinal.loadSources( action , storageMeta ) );
	}
	
	public MetaMonitoring loadMonitoring( ActionInit action , ServerProductMeta storageFinal ) throws Exception {
		MetadataStorage storageMeta = action.artefactory.getMetadataStorage( action , storageFinal.meta );
		return( storageFinal.loadMonitoring( action , storageMeta ) );
	}

	public MetaEnv loadEnvData( ActionInit action , ServerProductMeta storageFinal , String envFile , boolean loadProps ) throws Exception {
		MetadataStorage storageMeta = action.artefactory.getMetadataStorage( action , storageFinal.meta );
		MetaEnv env = storageFinal.loadEnvData( action , storageMeta , envFile );
		if( loadProps && env.missingSecretProperties )
			action.exit0( _Error.MissingSecretProperties0 , "operation is unavailable - secret properties are missing" );
		return( env );
	}
	
	public MetaDesign loadDesignData( ActionInit action , ServerProductMeta storageFinal , String fileName ) throws Exception {
		MetadataStorage storageMeta = action.artefactory.getMetadataStorage( action , storageFinal.meta );
		return( storageFinal.loadDesignData( action , storageMeta , fileName ) );
	}

	public void loadServerProducts( ActionInit action ) {
		clearServerProducts();
		for( String name : registry.directory.getProducts() ) {
			ServerProductMeta set = loadServerProduct( action , name );
			addServerProduct( set );
		}
	}

	private void addServerProduct( ServerProductMeta set ) {
		productMeta.put( set.name , set );
	}
	
	private ServerProductMeta loadServerProduct( ActionInit action , String name ) {
		ServerProductMeta set = new ServerProductMeta( this , name );
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
		for( ServerProductMeta storage : productMeta.values() )
			clearServerProduct( storage );
		productMeta.clear();
	}
	
	private void clearServerProduct( ServerProductMeta storage ) {
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
	
	public void setResources( TransactionBase transaction , ServerResources resourcesNew ) throws Exception {
		registry.setResources( transaction , resourcesNew );
	}

	public void setBuilders( TransactionBase transaction , ServerBuilders buildersNew ) throws Exception {
		registry.setBuilders( transaction , buildersNew );
	}

	public void setDirectory( TransactionBase transaction , ServerDirectory directoryNew ) throws Exception {
		registry.setDirectory( transaction , directoryNew );
	}

	public void setMirrors( TransactionBase transaction , ServerMirrors mirrorsNew ) throws Exception {
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

	public ServerSettings getServerSettings() {
		synchronized( engine ) {
			return( settings );
		}
	}

	public ServerRegistry getRegistry() {
		synchronized( engine ) {
			return( registry );
		}
	}

	public ServerInfrastructure getInfrastructure() {
		synchronized( engine ) {
			return( infra );
		}
	}

	public ServerReleaseLifecycles getReleaseLifecycles() {
		synchronized( engine ) {
			return( lifecycles );
		}
	}

	public ServerMonitoring getMonitoring() {
		synchronized( engine ) {
			return( mon );
		}
	}

	public ServerBase getServerBase() {
		synchronized( engine ) {
			return( base );
		}
	}

	public void setServerSettings( TransactionBase transaction , ServerSettings settingsNew ) throws Exception {
		String propertyFile = getServerSettingsFile();
		settingsNew.save( propertyFile , engine.execrc );
		settings = settingsNew;
	}

	public ServerProductMeta createProductMetadata( TransactionBase transaction , ServerDirectory directoryNew , ServerProduct product ) throws Exception {
		ActionInit action = transaction.getAction();
		
		ServerProductMeta set = new ServerProductMeta( this , product.NAME );
		ServerSettings settings = action.getServerSettings();
		set.createInitial( transaction , settings , directoryNew );
		
		return( set );
	}
	
	public void setProductMetadata( TransactionBase transaction , ServerProductMeta storageNew ) throws Exception {
		ActionBase action = transaction.getAction();
		
		MetadataStorage ms = action.artefactory.getMetadataStorage( action , storageNew.meta );
		storageNew.saveAll( action , ms );
		
		ServerProductMeta storageOld = productMeta.get( storageNew.name );
		if( storageOld != null )
			storageOld.setPrimary( false );
		productMeta.put( storageNew.name , storageNew );
		storageNew.setPrimary( true );
	}
	
	public void deleteProductMetadata( TransactionBase transaction , ServerProductMeta storage ) throws Exception {
		productMeta.remove( storage.name );
	}
	
}
