package org.urm.engine;

import java.util.HashMap;
import java.util.Map;

import org.urm.action.ActionBase;
import org.urm.common.Common;
import org.urm.common.PropertySet;
import org.urm.engine.action.ActionInit;
import org.urm.engine.meta.Meta;
import org.urm.engine.meta.MetaDatabase;
import org.urm.engine.meta.MetaDesign;
import org.urm.engine.meta.MetaDistr;
import org.urm.engine.meta.MetaEnv;
import org.urm.engine.meta.MetaMonitoring;
import org.urm.engine.meta.MetaProductSettings;
import org.urm.engine.meta.MetaProductVersion;
import org.urm.engine.meta.MetaSource;
import org.urm.engine.registry.ServerBuilders;
import org.urm.engine.registry.ServerDirectory;
import org.urm.engine.registry.ServerMirrors;
import org.urm.engine.registry.ServerProduct;
import org.urm.engine.registry.ServerRegistry;
import org.urm.engine.registry.ServerResources;
import org.urm.engine.storage.LocalFolder;
import org.urm.engine.storage.MetadataStorage;

public class ServerLoader {

	public ServerEngine engine;
	
	private ServerRegistry registry;
	private ServerSettings settings;
	private ServerProductMeta offline;
	private Map<String,ServerProductMeta> productMeta;
	
	public ServerLoader( ServerEngine engine ) {
		this.engine = engine;
		
		registry = new ServerRegistry( this ); 
		settings = new ServerSettings( this ); 
		productMeta = new HashMap<String,ServerProductMeta>();
	}
	
	public void init() throws Exception {
		loadRegistry();
		if( !engine.execrc.isStandalone() )
			loadServerSettings();
	}
	
	public void reload() throws Exception {
		registry = new ServerRegistry( this ); 
		settings = new ServerSettings( this );
		init();
	}
	
	public LocalFolder getServerSettingsFolder( ActionBase action ) throws Exception {
		String path = Common.getPath( engine.execrc.installPath , "etc" );
		LocalFolder folder = action.getLocalFolder( path );
		return( folder );
	}

	public LocalFolder getProductHomeFolder( ActionBase action , String productName ) throws Exception {
		ServerProductMeta set = productMeta.get( productName );
		if( set == null )
			return( null );
		MetadataStorage storageMeta = action.artefactory.getMetadataStorage( action , set.meta );
		LocalFolder folder = storageMeta.getHomeFolder( action );
		return( folder );
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

	public synchronized Meta getMetadata( ActionBase action , String productName ) throws Exception {
		ServerSession session = action.session;
		Meta meta = session.findMeta( productName );
		if( meta != null )
			return( meta );
		
		ServerProductMeta storage = getMetaStorage( action , session , productName );
		meta = new Meta( storage , session );
		storage.addSessionMeta( meta );
		session.addProductMeta( meta );
		return( meta );
	}

	public synchronized void releaseMetadata( ActionBase action , Meta meta ) throws Exception {
		ServerSession session = action.session;
		session.releaseProductMeta( meta );
		ServerProductMeta storage = meta.getStorage( action );
		storage.releaseSessionMeta( meta );
		
		if( storage.isReferencedBySessions() && !storage.isPrimary() )
			storage.deleteObject();
		
		meta.deleteObject();
	}
	
	private ServerProductMeta getMetaStorage( ActionBase action , ServerSession session , String productName ) throws Exception {
		if( session.offline ) {
			if( offline == null )
				offline = new ServerProductMeta( this , session.productName , action.session );
			return( offline );
		}
		
		ServerProductMeta storage = productMeta.get( productName );
		if( storage == null )
			action.exit1( _Error.UnknownSessionProduct1 , "unknown product=" + session.productName , session.productName );
		
		if( storage.loadFailed )
			action.exit1( _Error.UnusableProductMetadata1 , "unusable metadata of product=" + session.productName , session.productName );
		
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
		for( String name : registry.directory.getProducts() ) {
			
			ServerProductMeta set = new ServerProductMeta( this , name , action.session );
			set.setPrimary( true );
			productMeta.put( name , set );
			
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
		}
	}

	public void clearServerProducts() throws Exception {
		productMeta.clear();
	}
	
	public void setProductProps( ActionInit action , PropertySet props ) throws Exception {
		props.copyOriginalPropertiesToRaw( settings.getDefaultProductProperties() );
		for( PropertySet set : settings.getBuildModeDefaults() )
			props.copyOriginalPropertiesToRaw( set );
		props.resolveRawProperties();
	}

	public ServerMirrors getMirrors() {
		synchronized( engine ) {
			return( registry.mirrors );
		}
	}

	public ServerResources getResources() {
		synchronized( engine ) {
			return( registry.resources );
		}
	}

	public ServerBuilders getBuilders() {
		synchronized( engine ) {
			return( registry.builders );
		}
	}

	public ServerDirectory getDirectory() {
		synchronized( engine ) {
			return( registry.directory );
		}
	}

	public void saveRegistry( TransactionBase transaction ) throws Exception {
		String propertyFile = getServerRegistryFile();
		registry.save( transaction.getAction() , propertyFile , engine.execrc );
	}
	
	public void setResources( TransactionBase transaction , ServerResources resourcesNew ) throws Exception {
		registry.setResources( transaction , resourcesNew );
		saveRegistry( transaction );
	}

	public void setBuilders( TransactionBase transaction , ServerBuilders buildersNew ) throws Exception {
		registry.setBuilders( transaction , buildersNew );
		saveRegistry( transaction );
	}

	public void setDirectory( TransactionBase transaction , ServerDirectory directoryNew ) throws Exception {
		registry.setDirectory( transaction , directoryNew );
		saveRegistry( transaction );
	}

	public void saveMirrors( TransactionBase transaction ) throws Exception {
		saveRegistry( transaction );
	}

	public ServerSettings getSettings() {
		synchronized( engine ) {
			return( settings );
		}
	}

	public ServerRegistry getRegistry() {
		synchronized( engine ) {
			return( registry );
		}
	}

	public void setSettings( TransactionBase transaction , ServerSettings settingsNew ) throws Exception {
		String propertyFile = getServerSettingsFile();
		settingsNew.save( propertyFile , engine.execrc );
		settings = settingsNew;
	}

	public ServerProductMeta createMetadata( TransactionBase transaction , ServerDirectory directoryNew , ServerProduct product ) throws Exception {
		ActionInit action = transaction.getAction();
		
		ServerProductMeta set = new ServerProductMeta( this , product.NAME , action.session );
		ServerSettings settings = transaction.getSettings();
		set.createInitial( action , settings , directoryNew );
		
		return( set );
	}
	
	public void setMetadata( TransactionBase transaction , ServerProductMeta storageNew ) throws Exception {
		ActionInit action = transaction.getAction();
		
		MetadataStorage ms = action.artefactory.getMetadataStorage( action , storageNew.meta );
		storageNew.saveAll( action , ms );
		
		ServerProductMeta storageOld = productMeta.get( storageNew.name );
		if( storageOld != null )
			storageOld.setPrimary( false );
		productMeta.put( storageNew.name , storageNew );
		storageNew.setPrimary( true );
	}
	
	public void deleteMetadata( TransactionBase transaction , ServerProductMeta storage ) throws Exception {
		productMeta.remove( storage.name );
	}
	
}
