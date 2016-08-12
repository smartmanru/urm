package org.urm.server;

import java.util.HashMap;
import java.util.Map;

import org.urm.common.Common;
import org.urm.common.PropertySet;
import org.urm.server.action.ActionBase;
import org.urm.server.action.ActionInit;
import org.urm.server.meta.MetaDatabase;
import org.urm.server.meta.MetaDesign;
import org.urm.server.meta.MetaDistr;
import org.urm.server.meta.MetaEnv;
import org.urm.server.meta.MetaMonitoring;
import org.urm.server.meta.MetaProduct;
import org.urm.server.meta.MetaSource;
import org.urm.server.meta.Meta;
import org.urm.server.storage.MetadataStorage;

public class ServerLoader {

	public ServerEngine engine;
	
	private Map<String,ServerMetaSet> productMeta;
	private ServerMetaSet offline;
	private ServerRegistry registry;
	
	public ServerLoader( ServerEngine engine ) {
		this.engine = engine;
		registry = new ServerRegistry( this ); 
		productMeta = new HashMap<String,ServerMetaSet>();
	}
	
	public Meta createMetadata( SessionContext session ) throws Exception {
		Meta meta = new Meta( this , session );
		return( meta );
	}

	public synchronized ServerMetaSet getMetaStorage( String productName ) throws Exception {
		return( productMeta.get( productName ) );
	}
	
	public synchronized ServerMetaSet getMetaStorage( ActionInit action ) throws Exception {
		if( action.session.offline ) {
			if( offline == null )
				offline = new ServerMetaSet( this , action.session );
			return( offline );
		}
		
		if( !action.session.product )
			action.exitUnexpectedState();
			
		ServerMetaSet storage = productMeta.get( action.session.productName );
		if( storage == null )
			action.exit( "unknown product=" + action.session.productName );
		
		return( storage );
	}

	public MetaProduct loadProduct( ActionInit action , ServerMetaSet storageFinal ) throws Exception {
		MetadataStorage storageMeta = action.artefactory.getMetadataStorage( action );
		return( storageFinal.loadProduct( action , storageMeta ) );
	}

	public MetaDistr loadDistr( ActionInit action , ServerMetaSet storageFinal ) throws Exception {
		MetadataStorage storageMeta = action.artefactory.getMetadataStorage( action );
		return( storageFinal.loadDistr( action , storageMeta ) );
	}
	
	public MetaDatabase loadDatabase( ActionInit action , ServerMetaSet storageFinal ) throws Exception {
		MetadataStorage storageMeta = action.artefactory.getMetadataStorage( action );
		return( storageFinal.loadDatabase( action , storageMeta ) );
	}
	
	public MetaSource loadSources( ActionInit action , ServerMetaSet storageFinal ) throws Exception {
		MetadataStorage storageMeta = action.artefactory.getMetadataStorage( action );
		return( storageFinal.loadSources( action , storageMeta ) );
	}
	
	public MetaMonitoring loadMonitoring( ActionInit action , ServerMetaSet storageFinal ) throws Exception {
		MetadataStorage storageMeta = action.artefactory.getMetadataStorage( action );
		return( storageFinal.loadMonitoring( action , storageMeta ) );
	}

	public MetaEnv loadEnvData( ActionInit action , ServerMetaSet storageFinal , String envFile , boolean loadProps ) throws Exception {
		MetadataStorage storageMeta = action.artefactory.getMetadataStorage( action );
		MetaEnv env = storageFinal.loadEnvData( action , storageMeta , envFile );
		if( loadProps && env.missingSecretProperties )
			action.exit( "operation is unavailable - secret properties are missing" );
		return( env );
	}
	
	public MetaDesign loadDesignData( ActionInit action , ServerMetaSet storageFinal , String fileName ) throws Exception {
		MetadataStorage storageMeta = action.artefactory.getMetadataStorage( action );
		return( storageFinal.loadDesignData( action , storageMeta , fileName ) );
	}

	public void loadServerProducts( ActionInit action ) throws Exception {
		for( String name : registry.getProducts( action ) ) {
			action.setServerSystemProductLayout( name );
			
			try {
				MetadataStorage storageMeta = action.artefactory.getMetadataStorage( action );
				ServerMetaSet set = new ServerMetaSet( this , action.session );
				set.loadAll( action , storageMeta );
				productMeta.put( name , set );
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

	public void addProductProps( ActionInit action , PropertySet props ) throws Exception {
		props.copyRawProperties( registry.getDefaultProductProperties( action ) , "" );
		for( PropertySet set : registry.getBuildModeDefaults( action ) )
			props.copyRawProperties( set , set.set + "." );
	}

	public ServerRegistry getRegistry() {
		synchronized( engine ) {
			return( registry );
		}
	}

	public void setRegistry( ServerTransaction transacction , ServerRegistry registryNew ) throws Exception {
		String propertyFile = getServerSettingsFile();
		registryNew.save( propertyFile , engine.execrc );
		registry = registryNew;
	}

	public ServerMetaSet createMetadata( ServerTransaction transaction , ServerRegistry registryNew , ServerProduct product ) throws Exception {
		ActionBase action = transaction.action;
		action.actionInit.setServerSystemProductLayout( product );
		
		MetadataStorage storageMeta = action.artefactory.getMetadataStorage( action );
		ServerMetaSet set = new ServerMetaSet( this , action.session );
		set.createInitial( action , registryNew );
		set.saveAll( action , storageMeta , product );
		
		action.actionInit.clearServerProductLayout();
		return( set );
	}
	
	public void setMetadata( ServerTransaction transaction , ServerMetaSet storageNew ) throws Exception {
	}
	
}
