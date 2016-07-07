package org.urm.server.meta;

import java.util.HashMap;
import java.util.Map;

import org.urm.common.ConfReader;
import org.urm.server.SessionContext;
import org.urm.server.action.ActionBase;
import org.urm.server.storage.MetadataStorage;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

public class FinalMetaStorage {

	public FinalMetaLoader loader;
	public SessionContext session;
	
	private Metadata meta;
	
	private MetaProduct product;
	private MetaDistr distr;
	private MetaDatabase database;
	private MetaSource sources;
	private MetaMonitoring mon;
	
	private Map<String,MetaDesign> designFiles;
	private Map<String,MetaEnv> envs;
	
	public FinalMetaStorage( FinalMetaLoader loader , SessionContext session ) {
		this.loader = loader;
		this.session = session;
		
		meta = new Metadata( this , session );
		designFiles = new HashMap<String,MetaDesign>();
		envs = new HashMap<String,MetaEnv>();
	}

	public synchronized MetaProduct loadProduct( ActionBase action , MetadataStorage storageMeta ) throws Exception {
		if( product != null )
			return( product );
		
		product = new MetaProduct( meta );
		product.load( action , storageMeta );
		meta.setProduct( product );
		
		return( product );
	}
	
	public synchronized MetaDistr loadDistr( ActionBase action , MetadataStorage storageMeta ) throws Exception {
		if( distr != null )
			return( distr );
		
		distr = new MetaDistr( meta );
		
		// read xml
		String file = storageMeta.getDistrFile( action );
		
		action.debug( "read distributive definition file " + file + "..." );
		Document doc = action.readXmlFile( file );
		Node root = doc.getDocumentElement();
		
		loadDatabase( action , ConfReader.xmlGetPathNode( root , "distributive/database" ) );
		
		distr.load( action , root );
		meta.setDistr( distr );
		
		return( distr );
	}

	public synchronized MetaDatabase loadDatabase( ActionBase action , MetadataStorage storageMeta ) throws Exception {
		if( database == null )
			loadDistr( action , storageMeta );
		
		return( database );
	}
	
	public synchronized MetaSource loadSources( ActionBase action , MetadataStorage storageMeta ) throws Exception {
		if( sources != null )
			return( sources );
		
		sources = new MetaSource( meta );
		sources.load( action , storageMeta );
		meta.setSources( sources );
		
		return( sources );
	}
	
	public synchronized MetaMonitoring loadMonitoring( ActionBase action , MetadataStorage storageMeta ) throws Exception {
		if( mon != null )
			return( mon );
		
		mon = new MetaMonitoring( meta );
		mon.load( action , storageMeta );
		
		return( mon );
	}
	
	public synchronized MetaEnv loadEnvData( ActionBase action , MetadataStorage storageMeta , String envFile ) throws Exception {
		MetaEnv env = envs.get( envFile );
		if( env != null )
			return( env );
		
		if( envFile.isEmpty() )
			action.exit( "environment file name is empty" );
		
		MetaEnv envData = new MetaEnv( meta );
		envData.load( action , storageMeta , envFile );
		envs.put( envFile , env );
		
		return( envData );
	}
	
	public synchronized MetaDesign loadDesignData( ActionBase action , MetadataStorage storageMeta , String fileName ) throws Exception {
		MetaDesign design = designFiles.get( fileName );
		if( design != null )
			return( design );
		
		design = new MetaDesign( meta );
		design.load( action , storageMeta , fileName );
		designFiles.put( fileName , design );
		
		return( design );
	}

	public synchronized void loadAll( ActionBase action , MetadataStorage storageMeta ) throws Exception {
		action.meta.product = loadProduct( action , storageMeta );
		action.meta.distr = loadDistr( action , storageMeta );
		action.meta.database = meta.database;
		action.meta.sources = loadSources( action , storageMeta );
		
		String file = storageMeta.getMonitoringFile( action );
		if( action.shell.checkFileExists( action , file ) )
			loadMonitoring( action , storageMeta );
		
		for( String envFile : storageMeta.getEnvFiles( action ) )
			loadEnvData( action , storageMeta , envFile );
		for( String designFile : storageMeta.getDesignFiles( action ) )
			loadDesignData( action , storageMeta , designFile );
	}
	
	private void loadDatabase( ActionBase action , Node node ) throws Exception {
		database = new MetaDatabase( meta );
		meta.setDatabase( database );
		
		if( node != null )
			database.load( action , node );
	}
	
}
