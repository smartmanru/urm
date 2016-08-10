package org.urm.server.meta;

import java.util.HashMap;
import java.util.Map;

import org.urm.common.Common;
import org.urm.common.PropertySet;
import org.urm.server.ServerEngine;
import org.urm.server.SessionContext;
import org.urm.server.action.ActionInit;
import org.urm.server.storage.MetadataStorage;

public class FinalLoader {

	public ServerEngine engine;
	
	private Map<String,FinalMetaStorage> productMeta;
	private FinalMetaStorage offline;
	private FinalRegistry registry;
	
	public FinalLoader( ServerEngine engine ) {
		this.engine = engine;
		registry = new FinalRegistry( this ); 
		productMeta = new HashMap<String,FinalMetaStorage>();
	}
	
	public Metadata createMetadata( SessionContext session ) throws Exception {
		Metadata meta = new Metadata( this , session );
		return( meta );
	}

	public synchronized FinalMetaStorage getMetaStorage( String productName ) throws Exception {
		return( productMeta.get( productName ) );
	}
	
	public synchronized FinalMetaStorage getMetaStorage( ActionInit action ) throws Exception {
		if( action.session.offline ) {
			if( offline == null )
				offline = new FinalMetaStorage( this , action.session );
			return( offline );
		}
		
		if( !action.session.product )
			action.exitUnexpectedState();
			
		FinalMetaStorage storage = productMeta.get( action.session.productName );
		if( storage == null )
			action.exit( "unknown product=" + action.session.productName );
		
		return( storage );
	}

	public MetaProduct loadProduct( ActionInit action , FinalMetaStorage storageFinal ) throws Exception {
		MetadataStorage storageMeta = action.artefactory.getMetadataStorage( action );
		return( storageFinal.loadProduct( action , storageMeta , "" ) );
	}

	public MetaDistr loadDistr( ActionInit action , FinalMetaStorage storageFinal ) throws Exception {
		MetadataStorage storageMeta = action.artefactory.getMetadataStorage( action );
		return( storageFinal.loadDistr( action , storageMeta ) );
	}
	
	public MetaDatabase loadDatabase( ActionInit action , FinalMetaStorage storageFinal ) throws Exception {
		MetadataStorage storageMeta = action.artefactory.getMetadataStorage( action );
		return( storageFinal.loadDatabase( action , storageMeta ) );
	}
	
	public MetaSource loadSources( ActionInit action , FinalMetaStorage storageFinal ) throws Exception {
		MetadataStorage storageMeta = action.artefactory.getMetadataStorage( action );
		return( storageFinal.loadSources( action , storageMeta ) );
	}
	
	public MetaMonitoring loadMonitoring( ActionInit action , FinalMetaStorage storageFinal ) throws Exception {
		MetadataStorage storageMeta = action.artefactory.getMetadataStorage( action );
		return( storageFinal.loadMonitoring( action , storageMeta ) );
	}

	public MetaEnv loadEnvData( ActionInit action , FinalMetaStorage storageFinal , String envFile , boolean loadProps ) throws Exception {
		MetadataStorage storageMeta = action.artefactory.getMetadataStorage( action );
		MetaEnv env = storageFinal.loadEnvData( action , storageMeta , envFile );
		if( loadProps && env.missingSecretProperties )
			action.exit( "operation is unavailable - secret properties are missing" );
		return( env );
	}
	
	public MetaDesign loadDesignData( ActionInit action , FinalMetaStorage storageFinal , String fileName ) throws Exception {
		MetadataStorage storageMeta = action.artefactory.getMetadataStorage( action );
		return( storageFinal.loadDesignData( action , storageMeta , fileName ) );
	}

	public void loadServerProducts( ActionInit action ) throws Exception {
		for( String name : registry.getProducts( action ) ) {
			action.setServerSystemProductLayout( name );
			
			MetadataStorage storageMeta = action.artefactory.getMetadataStorage( action );
			FinalMetaStorage storage = new FinalMetaStorage( this , action.session );
			storage.loadAll( action , storageMeta , name );
			productMeta.put( name , storage );
			
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

	public void addProductProps( ActionInit action , PropertySet props ) throws Exception {
		props.copyRawProperties( registry.getDefaultProductProperties( action ) , "" );
		for( PropertySet set : registry.getBuildModeDefaults( action ) )
			props.copyRawProperties( set , set.set + "." );
	}

	public FinalRegistry getRegistry( ActionInit action ) {
		synchronized( engine ) {
			return( registry );
		}
	}

	public void setRegistry( FinalRegistry registryNew ) throws Exception {
		String propertyFile = getServerSettingsFile();
		registryNew.save( propertyFile , engine.execrc );
		registry = registryNew;
	}

	public FinalMetaStorage createMetadata( FinalRegistry registryNew , FinalMetaProduct product ) throws Exception {
		return( null );
	}
	
	public void setMetadata( FinalMetaStorage storageNew ) throws Exception {
	}
	
}
