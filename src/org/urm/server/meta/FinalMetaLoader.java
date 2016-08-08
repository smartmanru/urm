package org.urm.server.meta;

import java.util.HashMap;
import java.util.Map;

import org.urm.common.Common;
import org.urm.common.PropertySet;
import org.urm.server.ServerEngine;
import org.urm.server.SessionContext;
import org.urm.server.action.ActionBase;
import org.urm.server.storage.MetadataStorage;

public class FinalMetaLoader {

	public ServerEngine engine;
	
	private Map<String,FinalMetaStorage> productMeta;
	private FinalMetaStorage offline;
	private MetaRegistry engineMeta;
	
	public FinalMetaLoader( ServerEngine engine ) {
		this.engine = engine;
		engineMeta = new MetaRegistry( this ); 
		productMeta = new HashMap<String,FinalMetaStorage>();
	}
	
	public Metadata createMetadata( SessionContext session ) throws Exception {
		Metadata meta = new Metadata( this , session );
		return( meta );
	}

	public synchronized FinalMetaStorage getMetaStorage( String productName ) throws Exception {
		return( productMeta.get( productName ) );
	}
	
	public synchronized FinalMetaStorage getMetaStorage( ActionBase action ) throws Exception {
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

	public MetaProduct loadProduct( ActionBase action , FinalMetaStorage storageFinal ) throws Exception {
		MetadataStorage storageMeta = action.artefactory.getMetadataStorage( action );
		return( storageFinal.loadProduct( action , storageMeta , "" ) );
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
		for( String name : getProducts( action ) ) {
			action.setServerSystemProductLayout( name );
			
			MetadataStorage storageMeta = action.artefactory.getMetadataStorage( action );
			FinalMetaStorage storage = new FinalMetaStorage( this , action.session );
			storage.loadAll( action , storageMeta , name );
			productMeta.put( name , storage );
			
			action.clearServerProductLayout();
		}
	}

	public void loadServerSettings() throws Exception {
		String path = Common.getPath( engine.execrc.installPath , "etc" );
		String propertyFile = Common.getPath( path , "server.xml" );
		engineMeta.load( propertyFile , engine.execrc );
	}

	public String[] getSystems( ActionBase action ) throws Exception {
		return( Common.getSortedKeys( engineMeta.mapSystems ) );
	}

	public String[] getProducts( ActionBase action ) throws Exception {
		return( Common.getSortedKeys( engineMeta.mapProducts ) );
	}

	public FinalMetaSystem getSystemMeta( ActionBase action , String name ) throws Exception {
		FinalMetaSystem system = engineMeta.mapSystems.get( name );
		if( system == null )
			action.exit( "unknown system=" + system );
		return( system );
	}

	public FinalMetaProduct getProductMeta( ActionBase action , String name ) throws Exception {
		FinalMetaProduct product = engineMeta.mapProducts.get( name );
		if( product == null )
			action.exit( "unknown product=" + name );
		return( product );
	}

	public void addProductProps( ActionBase action , PropertySet props ) throws Exception {
		props.copyRawProperties( engineMeta.defaultProductProperties , "" );
		for( PropertySet set : engineMeta.mapBuildModeDefaults.values() )
			props.copyRawProperties( set , set.set + "." );
	}
	
}
