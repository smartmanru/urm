package org.urm.engine;

import java.util.HashMap;
import java.util.Map;

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

	public Meta createMetadata( SessionContext session ) throws Exception {
		Meta meta = new Meta( this , session );
		return( meta );
	}

	public synchronized ServerProductMeta findMetaStorage( String productName ) {
		return( productMeta.get( productName ) );
	}
	
	public synchronized ServerProductMeta getMetaStorage( ActionInit action ) throws Exception {
		if( !action.session.product )
			action.exitUnexpectedState();
			
		if( action.session.offline ) {
			if( offline == null )
				offline = new ServerProductMeta( this , action.session.productName , action.session );
			return( offline );
		}
		
		ServerProductMeta storage = productMeta.get( action.session.productName );
		if( storage == null )
			action.exit1( _Error.UnknownSessionProduct1 , "unknown product=" + action.session.productName , action.session.productName );
		
		if( storage.loadFailed )
			action.exit1( _Error.UnusableProductMetadata1 , "unusable metadata of product=" + action.session.productName , action.session.productName );
		
		return( storage );
	}

	public MetaProductVersion loadVersion( ActionInit action , ServerProductMeta storageFinal ) throws Exception {
		MetadataStorage storageMeta = action.artefactory.getMetadataStorage( action );
		return( storageFinal.loadVersion( action , storageMeta ) );
	}

	public MetaProductSettings loadProduct( ActionInit action , ServerProductMeta storageFinal ) throws Exception {
		MetadataStorage storageMeta = action.artefactory.getMetadataStorage( action );
		return( storageFinal.loadProduct( action , storageMeta ) );
	}

	public MetaDistr loadDistr( ActionInit action , ServerProductMeta storageFinal ) throws Exception {
		MetadataStorage storageMeta = action.artefactory.getMetadataStorage( action );
		return( storageFinal.loadDistr( action , storageMeta ) );
	}
	
	public MetaDatabase loadDatabase( ActionInit action , ServerProductMeta storageFinal ) throws Exception {
		MetadataStorage storageMeta = action.artefactory.getMetadataStorage( action );
		return( storageFinal.loadDatabase( action , storageMeta ) );
	}
	
	public MetaSource loadSources( ActionInit action , ServerProductMeta storageFinal ) throws Exception {
		MetadataStorage storageMeta = action.artefactory.getMetadataStorage( action );
		return( storageFinal.loadSources( action , storageMeta ) );
	}
	
	public MetaMonitoring loadMonitoring( ActionInit action , ServerProductMeta storageFinal ) throws Exception {
		MetadataStorage storageMeta = action.artefactory.getMetadataStorage( action );
		return( storageFinal.loadMonitoring( action , storageMeta ) );
	}

	public MetaEnv loadEnvData( ActionInit action , ServerProductMeta storageFinal , String envFile , boolean loadProps ) throws Exception {
		MetadataStorage storageMeta = action.artefactory.getMetadataStorage( action );
		MetaEnv env = storageFinal.loadEnvData( action , storageMeta , envFile );
		if( loadProps && env.missingSecretProperties )
			action.exit0( _Error.MissingSecretProperties0 , "operation is unavailable - secret properties are missing" );
		return( env );
	}
	
	public MetaDesign loadDesignData( ActionInit action , ServerProductMeta storageFinal , String fileName ) throws Exception {
		MetadataStorage storageMeta = action.artefactory.getMetadataStorage( action );
		return( storageFinal.loadDesignData( action , storageMeta , fileName ) );
	}

	public void loadServerProducts( ActionInit action ) {
		for( String name : registry.directory.getProducts() ) {
			
			ServerProductMeta set = new ServerProductMeta( this , name , action.session );
			productMeta.put( name , set );
			
			try {
				action.setServerSystemProductLayout( name );
				MetadataStorage storageMeta = action.artefactory.getMetadataStorage( action );
				LocalFolder folder = storageMeta.getFolder( action );
				if( folder.checkExists( action ) )
					set.loadAll( action , storageMeta );
				else
					set.setLoadFailed( action , "metadata folder is missing, product=" + name );
			}
			catch( Throwable e ) {
				action.log( e );
				action.error( "unable to load metadata, product=" + name );
			}
			
			action.clearServerProductLayout();
		}
	}

	public void setProductProps( ActionInit action , PropertySet props ) throws Exception {
		props.copyOriginalPropertiesToRaw( settings.getDefaultProductProperties() );
		for( PropertySet set : settings.getBuildModeDefaults() )
			props.copyOriginalPropertiesToRaw( set );
		props.resolveRawProperties();
	}

	public ServerMirror getMirror() {
		synchronized( engine ) {
			return( registry.mirror );
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

	public void saveRegistry( ServerTransaction transaction ) throws Exception {
		String propertyFile = getServerRegistryFile();
		registry.save( transaction.getAction() , propertyFile , engine.execrc );
	}
	
	public void setResources( ServerTransaction transaction , ServerResources resourcesNew ) throws Exception {
		registry.setResources( transaction , resourcesNew );
		saveRegistry( transaction );
	}

	public void setBuilders( ServerTransaction transaction , ServerBuilders buildersNew ) throws Exception {
		registry.setBuilders( transaction , buildersNew );
		saveRegistry( transaction );
	}

	public void setDirectory( ServerTransaction transaction , ServerDirectory directoryNew ) throws Exception {
		registry.setDirectory( transaction , directoryNew );
		saveRegistry( transaction );
	}

	public void saveMirrors( ServerTransaction transaction ) throws Exception {
		saveRegistry( transaction );
	}

	public ServerSettings getSettings() {
		synchronized( engine ) {
			return( settings );
		}
	}

	public void setSettings( ServerTransaction transaction , ServerSettings settingsNew ) throws Exception {
		String propertyFile = getServerSettingsFile();
		settingsNew.save( propertyFile , engine.execrc );
		settings = settingsNew;
	}

	public ServerProductMeta createMetadata( ServerTransaction transaction , ServerDirectory directoryNew , ServerProduct product ) throws Exception {
		ActionInit action = transaction.getAction();
		action.setServerSystemProductLayout( product );
		
		ServerProductMeta set = new ServerProductMeta( this , product.NAME , action.session );
		ServerSettings settings = transaction.getSettings();
		set.createInitial( action , settings , directoryNew );
		
		action.clearServerProductLayout();
		return( set );
	}
	
	public void setMetadata( ServerTransaction transaction , ServerProductMeta storageNew ) throws Exception {
		ActionInit action = transaction.getAction();
		action.setServerSystemProductLayout( storageNew.name );
		
		MetadataStorage storage = action.artefactory.getMetadataStorage( action );
		storageNew.saveAll( action , storage );
		productMeta.put( storageNew.name , storageNew );
	}
	
	public void deleteMetadata( ServerTransaction transaction , ServerProductMeta storage ) throws Exception {
		productMeta.remove( storage.name );
	}
	
}
