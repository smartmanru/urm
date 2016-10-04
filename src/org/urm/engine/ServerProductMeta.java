package org.urm.engine;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.urm.action.ActionBase;
import org.urm.common.Common;
import org.urm.common.ConfReader;
import org.urm.common.PropertySet;
import org.urm.engine.storage.MetadataStorage;
import org.urm.meta.engine.ServerDirectory;
import org.urm.meta.product.Meta;
import org.urm.meta.product.MetaDatabase;
import org.urm.meta.product.MetaDesign;
import org.urm.meta.product.MetaDistr;
import org.urm.meta.product.MetaEnv;
import org.urm.meta.product.MetaMonitoring;
import org.urm.meta.product.MetaProductSettings;
import org.urm.meta.product.MetaProductVersion;
import org.urm.meta.product.MetaSource;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

public class ServerProductMeta extends ServerObject {

	public ServerLoader loader;
	public String name;
	public ServerSession session;
	
	public Meta meta;
	
	private MetaProductVersion version;
	private MetaProductSettings product;
	private MetaDatabase database;
	private MetaDistr distr;
	private MetaSource sources;
	private MetaMonitoring mon;
	
	private Map<String,MetaEnv> envs;
	private Map<String,MetaDesign> designFiles;
	
	public static String XML_ROOT_VERSION = "version";
	public static String XML_ROOT_PRODUCT = "product";
	public static String XML_ROOT_DISTR = "distributive";
	public static String XML_ROOT_DATABASE = "database";
	public static String XML_ROOT_SOURCES = "sources";
	public static String XML_ROOT_MONITORING = "monitoring";
	public static String XML_ROOT_ENV = "environment";

	public boolean loadFailed;
	
	private Map<ServerSession,Meta> sessionMeta;
	private boolean primary;
	
	public ServerProductMeta( ServerLoader loader , String name , ServerSession session ) {
		super( null );
		this.loader = loader;
		this.name = name;
		this.session = session;
		
		meta = new Meta( this , session );
		loader.engine.serverAction.trace( "new product storage meta object, id=" + meta.objectId + ", storage=" + objectId );
		designFiles = new HashMap<String,MetaDesign>();
		envs = new HashMap<String,MetaEnv>();
		
		loadFailed = false;
		sessionMeta = new HashMap<ServerSession,Meta>();
		primary = false;
	}

	public void setPrimary( boolean primary ) {
		this.primary = primary;
	}
	
	public synchronized void addSessionMeta( Meta meta ) {
		sessionMeta.put( meta.session , meta );
	}
	
	public synchronized void releaseSessionMeta( Meta meta ) {
		sessionMeta.remove( meta.session );
	}

	public synchronized Meta findSessionMeta( ServerSession session ) {
		return( sessionMeta.get( session ) );
	}

	public synchronized boolean isReferencedBySessions() {
		if( sessionMeta.isEmpty() )
			return( false );
		return( true );
	}
	
	public boolean isPrimary() {
		return( primary );
	}
	
	public synchronized ServerProductMeta copy( ActionBase action ) throws Exception {
		ServerProductMeta r = new ServerProductMeta( loader , name , session );
		if( version != null ) {
			r.version = version.copy( action , r.meta );
			if( r.version.isLoadFailed() )
				r.loadFailed = true;
		}
		if( product != null ) {
			r.product = product.copy( action , r.meta );
			if( r.product.isLoadFailed() )
				r.loadFailed = true;
		}
		if( database != null ) {
			r.database = database.copy( action , r.meta );
			if( r.database.isLoadFailed() )
				r.loadFailed = true;
		}
		if( distr != null ) {
			r.distr = distr.copy( action , r.meta );
			if( r.distr.isLoadFailed() )
				r.loadFailed = true;
		}
		if( sources != null ) {
			r.sources = sources.copy( action , r.meta );
			if( r.sources.isLoadFailed() )
				r.loadFailed = true;
		}
		if( mon != null ) {
			r.mon = mon.copy( action , r.meta );
			if( r.mon.isLoadFailed() )
				r.loadFailed = true;
		}
		for( String envKey : envs.keySet() ) {
			MetaEnv env = envs.get( envKey );
			MetaEnv re = env.copy( action , r.meta );
			r.envs.put( envKey , re );
			if( re.isLoadFailed() )
				r.loadFailed = true;
		}
		for( String designFile : designFiles.keySet() ) {
			MetaDesign design = designFiles.get( designFile );
			MetaDesign rd = design.copy( action , r.meta );
			r.designFiles.put( designFile , rd );
			if( rd.loadFailed )
				r.loadFailed = true;
		}
		
		return( r );
	}

	public void setLoadFailed( ActionBase action , String msg ) {
		loadFailed = true;
		action.error( msg );
	}
	
	private void setLoadFailed( ActionBase action , Throwable e , String msg ) {
		loadFailed = true;
		action.handle( e );
		action.error( msg );
	}
	
	public synchronized MetaProductVersion loadVersion( ActionBase action , MetadataStorage storageMeta ) {
		if( version != null )
			return( version );
		
		version = new MetaProductVersion( this , meta );

		if( !loadFailed ) {
			try {
				// read
				String file = storageMeta.getVersionConfFile( action );
				action.debug( "read product version file " + file + "..." );
				Document doc = ConfReader.readXmlFile( action.session.execrc , file );
				Node root = doc.getDocumentElement();
				version.load( action , root );
				if( version.isLoadFailed() )
					setLoadFailed( action , "invalid version metadata, product=" + name );
			}
			catch( Throwable e ) {
				setLoadFailed( action , e , "unable to load version metadata, product=" + name );
			}
		}
		
		return( version );
	}

	public synchronized MetaProductSettings loadProduct( ActionBase action , MetadataStorage storageMeta ) {
		if( product != null )
			return( product );
		
		PropertySet execprops = null;
		if( action.isStandalone() ) {
			execprops = new PropertySet( "execrc" , null );
			try {
				action.engine.execrc.getProperties( execprops );
			}
			catch( Throwable e ) {
				action.handle( e );
			}
		}
		else {
			ServerSettings settings = action.getServerSettings();
			execprops = settings.serverContext.execprops;
		}
		
		product = new MetaProductSettings( this , meta , execprops );

		if( !loadFailed ) {
			try {
				ServerProductContext productContext = new ServerProductContext( meta );
				productContext.create( action , version );
				
				// read
				String file = storageMeta.getProductConfFile( action );
				action.debug( "read product definition file " + file + "..." );
				Document doc = ConfReader.readXmlFile( action.session.execrc , file );
				Node root = doc.getDocumentElement();
				product.load( action , productContext , root );
				if( product.isLoadFailed() )
					setLoadFailed( action , "invalid settings metadata, product=" + name );
			}
			catch( Throwable e ) {
				setLoadFailed( action , e , "unable to load settings metadata, product=" + name );
			}
		}
		
		return( product );
	}
	
	public synchronized MetaDatabase loadDatabase( ActionBase action , MetadataStorage storageMeta ) {
		if( database != null )
			return( database );
		
		database = new MetaDatabase( this , meta );
		
		if( !loadFailed ) {
			try {
				// read
				String file = storageMeta.getDatabaseConfFile( action );
				action.debug( "read database definition file " + file + "..." );
				Document doc = action.readXmlFile( file );
				Node root = doc.getDocumentElement();
				database.load( action , root );
				if( database.isLoadFailed() )
					setLoadFailed( action , "invalid database metadata, product=" + name );
			}
			catch( Throwable e ) {
				setLoadFailed( action , e , "unable to load database metadata, product=" + name );
			}
		}
		
		return( database );
	}
	
	public synchronized MetaDistr loadDistr( ActionBase action , MetadataStorage storageMeta ) {
		if( distr != null )
			return( distr );
		
		distr = new MetaDistr( this , meta );
		meta.setDistr( distr );
		
		if( !loadFailed ) {
			try {
				// read
				String file = storageMeta.getDistrConfFile( action );
				action.debug( "read distributive definition file " + file + "..." );
				Document doc = action.readXmlFile( file );
				Node root = doc.getDocumentElement();
				distr.load( action , root );
				if( distr.isLoadFailed() )
					setLoadFailed( action , "invalid distributive metadata, product=" + name );
			}
			catch( Throwable e ) {
				setLoadFailed( action , e , "unable to load distributive metadata, product=" + name );
			}
		}
		
		return( distr );
	}

	public synchronized MetaSource loadSources( ActionBase action , MetadataStorage storageMeta ) {
		if( sources != null )
			return( sources );
		
		sources = new MetaSource( this , meta );
		meta.setSources( sources );
		
		if( !loadFailed ) {
			try {
				// read
				String file = storageMeta.getSourcesConfFile( action );
				action.debug( "read source definition file " + file + "..." );
				Document doc = action.readXmlFile( file );
				Node root = doc.getDocumentElement();
				sources.load( action , root );
				if( sources.isLoadFailed() )
					setLoadFailed( action , "invalid sources metadata, product=" + name );
			}
			catch( Throwable e ) {
				setLoadFailed( action , e , "unable to load source metadata, product=" + name );
			}
		}
		
		return( sources );
	}
	
	public synchronized MetaMonitoring loadMonitoring( ActionBase action , MetadataStorage storageMeta ) {
		if( mon != null )
			return( mon );
		
		mon = new MetaMonitoring( this , meta );
		
		if( !loadFailed ) {
			try {
				// read
				String file = storageMeta.getMonitoringConfFile( action );
				action.debug( "read monitoring definition file " + file + "..." );
				Document doc = action.readXmlFile( file );
				Node root = doc.getDocumentElement();
				mon.load( action , root );
				if( mon.isLoadFailed() )
					setLoadFailed( action , "invalid monitoring metadata, product=" + name );
			}
			catch( Throwable e ) {
				setLoadFailed( action , e , "unable to load monitoring metadata, product=" + name );
			}
		}
		
		return( mon );
	}
	
	public synchronized MetaEnv loadEnvData( ActionBase action , MetadataStorage storageMeta , String envName ) {
		MetaEnv env = envs.get( envName );
		if( env != null )
			return( env );
		
		env = new MetaEnv( this , meta );
		envs.put( envName , env );

		if( !loadFailed ) {
			try {
				// read
				String file = storageMeta.getEnvConfFile( action , envName );
				action.debug( "read environment definition file " + file + "..." );
				Document doc = action.readXmlFile( file );
				Node root = doc.getDocumentElement();
				env.load( action , root );
				if( env.isLoadFailed() )
					setLoadFailed( action , "invalid environment metadata, product=" + name );
			}
			catch( Throwable e ) {
				setLoadFailed( action , e , "unable to load environment metadata, product=" + name + ", env=" + envName );
			}
		}
		
		return( env );
	}
	
	public synchronized MetaDesign loadDesignData( ActionBase action , MetadataStorage storageMeta , String fileName ) {
		MetaDesign design = designFiles.get( fileName );
		if( design != null )
			return( design );
		
		design = new MetaDesign( this , meta );
		
		if( !loadFailed ) {
			try {
				// read
				String filePath = storageMeta.getDesignFile( action , fileName );
				action.debug( "read design definition file " + filePath + "..." );
				Document doc = action.readXmlFile( filePath );
				Node root = doc.getDocumentElement();
				design.load( action , root );
				designFiles.put( fileName , design );
				if( design.isLoadFailed() )
					setLoadFailed( action , "invalid monitoring metadata, product=" + name );
			}
			catch( Throwable e ) {
				setLoadFailed( action , e , "unable to load design metadata, product=" + name + ", file=" + fileName );
				design.setLoadFailed();
			}
		}
		
		return( design );
	}

	public synchronized void loadAll( ActionBase action , MetadataStorage storageMeta ) {
		loadVersion( action , storageMeta );
		loadProduct( action , storageMeta );
		loadDatabase( action , storageMeta );
		loadDistr( action , storageMeta );
		loadSources( action , storageMeta );
		loadMonitoring( action , storageMeta );
		
		meta.setVersion( version );
		meta.setProduct( product );
		meta.setDatabase( database );
		meta.setDistr( distr );
		meta.setSources( sources );
		
		if( loadFailed )
			return;
		
		try {
			for( String envFile : storageMeta.getEnvFiles( action ) )
				loadEnvData( action , storageMeta , envFile );
			for( String designFile : storageMeta.getDesignFiles( action ) )
				loadDesignData( action , storageMeta , designFile );
		}
		catch( Throwable e ) {
			setLoadFailed( action , e , "unable to load metadata, product=" + name );
		}
	}
	
	public synchronized void createInitial( TransactionBase transaction , ServerSettings settings , ServerDirectory directory ) throws Exception {
		createInitialVersion( transaction );
		createInitialProduct( transaction , settings );
		createInitialDatabase( transaction );
		createInitialDistr( transaction );
		createInitialSources( transaction );
		createInitialMonitoring( transaction );
	}

	private void createInitialVersion( TransactionBase transaction ) throws Exception {
		version = new MetaProductVersion( this , meta );
		version.createVersion( transaction , 1 , 0 , 1 , 1 , 1 , 2 );
		meta.setVersion( version );
	}
	
	private void createInitialProduct( TransactionBase transaction , ServerSettings settings ) throws Exception {
		product = new MetaProductSettings( this , meta , settings.serverContext.execprops );
		
		ServerProductContext productContext = new ServerProductContext( meta );
		productContext.create( transaction.action , version );
		
		product.createSettings( transaction , settings , productContext );
		meta.setProduct( product );
	}
	
	private void createInitialDatabase( TransactionBase transaction ) throws Exception {
		database = new MetaDatabase( this , meta );
		database.createDatabase( transaction );
		meta.setDatabase( database );
	}
	
	private void createInitialDistr( TransactionBase transaction ) throws Exception {
		distr = new MetaDistr( this , meta );
		distr.createDistr( transaction );
		meta.setDistr( distr );
	}
	
	private void createInitialSources( TransactionBase transaction ) throws Exception {
		sources = new MetaSource( this , meta );
		sources.createSources( transaction );
		meta.setSources( sources );
	}
	
	private void createInitialMonitoring( TransactionBase transaction ) throws Exception {
		mon = new MetaMonitoring( this , meta );
		mon.createMonitoring( transaction );
	}
	
	public void saveAll( ActionBase action , MetadataStorage storageMeta ) throws Exception {
		saveVersion( action , storageMeta );
		saveProduct( action , storageMeta );
		saveDatabase( action , storageMeta );
		saveDistr( action , storageMeta );
		saveSources( action , storageMeta );
		saveMonitoring( action , storageMeta );
		
		for( String envFile : envs.keySet() )
			saveEnvData( action , storageMeta , envFile , envs.get( envFile ) );
		for( String designFile : designFiles.keySet() )
			saveDesignData( action , storageMeta , designFile , designFiles.get( designFile ) );
	}
	
	public void saveVersion( ActionBase action , MetadataStorage storageMeta ) throws Exception {
		Document doc = Common.xmlCreateDoc( XML_ROOT_VERSION );
		version.save( action , doc , doc.getDocumentElement() );
		storageMeta.saveVersionConfFile( action , doc );
	}
	
	public void saveProduct( ActionBase action , MetadataStorage storageMeta ) throws Exception {
		Document doc = Common.xmlCreateDoc( XML_ROOT_PRODUCT );
		product.save( action , doc , doc.getDocumentElement() );
		storageMeta.saveProductConfFile( action , doc );
	}
	
	public void saveDatabase( ActionBase action , MetadataStorage storageMeta ) throws Exception {
		Document doc = Common.xmlCreateDoc( XML_ROOT_DATABASE );
		database.save( action , doc , doc.getDocumentElement() );
		storageMeta.saveDatabaseConfFile( action , doc );
	}
	
	public void saveDistr( ActionBase action , MetadataStorage storageMeta ) throws Exception {
		Document doc = Common.xmlCreateDoc( XML_ROOT_DISTR );
		distr.save( action , doc , doc.getDocumentElement() );
		storageMeta.saveDistrConfFile( action , doc );
	}
	
	public void saveSources( ActionBase action , MetadataStorage storageMeta ) throws Exception {
		Document doc = Common.xmlCreateDoc( XML_ROOT_SOURCES );
		sources.save( action , doc , doc.getDocumentElement() );
		storageMeta.saveSourcesConfFile( action , doc );
	}
	
	public void saveMonitoring( ActionBase action , MetadataStorage storageMeta ) throws Exception {
		Document doc = Common.xmlCreateDoc( XML_ROOT_MONITORING );
		sources.save( action , doc , doc.getDocumentElement() );
		storageMeta.saveMonitoringConfFile( action , doc );
	}
	
	public void saveEnvData( ActionBase action , MetadataStorage storageMeta , String envFile , MetaEnv env ) throws Exception {
		Document doc = Common.xmlCreateDoc( XML_ROOT_ENV );
		env.save( action , doc , doc.getDocumentElement() );
		storageMeta.saveEnvConfFile( action , doc , envFile );
	}
	
	public void saveDesignData( ActionBase action , MetadataStorage storageMeta , String designFile , MetaDesign design ) throws Exception {
	}
	
	public void setVersion( ServerTransaction transaction , MetaProductVersion version ) throws Exception {
		this.version.deleteObject();
		this.version = version;
	}

	public void addEnv( ServerTransaction transaction , MetaEnv env ) throws Exception {
		String envFile = env.ID + ".xml";
		envs.put( envFile , env );
	}

	public MetaProductVersion getVersion() {
		return( version );
	}
	
	public MetaProductSettings getProductSettings() {
		return( product );
	}
	
	public MetaDatabase getDatabase() {
		return( database );
	}
	
	public MetaDistr getDistr() {
		return( distr );
	}
	
	public MetaSource getSources() {
		return( sources );
	}
	
	public MetaMonitoring getMonitoring() {
		return( mon );
	}
	
	public synchronized String[] getEnvironments() {
		List<String> names = new LinkedList<String>();
		for( MetaEnv env : envs.values() )
			names.add( env.ID );
		Collections.sort( names );
		return( names.toArray( new String[0] ) );
	}

	public synchronized MetaEnv findEnvironment( String envId ) {
		for( MetaEnv env : envs.values() ) {
			if( env.ID.equals( envId ) )
				return( env );
		}
		return( null );
	}

	public void deleteEnv( ServerTransaction transaction , MetaEnv env ) throws Exception {
		String envFile = env.ID + ".xml";
		envs.remove( envFile );
		ActionBase action = transaction.getAction();
		MetadataStorage storage = action.artefactory.getMetadataStorage( action , env.meta );
		storage.deleteEnvConfFile( action , envFile );
		env.deleteObject();
	}

}
