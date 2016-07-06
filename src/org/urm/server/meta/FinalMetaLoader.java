package org.urm.server.meta;

import java.util.HashMap;
import java.util.Map;

import org.urm.server.ServerEngine;
import org.urm.server.SessionContext;
import org.urm.server.action.ActionBase;
import org.urm.server.storage.LocalFolder;
import org.urm.server.storage.MetadataStorage;
import org.urm.server.storage.UrmStorage;

public class FinalMetaLoader {

	public ServerEngine engine;
	
	private Map<String,FinalMetaStorage> productMeta;
	private FinalMetaStorage standalone;
	
	public FinalMetaLoader( ServerEngine engine ) {
		this.engine = engine;
		productMeta = new HashMap<String,FinalMetaStorage>();
	}
	
	public Metadata createMetadata( SessionContext session ) throws Exception {
		Metadata meta = new Metadata( this , session );
		return( meta );
	}
	
	public synchronized FinalMetaStorage getMetaStorage( ActionBase action ) throws Exception {
		if( action.session.standalone ) {
			if( standalone == null )
				standalone = new FinalMetaStorage( this , action.session );
			return( standalone );
		}
		
		if( !action.session.product )
			action.exitUnexpectedState();
			
		FinalMetaStorage storage = productMeta.get( action.session.productDir );
		if( storage == null )
			action.exit( "unknown product=" + action.session.productDir );
		
		return( storage );
	}
	
	public MetaProduct loadProduct( ActionBase action , FinalMetaStorage storageFinal ) throws Exception {
		MetadataStorage storageMeta = action.artefactory.getMetadataStorage( action );
		return( storageFinal.loadProduct( action , storageMeta ) );
	}

	public MetaDistr loadDistr( ActionBase action , FinalMetaStorage storageFinal ) throws Exception {
		MetadataStorage storageMeta = action.artefactory.getMetadataStorage( action );
		return( storageFinal.loadDistr( action , storageMeta ) );
	}
	
	public MetaDatabase loadDatabase( ActionBase action , FinalMetaStorage storageFinal ) throws Exception {
		MetadataStorage storageMeta = action.artefactory.getMetadataStorage( action );
		return( storageFinal.loadDatabase( action , storageMeta ) );
	}
	
	public MetaSource loadSources( ActionBase action , FinalMetaStorage storageFinal ) throws Exception {
		MetadataStorage storageMeta = action.artefactory.getMetadataStorage( action );
		return( storageFinal.loadSources( action , storageMeta ) );
	}
	
	public MetaMonitoring loadMonitoring( ActionBase action , FinalMetaStorage storageFinal ) throws Exception {
		MetadataStorage storageMeta = action.artefactory.getMetadataStorage( action );
		return( storageFinal.loadMonitoring( action , storageMeta ) );
	}

	public MetaEnv loadEnvData( ActionBase action , FinalMetaStorage storageFinal , String envFile , boolean loadProps ) throws Exception {
		MetadataStorage storageMeta = action.artefactory.getMetadataStorage( action );
		MetaEnv env = storageFinal.loadEnvData( action , storageMeta , envFile );
		if( loadProps && env.missingSecretProperties )
			action.exit( "operation is unavailable - secret properties are missing" );
		return( env );
	}
	
	public MetaDesign loadDesignData( ActionBase action , FinalMetaStorage storageFinal , String fileName ) throws Exception {
		MetadataStorage storageMeta = action.artefactory.getMetadataStorage( action );
		return( storageFinal.loadDesignData( action , storageMeta , fileName ) );
	}

	public void loadServerProducts( ActionBase action ) throws Exception {
		UrmStorage urm = action.artefactory.getUrmStorage();
		LocalFolder pfProducts = urm.getServerProductsFolder( action );
		
		if( !pfProducts.checkExists( action ) )
			action.exit( "before configure, please create directory: " + pfProducts.folderPath );

		for( String productDir : pfProducts.getTopDirs( action ) ) {
			action.session.setServerProductLayout( productDir );
			MetadataStorage storageMeta = action.artefactory.getMetadataStorage( action );
			
			FinalMetaStorage storage = new FinalMetaStorage( this , action.session );
			storage.loadAll( action , storageMeta );
			
			productMeta.put( productDir , storage );
		}
		
		action.session.clearServerProductLayout();
	}
	
}
