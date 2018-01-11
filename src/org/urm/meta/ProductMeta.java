package org.urm.meta;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.urm.action.ActionBase;
import org.urm.common.Common;
import org.urm.common.ConfReader;
import org.urm.engine.Engine;
import org.urm.engine.EngineSession;
import org.urm.engine.EngineTransaction;
import org.urm.engine.TransactionBase;
import org.urm.engine.dist.DistRepository;
import org.urm.engine.properties.ObjectProperties;
import org.urm.engine.storage.MetadataStorage;
import org.urm.meta.engine.AppProduct;
import org.urm.meta.engine.AppSystem;
import org.urm.meta.engine.EngineDirectory;
import org.urm.meta.engine.EngineProducts;
import org.urm.meta.engine.HostAccount;
import org.urm.meta.engine.EngineSettings;
import org.urm.meta.product.Meta;
import org.urm.meta.product.MetaDatabase;
import org.urm.meta.product.MetaDesignDiagram;
import org.urm.meta.product.MetaDistr;
import org.urm.meta.product.MetaDocs;
import org.urm.meta.product.MetaEnv;
import org.urm.meta.product.MetaMonitoring;
import org.urm.meta.product.MetaProductSettings;
import org.urm.meta.product.MetaProductVersion;
import org.urm.meta.product.MetaSource;
import org.urm.meta.product.MetaUnits;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class ProductMeta extends EngineObject {

	public Engine engine;
	public EngineProducts products;
	public String name;
	
	public Meta meta;
	
	private MetaProductVersion version;
	private MetaProductSettings productSettings;
	private MetaUnits units;
	private MetaDatabase database;
	private MetaSource sources;
	private MetaDocs docs;
	private MetaDistr distr;
	private MetaMonitoring mon;

	private DistRepository repo;
	
	private Map<String,MetaEnv> envs;
	private Map<String,MetaDesignDiagram> designFiles;
	
	public static String XML_ROOT_VERSION = "version";
	public static String XML_ROOT_PRODUCT = "product";
	public static String XML_ROOT_DISTR = "distributive";
	public static String XML_ROOT_DATABASE = "database";
	public static String XML_ROOT_SOURCES = "sources";
	public static String XML_ROOT_MONITORING = "monitoring";
	public static String XML_ROOT_ENV = "environment";
	public static String XML_ROOT_DESIGN = "design";

	public boolean loadFailed;
	
	private Map<EngineSession,Meta> sessionMeta;
	private boolean primary;
	
	public ProductMeta( EngineProducts products , String name ) {
		super( null );
		this.products = products;
		this.engine = products.engine;
		this.name = name;
		
		meta = new Meta( this , null );
		engine.trace( "new product storage meta object, id=" + meta.objectId + ", storage=" + objectId );
		designFiles = new HashMap<String,MetaDesignDiagram>();
		envs = new HashMap<String,MetaEnv>();
		
		loadFailed = false;
		sessionMeta = new HashMap<EngineSession,Meta>();
		primary = false;
	}

	@Override
	public String getName() {
		return( name );
	}
	
	public DistRepository getDistRepository( ActionBase action ) {
		return( repo );
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

	public synchronized Meta findSessionMeta( EngineSession session ) {
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
	
	public synchronized ProductMeta copy( ActionBase action ) throws Exception {
		ProductMeta r = new ProductMeta( products , name );
		
		if( version != null ) {
			r.version = version.copy( action , r.meta );
			if( r.version.isLoadFailed() )
				r.loadFailed = true;
		}
		if( productSettings != null ) {
			r.productSettings = productSettings.copy( action , r.meta );
			if( r.productSettings.isLoadFailed() )
				r.loadFailed = true;
		}
		if( units != null ) {
			r.units = units.copy( action , r.meta );
			if( r.units.isLoadFailed() )
				r.loadFailed = true;
		}
		if( database != null ) {
			r.database = database.copy( action , r.meta );
			if( r.database.isLoadFailed() )
				r.loadFailed = true;
		}
		if( sources != null ) {
			r.sources = sources.copy( action , r.meta );
			if( r.sources.isLoadFailed() )
				r.loadFailed = true;
		}
		if( docs != null ) {
			r.docs = docs.copy( action , r.meta );
			if( r.docs.isLoadFailed() )
				r.loadFailed = true;
		}
		if( distr != null ) {
			r.distr = distr.copy( action , r.meta , r.database , r.docs );
			if( r.distr.isLoadFailed() )
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
			MetaDesignDiagram design = designFiles.get( designFile );
			MetaDesignDiagram rd = design.copy( action , r.meta );
			r.designFiles.put( designFile , rd );
			if( rd.loadFailed )
				r.loadFailed = true;
		}
		
		r.repo = repo.copy( action , r.meta );
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

	public synchronized MetaProductVersion loadVersion( EngineLoader loader , MetadataStorage storageMeta ) {
		if( version != null )
			return( version );
		
		version = new MetaProductVersion( this , meta );

		if( !loadFailed ) {
			ActionBase action = loader.getAction();
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

	public synchronized MetaProductSettings loadProduct( EngineLoader loader , MetadataStorage storageMeta ) {
		if( productSettings != null )
			return( productSettings );
		
		if( !loadFailed ) {
			ActionBase action = loader.getAction();
			try {
				EngineDirectory directory = loader.getDirectory();
				AppProduct product = directory.getProduct( meta.name );
				AppSystem system = product.system;
				ObjectProperties systemProps = system.getParameters();
				productSettings = new MetaProductSettings( this , meta , systemProps.getProperties() );

				ProductContext productContext = new ProductContext( meta );
				productContext.create( action , version );
				
				// read
				String file = storageMeta.getProductConfFile( action );
				action.debug( "read product definition file " + file + "..." );
				Document doc = ConfReader.readXmlFile( action.session.execrc , file );
				Node root = doc.getDocumentElement();
				productSettings.load( action , productContext , root );
				if( productSettings.isLoadFailed() )
					setLoadFailed( action , "invalid settings metadata, product=" + name );
			}
			catch( Throwable e ) {
				setLoadFailed( action , e , "unable to load settings metadata, product=" + name );
			}
		}
		
		return( productSettings );
	}
	
	public synchronized MetaUnits loadUnits( EngineLoader loader , MetadataStorage storageMeta ) {
		if( units != null )
			return( units );
		
		MetaProductSettings settings = loadProduct( loader , storageMeta );
		units = new MetaUnits( this , settings , meta );
		
		if( !loadFailed ) {
			ActionBase action = loader.getAction();
			try {
				// read
				String file = storageMeta.getProductConfFile( action );
				action.debug( "read units definition file " + file + "..." );
				Document doc = action.readXmlFile( file );
				Node root = doc.getDocumentElement();
				Node node = ConfReader.xmlGetFirstChild( root , "units" );
				units.load( action , node );
				
				if( units.isLoadFailed() )
					setLoadFailed( action , "invalid units metadata, product=" + name );
			}
			catch( Throwable e ) {
				setLoadFailed( action , e , "unable to load units metadata, product=" + name );
			}
		}
		
		return( units );
	}
	
	public synchronized MetaDatabase loadDatabase( EngineLoader loader , MetadataStorage storageMeta ) {
		if( database != null )
			return( database );
		
		MetaProductSettings settings = loadProduct( loader , storageMeta );
		database = new MetaDatabase( this , settings , meta );
		
		if( !loadFailed ) {
			ActionBase action = loader.getAction();
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
	
	public synchronized MetaDocs loadDocs( EngineLoader loader , MetadataStorage storageMeta ) {
		if( docs != null )
			return( docs );
		
		MetaProductSettings settings = loadProduct( loader , storageMeta );
		docs = new MetaDocs( this , settings , meta );
		
		if( !loadFailed ) {
			ActionBase action = loader.getAction();
			try {
				// read
				String file = storageMeta.getProductConfFile( action );
				action.debug( "read units definition file " + file + "..." );
				Document doc = action.readXmlFile( file );
				Node root = doc.getDocumentElement();
				Node node = ConfReader.xmlGetFirstChild( root , "documentation" );
				docs.load( action , node );
				
				if( docs.isLoadFailed() )
					setLoadFailed( action , "invalid documentation metadata, product=" + name );
			}
			catch( Throwable e ) {
				setLoadFailed( action , e , "unable to load documentation metadata, product=" + name );
			}
		}
		
		return( docs );
	}
	
	public synchronized MetaDistr loadDistr( EngineLoader loader , MetadataStorage storageMeta ) {
		if( distr != null )
			return( distr );
		
		MetaProductSettings settings = loadProduct( loader , storageMeta );
		MetaDatabase db = loadDatabase( loader , storageMeta );
		MetaDocs docs = loadDocs( loader , storageMeta );
		distr = new MetaDistr( this , settings , meta );
		meta.setDistr( distr );
		
		if( !loadFailed ) {
			ActionBase action = loader.getAction();
			try {
				// read
				String file = storageMeta.getDistrConfFile( action );
				action.debug( "read distributive definition file " + file + "..." );
				Document doc = action.readXmlFile( file );
				Node root = doc.getDocumentElement();
				distr.load( action , db , docs , root );
				if( distr.isLoadFailed() )
					setLoadFailed( action , "invalid distributive metadata, product=" + name );
			}
			catch( Throwable e ) {
				setLoadFailed( action , e , "unable to load distributive metadata, product=" + name );
			}
		}
		
		return( distr );
	}

	public synchronized MetaSource loadSources( EngineLoader loader , MetadataStorage storageMeta ) {
		if( sources != null )
			return( sources );
		
		MetaProductSettings settings = loadProduct( loader , storageMeta );
		sources = new MetaSource( this , settings , meta );
		meta.setSources( sources );
		
		if( !loadFailed ) {
			ActionBase action = loader.getAction();
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
	
	public synchronized MetaMonitoring loadMonitoring( EngineLoader loader , MetadataStorage storageMeta ) {
		if( mon != null )
			return( mon );
		
		MetaProductSettings settings = loadProduct( loader , storageMeta );
		mon = new MetaMonitoring( this , settings , meta );
		
		if( !loadFailed ) {
			ActionBase action = loader.getAction();
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
	
	public synchronized MetaEnv loadEnvData( EngineLoader loader , MetadataStorage storageMeta , String envName ) {
		MetaEnv env = envs.get( envName );
		if( env != null )
			return( env );
		
		MetaProductSettings settings = loadProduct( loader , storageMeta );
		env = new MetaEnv( this , settings , meta );
		envs.put( envName , env );

		if( !loadFailed ) {
			ActionBase action = loader.getAction();
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
	
	public synchronized MetaDesignDiagram loadDesignData( EngineLoader loader , MetadataStorage storageMeta , String fileName ) {
		MetaDesignDiagram design = designFiles.get( fileName );
		if( design != null )
			return( design );
		
		design = new MetaDesignDiagram( this , meta );
		
		if( !loadFailed ) {
			ActionBase action = loader.getAction();
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

	public synchronized void loadAll( EngineLoader loader , MetadataStorage storageMeta , boolean importxml ) {
		ActionBase action = loader.getAction();
		
		loadVersion( loader , storageMeta );
		loadProduct( loader , storageMeta );
		loadUnits( loader , storageMeta );
		loadDatabase( loader , storageMeta );
		loadSources( loader , storageMeta );
		loadDocs( loader , storageMeta );
		loadDistr( loader , storageMeta );
		loadMonitoring( loader , storageMeta );
		
		meta.setVersion( version );
		meta.setProduct( productSettings );
		meta.setUnits( units );
		meta.setDatabase( database );
		meta.setDocs( docs );
		meta.setDistr( distr );
		meta.setSources( sources );
		
		if( loadFailed )
			return;
		
		try {
			for( String envFile : storageMeta.getEnvFiles( action ) )
				loadEnvData( loader , storageMeta , envFile );
			for( String designFile : storageMeta.getDesignFiles( action ) )
				loadDesignData( loader , storageMeta , designFile );
			
			repo = DistRepository.loadDistRepository( action , meta , importxml );
		}
		catch( Throwable e ) {
			setLoadFailed( action , e , "unable to load metadata, product=" + name );
		}
	}
	
	public synchronized void createInitial( TransactionBase transaction , EngineSettings settings , AppProduct product ) throws Exception {
		createInitialVersion( transaction );
		createInitialProduct( transaction , settings , product );
		createInitialUnits( transaction );
		createInitialDatabase( transaction );
		createInitialSources( transaction );
		createInitialDocs( transaction );
		createInitialDistr( transaction );
		createInitialMonitoring( transaction );
	}

	private void createInitialVersion( TransactionBase transaction ) throws Exception {
		version = new MetaProductVersion( this , meta );
		version.createVersion( transaction , 1 , 0 , 1 , 1 , 1 , 2 );
		meta.setVersion( version );
	}
	
	private void createInitialProduct( TransactionBase transaction , EngineSettings settings , AppProduct product ) throws Exception {
		ObjectProperties systemProps = product.system.getParameters();
		productSettings = new MetaProductSettings( this , meta , systemProps.getProperties() );
		
		ProductContext productContext = new ProductContext( meta );
		productContext.create( transaction.action , version );
		
		productSettings.createSettings( transaction , settings , productContext );
		meta.setProduct( productSettings );
	}
	
	private void createInitialUnits( TransactionBase transaction ) throws Exception {
		units = new MetaUnits( this , productSettings , meta );
		units.createUnits( transaction );
		meta.setUnits( units );
	}
	
	private void createInitialDatabase( TransactionBase transaction ) throws Exception {
		database = new MetaDatabase( this , productSettings , meta );
		database.createDatabase( transaction );
		meta.setDatabase( database );
	}
	
	private void createInitialDocs( TransactionBase transaction ) throws Exception {
		docs = new MetaDocs( this , productSettings , meta );
		docs.createDocs( transaction );
		meta.setDocs( docs );
	}
	
	private void createInitialDistr( TransactionBase transaction ) throws Exception {
		distr = new MetaDistr( this , productSettings , meta );
		distr.createDistr( transaction );
		meta.setDistr( distr );
	}
	
	private void createInitialSources( TransactionBase transaction ) throws Exception {
		sources = new MetaSource( this , productSettings , meta );
		sources.createSources( transaction );
		meta.setSources( sources );
	}
	
	private void createInitialMonitoring( TransactionBase transaction ) throws Exception {
		mon = new MetaMonitoring( this , productSettings , meta );
		mon.createMonitoring( transaction );
	}

	public void createInitialRepository( TransactionBase transaction , boolean forceClear ) throws Exception {
		repo = DistRepository.createInitialRepository( transaction.action , meta , forceClear );
	}

	public DistRepository getDistRepository() {
		return( repo );
	}
	
	public void saveAll( ActionBase action , MetadataStorage storageMeta ) throws Exception {
		saveVersion( action , storageMeta );
		saveProduct( action , storageMeta );
		saveDatabase( action , storageMeta );
		saveSources( action , storageMeta );
		saveDistr( action , storageMeta );
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
		Element root = doc.getDocumentElement();
		productSettings.save( action , doc , root );
		
		Element node = Common.xmlCreateElement( doc , root , "units" );
		units.save( action , doc , node );
		
		node = Common.xmlCreateElement( doc , root , "documentation" );
		docs.save( action , doc , node );
		
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
		mon.save( action , doc , doc.getDocumentElement() );
		storageMeta.saveMonitoringConfFile( action , doc );
	}
	
	public void saveEnvData( ActionBase action , MetadataStorage storageMeta , String envFile , MetaEnv env ) throws Exception {
		Document doc = Common.xmlCreateDoc( XML_ROOT_ENV );
		env.save( action , doc , doc.getDocumentElement() );
		storageMeta.saveEnvConfFile( action , doc , envFile );
	}
	
	public void saveDesignData( ActionBase action , MetadataStorage storageMeta , String designFile , MetaDesignDiagram design ) throws Exception {
		Document doc = Common.xmlCreateDoc( XML_ROOT_DESIGN );
		design.save( action , doc , doc.getDocumentElement() );
		storageMeta.saveEnvConfFile( action , doc , designFile );
	}
	
	public void setVersion( EngineTransaction transaction , MetaProductVersion version ) throws Exception {
		this.version.deleteObject();
		this.version = version;
	}

	public void addEnv( EngineTransaction transaction , MetaEnv env ) throws Exception {
		String envFile = env.NAME + ".xml";
		envs.put( envFile , env );
	}

	public MetaProductVersion getVersion() {
		return( version );
	}
	
	public MetaProductSettings getProductSettings() {
		return( productSettings );
	}
	
	public MetaUnits getUnits() {
		return( units );
	}
	
	public MetaDatabase getDatabase() {
		return( database );
	}
	
	public MetaDocs getDocs() {
		return( docs );
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
	
	public String[] getEnvironmentNames() {
		List<String> names = new LinkedList<String>();
		for( MetaEnv env : envs.values() )
			names.add( env.NAME );
		Collections.sort( names );
		return( names.toArray( new String[0] ) );
	}

	public MetaEnv findEnvironment( String envId ) {
		for( MetaEnv env : envs.values() ) {
			if( env.NAME.equals( envId ) )
				return( env );
		}
		return( null );
	}

	public MetaEnv[] getEnvironments() {
		return( envs.values().toArray( new MetaEnv[0] ) );
	}
	
	public void deleteEnv( EngineTransaction transaction , MetaEnv env ) throws Exception {
		String envFile = env.NAME + ".xml";
		envs.remove( envFile );
		ActionBase action = transaction.getAction();
		MetadataStorage storage = action.artefactory.getMetadataStorage( action , env.meta );
		storage.deleteEnvConfFile( action , envFile );
		env.deleteObject();
	}

	public void deleteHostAccount( EngineTransaction transaction , HostAccount account ) throws Exception {
		for( MetaEnv env : envs.values() )
			env.deleteHostAccount( transaction , account );
	}

}
