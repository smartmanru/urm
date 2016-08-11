package org.urm.server.meta;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.urm.common.ConfReader;
import org.urm.server.SessionContext;
import org.urm.server.action.ActionBase;
import org.urm.server.storage.MetadataStorage;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

public class FinalMetaSet {

	public FinalLoader loader;
	public SessionContext session;
	
	public Metadata meta;
	
	private MetaProduct product;
	private MetaDistr distr;
	private MetaDatabase database;
	private MetaSource sources;
	private MetaMonitoring mon;
	
	private Map<String,MetaDesign> designFiles;
	private Map<String,MetaEnv> envs;
	
	public FinalMetaSet( FinalLoader loader , SessionContext session ) {
		this.loader = loader;
		this.session = session;
		
		meta = new Metadata( this , session );
		designFiles = new HashMap<String,MetaDesign>();
		envs = new HashMap<String,MetaEnv>();
	}

	public synchronized MetaProduct loadProduct( ActionBase action , MetadataStorage storageMeta , String productId ) throws Exception {
		if( product != null )
			return( product );
		
		product = new MetaProduct( meta );
		meta.setProduct( product );

		// read
		String file = storageMeta.getProductConfFile( action );
		action.debug( "read product definition file " + file + "..." );
		Document doc = ConfReader.readXmlFile( action.session.execrc , file );
		Node root = doc.getDocumentElement();
		product.load( action , productId , root );
		
		return( product );
	}
	
	public synchronized MetaDistr loadDistr( ActionBase action , MetadataStorage storageMeta ) throws Exception {
		if( distr != null )
			return( distr );
		
		distr = new MetaDistr( meta );
		meta.setDistr( distr );
		
		// read
		String file = storageMeta.getDistrFile( action );
		action.debug( "read distributive definition file " + file + "..." );
		Document doc = action.readXmlFile( file );
		Node root = doc.getDocumentElement();
		distr.load( action , root );
		
		return( distr );
	}

	public synchronized MetaDatabase loadDatabase( ActionBase action , MetadataStorage storageMeta ) throws Exception {
		if( database != null )
			return( database );
		
		database = new MetaDatabase( meta );
		meta.setDatabase( database );
		
		// read
		String file = storageMeta.getDatabaseFile( action );
		action.debug( "read database definition file " + file + "..." );
		Document doc = action.readXmlFile( file );
		Node root = doc.getDocumentElement();
		database.load( action , root );
		
		return( database );
	}
	
	public synchronized MetaSource loadSources( ActionBase action , MetadataStorage storageMeta ) throws Exception {
		if( sources != null )
			return( sources );
		
		sources = new MetaSource( meta );
		meta.setSources( sources );
		
		// read
		String file = storageMeta.getSourceConfFile( action );
		action.debug( "read source definition file " + file + "..." );
		Document doc = action.readXmlFile( file );
		Node root = doc.getDocumentElement();
		sources.load( action , root );
		
		return( sources );
	}
	
	public synchronized MetaMonitoring loadMonitoring( ActionBase action , MetadataStorage storageMeta ) throws Exception {
		if( mon != null )
			return( mon );
		
		mon = new MetaMonitoring( meta );
		
		// read
		String file = storageMeta.getMonitoringFile( action );
		action.debug( "read monitoring definition file " + file + "..." );
		Document doc = action.readXmlFile( file );
		Node root = doc.getDocumentElement();
		mon.load( action , root );
		
		return( mon );
	}
	
	public synchronized MetaEnv loadEnvData( ActionBase action , MetadataStorage storageMeta , String envFile ) throws Exception {
		MetaEnv env = envs.get( envFile );
		if( env != null )
			return( env );
		
		if( envFile.isEmpty() )
			action.exit( "environment file name is empty" );
		
		env = new MetaEnv( meta );

		// read
		String file = storageMeta.getEnvFile( action , envFile );
		action.debug( "read environment definition file " + file + "..." );
		Document doc = action.readXmlFile( file );
		Node root = doc.getDocumentElement();
		env.load( action , root );
		envs.put( envFile , env );
		
		return( env );
	}
	
	public synchronized MetaDesign loadDesignData( ActionBase action , MetadataStorage storageMeta , String fileName ) throws Exception {
		MetaDesign design = designFiles.get( fileName );
		if( design != null )
			return( design );
		
		design = new MetaDesign( meta );
		
		// read
		String filePath = storageMeta.getDesignFile( action , fileName );
		action.debug( "read design definition file " + filePath + "..." );
		Document doc = action.readXmlFile( filePath );
		Node root = doc.getDocumentElement();
		design.load( action , root );
		designFiles.put( fileName , design );
		
		return( design );
	}

	public synchronized void loadAll( ActionBase action , MetadataStorage storageMeta , String productId ) throws Exception {
		loadProduct( action , storageMeta , productId );
		loadDatabase( action , storageMeta );
		loadDistr( action , storageMeta );
		loadSources( action , storageMeta );
		loadMonitoring( action , storageMeta );
		
		action.meta.setProduct( product );
		action.meta.setDatabase( database );
		action.meta.setDistr( distr );
		action.meta.setSources( sources );
		
		for( String envFile : storageMeta.getEnvFiles( action ) )
			loadEnvData( action , storageMeta , envFile );
		for( String designFile : storageMeta.getDesignFiles( action ) )
			loadDesignData( action , storageMeta , designFile );
	}
	
	public synchronized String[] getEnvironments() throws Exception {
		List<String> names = new LinkedList<String>();
		for( MetaEnv env : envs.values() )
			names.add( env.ID );
		Collections.sort( names );
		return( names.toArray( new String[0] ) );
	}

	public synchronized MetaEnv getEnvironment( String envId ) throws Exception {
		for( MetaEnv env : envs.values() ) {
			if( env.ID.equals( envId ) )
				return( env );
		}
		return( null );
	}

	public FinalMetaSet copy( ActionBase action ) throws Exception {
		return( null );
	}

	public void createInitial( FinalRegistry registry ) throws Exception {
	}

	public void saveAll( ActionBase action , MetadataStorage storageMeta , FinalMetaProduct product ) throws Exception {
	}
	
}
