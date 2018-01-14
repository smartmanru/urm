package org.urm.meta;

import org.urm.action.ActionBase;
import org.urm.common.Common;
import org.urm.common.ConfReader;
import org.urm.db.product.DBProductVersion;
import org.urm.engine.storage.MetadataStorage;
import org.urm.meta.engine.AppProduct;
import org.urm.meta.engine.EngineDirectory;
import org.urm.meta.product.Meta;
import org.urm.meta.product.MetaDatabase;
import org.urm.meta.product.MetaDistr;
import org.urm.meta.product.MetaDocs;
import org.urm.meta.product.MetaMonitoring;
import org.urm.meta.product.MetaProductPolicy;
import org.urm.meta.product.MetaProductSettings;
import org.urm.meta.product.MetaProductVersion;
import org.urm.meta.product.MetaSource;
import org.urm.meta.product.MetaUnits;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class EngineLoaderMeta {

	public static String XML_ROOT_VERSION = "version";
	public static String XML_ROOT_SETTINGS = "product";
	public static String XML_ROOT_POLICY = "product";
	public static String XML_ROOT_DISTR = "distributive";
	public static String XML_ROOT_DATABASE = "database";
	public static String XML_ROOT_SOURCES = "sources";
	public static String XML_ROOT_MONITORING = "monitoring";
	
	public EngineLoader loader;
	public ProductMeta set;
	public Meta meta;

	public MetaProductVersion versionNew;
	
	public EngineLoaderMeta( EngineLoader loader , ProductMeta set ) {
		this.loader = loader;
		this.set = set;
		this.meta = set.meta;
	}

	public void saveAll( MetadataStorage ms ) throws Exception {
		saveVersion( ms );
		saveProduct( ms );
		saveDatabase( ms );
		saveSources( ms );
		saveDistr( ms );
		saveMonitoring( ms );
	}
	
	public void loaddbAll() throws Exception {
		loaddbVersion();
		loaddbSettings();
		loaddbPolicy();
		loaddbUnits();
		loaddbDatabase();
		loaddbSources();
		loaddbDocs();
		loaddbDistr();
		loaddbMonitoring();
	}

	public void importxmlAll( MetadataStorage storageMeta ) throws Exception {
		importxmlVersion( storageMeta );
		importxmlSettings( storageMeta );
		importxmlPolicy( storageMeta );
		importxmlUnits( storageMeta );
		importxlDatabase( storageMeta );
		importxmlSources( storageMeta );
		importxmlDocs( storageMeta );
		importxmlDistr( storageMeta );
		importxmlMonitoring( storageMeta );
	}
	
	private void saveVersion( MetadataStorage storageMeta ) throws Exception {
		ActionBase action = loader.getAction();
		Document doc = Common.xmlCreateDoc( XML_ROOT_VERSION );
		//MetaProductVersion version = set.getVersion();
		//version.save( action , doc , doc.getDocumentElement() );
		storageMeta.saveVersionConfFile( action , doc );
	}
	
	private void saveProduct( MetadataStorage storageMeta ) throws Exception {
		ActionBase action = loader.getAction();
		Document doc = Common.xmlCreateDoc( XML_ROOT_SETTINGS );
		Element root = doc.getDocumentElement();
		MetaProductSettings settings = set.getSettings();
		settings.save( action , doc , root );
		
		Element node = Common.xmlCreateElement( doc , root , "units" );
		MetaUnits units = set.getUnits();
		units.save( action , doc , node );
		
		node = Common.xmlCreateElement( doc , root , "documentation" );
		MetaDocs docs = set.getDocs();
		docs.save( action , doc , node );
		
		storageMeta.saveCoreConfFile( action , doc );
	}
	
	private void saveDatabase( MetadataStorage storageMeta ) throws Exception {
		ActionBase action = loader.getAction();
		Document doc = Common.xmlCreateDoc( XML_ROOT_DATABASE );
		MetaDatabase database = set.getDatabase();
		database.save( action , doc , doc.getDocumentElement() );
		storageMeta.saveDatabaseConfFile( action , doc );
	}
	
	private void saveDistr( MetadataStorage storageMeta ) throws Exception {
		ActionBase action = loader.getAction();
		Document doc = Common.xmlCreateDoc( XML_ROOT_DISTR );
		MetaDistr distr = set.getDistr();
		distr.save( action , doc , doc.getDocumentElement() );
		storageMeta.saveDistrConfFile( action , doc );
	}
	
	private void saveSources( MetadataStorage storageMeta ) throws Exception {
		ActionBase action = loader.getAction();
		Document doc = Common.xmlCreateDoc( XML_ROOT_SOURCES );
		MetaSource sources = set.getSources();
		sources.save( action , doc , doc.getDocumentElement() );
		storageMeta.saveSourcesConfFile( action , doc );
	}
	
	private void saveMonitoring( MetadataStorage storageMeta ) throws Exception {
		ActionBase action = loader.getAction();
		Document doc = Common.xmlCreateDoc( XML_ROOT_MONITORING );
		MetaMonitoring mon = set.getMonitoring();
		mon.save( action , doc , doc.getDocumentElement() );
		storageMeta.saveMonitoringConfFile( action , doc );
	}
	
	private void loaddbVersion() throws Exception {
		trace( "load engine settings data ..." );
		versionNew = new MetaProductVersion( set , meta );
		DBProductVersion.loaddb( loader , versionNew );
		set.setVersion( versionNew );
	}

	private void loaddbSettings() throws Exception {
	}
	
	private void loaddbPolicy() throws Exception {
	}
	
	private void loaddbUnits() throws Exception {
	}
	
	private void loaddbDatabase() throws Exception {
	}
	
	private void loaddbSources() throws Exception {
	}
	
	private void loaddbDocs() throws Exception {
	}
	
	private void loaddbDistr() throws Exception {
	}
	
	private void loaddbMonitoring() throws Exception {
	}
	
	private void importxmlVersion( MetadataStorage storageMeta ) throws Exception {
		MetaProductVersion version = new MetaProductVersion( set , set.meta );
		set.setVersion( version );

		ActionBase action = loader.getAction();
		try {
			// read
			String file = storageMeta.getVersionConfFile( action );
			action.debug( "read product version file " + file + "..." );
			//Document doc = ConfReader.readXmlFile( action.session.execrc , file );
			//Node root = doc.getDocumentElement();
			//version.load( action , root );
		}
		catch( Throwable e ) {
			setLoadFailed( action , _Error.UnableLoadProductVersion1 , e , "unable to load version metadata, product=" + set.name , set.name );
		}
	}

	private void importxmlSettings( MetadataStorage storageMeta ) throws Exception {
		EngineDirectory directory = loader.getDirectory();
		AppProduct product = directory.getProduct( set.name );
		
		MetaProductSettings settings = new MetaProductSettings( set , set.meta );
		set.setSettings( settings );

		ActionBase action = loader.getAction();
		try {
			ProductContext productContext = new ProductContext( product );
			productContext.create( action , set.getVersion() );
			
			// read
			String file = storageMeta.getCoreConfFile( action );
			action.debug( "read product definition file " + file + "..." );
			Document doc = ConfReader.readXmlFile( action.session.execrc , file );
			Node root = doc.getDocumentElement();
			settings.load( action , productContext , root );
		}
		catch( Throwable e ) {
			setLoadFailed( action , _Error.UnableLoadProductSettings1 , e , "unable to load settings metadata, product=" + set.name , set.name );
		}
	}
	
	private void importxmlPolicy( MetadataStorage storageMeta ) throws Exception {
		MetaProductPolicy policy = new MetaProductPolicy( set , set.meta );
		set.setPolicy( policy );

		ActionBase action = loader.getAction();
		try {
			// read
			String file = storageMeta.getPolicyConfFile( action );
			action.debug( "read product policy file " + file + "..." );
			Document doc = ConfReader.readXmlFile( action.session.execrc , file );
			Node root = doc.getDocumentElement();
			policy.load( action , root );
		}
		catch( Throwable e ) {
			setLoadFailed( action , _Error.UnableLoadProductVersion1 , e , "unable to load version metadata, product=" + set.name , set.name );
		}
	}

	private void importxmlUnits( MetadataStorage storageMeta ) throws Exception {
		MetaProductSettings settings = set.getSettings();
		
		MetaUnits units = new MetaUnits( set , settings , set.meta );
		set.setUnits( units );
		
		ActionBase action = loader.getAction();
		try {
			// read
			String file = storageMeta.getCoreConfFile( action );
			action.debug( "read units definition file " + file + "..." );
			Document doc = action.readXmlFile( file );
			Node root = doc.getDocumentElement();
			Node node = ConfReader.xmlGetFirstChild( root , "units" );
			units.load( action , node );
		}
		catch( Throwable e ) {
			setLoadFailed( action , _Error.UnableLoadProductUnits1 , e , "unable to load units metadata, product=" + set.name , set.name );
		}
	}
	
	private void importxlDatabase( MetadataStorage storageMeta ) throws Exception {
		MetaProductSettings settings = set.getSettings();
		
		MetaDatabase database = new MetaDatabase( set , settings , set.meta );
		set.setDatabase( database );
		
		ActionBase action = loader.getAction();
		try {
			// read
			String file = storageMeta.getDatabaseConfFile( action );
			action.debug( "read database definition file " + file + "..." );
			Document doc = action.readXmlFile( file );
			Node root = doc.getDocumentElement();
			database.load( action , root );
		}
		catch( Throwable e ) {
			setLoadFailed( action , _Error.UnableLoadProductDatabase1 , e , "unable to load database metadata, product=" + set.name , set.name );
		}
	}
	
	private void importxmlSources( MetadataStorage storageMeta ) throws Exception {
		MetaProductSettings settings = set.getSettings();
		MetaSource sources = new MetaSource( set , settings , set.meta );
		set.setSources( sources );
		
		ActionBase action = loader.getAction();
		try {
			// read
			String file = storageMeta.getSourcesConfFile( action );
			action.debug( "read source definition file " + file + "..." );
			Document doc = action.readXmlFile( file );
			Node root = doc.getDocumentElement();
			sources.load( action , root );
		}
		catch( Throwable e ) {
			setLoadFailed( action , _Error.UnableLoadProductSources1 , e , "unable to load source metadata, product=" + set.name , set.name );
		}
	}
	
	private void importxmlDocs( MetadataStorage storageMeta ) throws Exception {
		MetaProductSettings settings = set.getSettings();
		MetaDocs docs = new MetaDocs( set , settings , set.meta );
		set.setDocs( docs );
		
		ActionBase action = loader.getAction();
		try {
			// read
			String file = storageMeta.getCoreConfFile( action );
			action.debug( "read units definition file " + file + "..." );
			Document doc = action.readXmlFile( file );
			Node root = doc.getDocumentElement();
			Node node = ConfReader.xmlGetFirstChild( root , "documentation" );
			docs.load( action , node );
		}
		catch( Throwable e ) {
			setLoadFailed( action , _Error.UnableLoadProductDocs1 , e , "unable to load documentation metadata, product=" + set.name , set.name );
		}
	}
	
	private void importxmlDistr( MetadataStorage storageMeta ) throws Exception {
		MetaProductSettings settings = set.getSettings();
		MetaDatabase db = set.getDatabase();
		MetaDocs docs = set.getDocs();
		MetaDistr distr = new MetaDistr( set , settings , set.meta );
		set.setDistr( distr );
		
		ActionBase action = loader.getAction();
		try {
			// read
			String file = storageMeta.getDistrConfFile( action );
			action.debug( "read distributive definition file " + file + "..." );
			Document doc = action.readXmlFile( file );
			Node root = doc.getDocumentElement();
			distr.load( action , db , docs , root );
		}
		catch( Throwable e ) {
			setLoadFailed( action , _Error.UnableLoadProductDistr1 , e , "unable to load distributive metadata, product=" + set.name , set.name );
		}
	}

	private void importxmlMonitoring( MetadataStorage storageMeta ) throws Exception {
		MetaProductSettings settings = set.getSettings();
		MetaMonitoring mon = new MetaMonitoring( set , settings , set.meta );
		set.setMonitoring( mon );
		
		ActionBase action = loader.getAction();
		try {
			// read
			String file = storageMeta.getMonitoringConfFile( action );
			action.debug( "read monitoring definition file " + file + "..." );
			Document doc = action.readXmlFile( file );
			Node root = doc.getDocumentElement();
			mon.load( action , root );
		}
		catch( Throwable e ) {
			setLoadFailed( action , _Error.UnableLoadProductMonitoring1 , e , "unable to load monitoring metadata, product=" + set.name , set.name );
		}
	}
	
	public void trace( String s ) {
		loader.trace( s );
	}

	private void setLoadFailed( ActionBase action , int error , Throwable e , String msg , String product ) throws Exception {
		loader.log( msg ,  e );
		Common.exit1( error , msg , product );
	}
	
}
