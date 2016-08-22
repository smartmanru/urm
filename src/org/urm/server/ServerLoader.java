package org.urm.server;

import java.util.HashMap;
import java.util.Map;

import org.urm.common.Common;
import org.urm.common.PropertySet;
import org.urm.server.action.ActionInit;
import org.urm.server.meta.MetaDatabase;
import org.urm.server.meta.MetaDesign;
import org.urm.server.meta.MetaDistr;
import org.urm.server.meta.MetaEnv;
import org.urm.server.meta.MetaMonitoring;
import org.urm.server.meta.MetaProductSettings;
import org.urm.server.meta.MetaSource;
import org.urm.server.meta.Meta;
import org.urm.server.meta.MetaProductVersion;
import org.urm.server.storage.LocalFolder;
import org.urm.server.storage.MetadataStorage;

public class ServerLoader {

	public ServerEngine engine;
	
	private Map<String,ServerProductMeta> productMeta;
	private ServerProductMeta offline;
	private ServerRegistry registry;
	
	public ServerLoader( ServerEngine engine ) {
		this.engine = engine;
		
		registry = new ServerRegistry( this ); 
		productMeta = new HashMap<String,ServerProductMeta>();
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
			action.exit( "unknown product=" + action.session.productName );
		
		if( storage.loadFailed )
			action.exit( "unusable metadata of product=" + action.session.productName );
		
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
			action.exit( "operation is unavailable - secret properties are missing" );
		return( env );
	}
	
	public MetaDesign loadDesignData( ActionInit action , ServerProductMeta storageFinal , String fileName ) throws Exception {
		MetadataStorage storageMeta = action.artefactory.getMetadataStorage( action );
		return( storageFinal.loadDesignData( action , storageMeta , fileName ) );
	}

	public void loadServerProducts( ActionInit action ) {
		for( String name : registry.getProducts() ) {
			
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

	private String getServerSettingsFile() {
		String path = Common.getPath( engine.execrc.installPath , "etc" );
		String propertyFile = Common.getPath( path , "server.xml" );
		return( propertyFile );
	}
	
	public void loadServerSettings() throws Exception {
		String propertyFile = getServerSettingsFile();
		registry.load( propertyFile , engine.execrc );
	}

	public void setProductProps( ActionInit action , PropertySet props ) throws Exception {
		props.copyOriginalPropertiesToRaw( registry.getDefaultProductProperties() );
		for( PropertySet set : registry.getBuildModeDefaults() )
			props.copyOriginalPropertiesToRaw( set );
		props.resolveRawProperties();
	}

	public ServerRegistry getRegistry() {
		synchronized( engine ) {
			return( registry );
		}
	}

	public void setRegistry( ServerTransaction transaction , ServerRegistry registryNew ) throws Exception {
		String propertyFile = getServerSettingsFile();
		registryNew.save( propertyFile , engine.execrc );
		registry = registryNew;
	}

	public ServerProductMeta createMetadata( ServerTransaction transaction , ServerRegistry registryNew , ServerProduct product ) throws Exception {
		ActionInit action = transaction.metadataAction;
		action.setServerSystemProductLayout( product );
		
		ServerProductMeta set = new ServerProductMeta( this , product.NAME , action.session );
		set.createInitial( action , registryNew );
		
		action.clearServerProductLayout();
		return( set );
	}
	
	public void setMetadata( ServerTransaction transaction , ServerProductMeta storageNew ) throws Exception {
		ActionInit action = transaction.metadataAction;
		action.setServerSystemProductLayout( storageNew.name );
		
		MetadataStorage storage = action.artefactory.getMetadataStorage( action );
		storageNew.saveAll( action , storage );
		productMeta.put( storageNew.name , storageNew );
	}
	
	public void deleteMetadata( ServerTransaction transaction , ServerProductMeta storage ) throws Exception {
		productMeta.remove( storage.name );
	}
	
}
