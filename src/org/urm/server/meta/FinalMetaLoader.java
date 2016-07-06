package org.urm.server.meta;

import java.util.HashMap;
import java.util.Map;

import org.urm.server.ServerEngine;
import org.urm.server.SessionContext;
import org.urm.server.action.ActionBase;
import org.urm.server.storage.MetadataStorage;

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
		return( storageFinal.loadEnvData( action , storageMeta , envFile , loadProps ) );
	}
	
	public MetaDesign loadDesignData( ActionBase action , FinalMetaStorage storageFinal , String fileName ) throws Exception {
		MetadataStorage storageMeta = action.artefactory.getMetadataStorage( action );
		return( storageFinal.loadDesignData( action , storageMeta , fileName ) );
	}
	
}
