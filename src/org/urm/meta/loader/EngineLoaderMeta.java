package org.urm.meta.loader;

import org.urm.action.ActionBase;
import org.urm.common.Common;
import org.urm.common.ConfReader;
import org.urm.db.DBConnection;
import org.urm.db.product.DBMeta;
import org.urm.db.product.DBMetaDatabase;
import org.urm.db.product.DBMetaDistr;
import org.urm.db.product.DBMetaDocs;
import org.urm.db.product.DBMetaSettings;
import org.urm.db.product.DBMetaSources;
import org.urm.db.product.DBMetaUnits;
import org.urm.engine.data.EngineProducts;
import org.urm.engine.products.EngineProduct;
import org.urm.engine.storage.ProductStorage;
import org.urm.engine.transaction.TransactionBase;
import org.urm.meta.product.Meta;
import org.urm.meta.product.MetaDesignDiagram;
import org.urm.meta.product.MetaDocs;
import org.urm.meta.product.ProductContext;
import org.urm.meta.product.ProductMeta;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class EngineLoaderMeta {

	public static String XML_ROOT_DESIGN = "design";
	public static String XML_ROOT_VERSION = "version";
	public static String XML_ROOT_SETTINGS = "product";
	public static String XML_ROOT_POLICY = "product";
	public static String XML_ROOT_DISTR = "distributive";
	public static String XML_ROOT_DATABASE = "database";
	public static String XML_ROOT_DOCS = "docs";
	public static String XML_ROOT_SOURCES = "sources";
	public static String XML_ROOT_UNITS = "units";
	
	public EngineLoader loader;
	public ProductMeta set;
	public Meta meta;

	public EngineLoaderMeta( EngineLoader loader , ProductMeta set ) {
		this.loader = loader;
		this.set = set;
		this.meta = set.meta;
	}

	public void loadDesignDocs( ProductStorage ms ) throws Exception {
		ActionBase action = loader.getAction();
		for( String designFile : ms.getDesignFiles( action ) )
			loadDesignData( ms , designFile );
	}
	
	private void loadDesignData( ProductStorage storageMeta , String diagramName ) throws Exception {
		MetaDesignDiagram diagram = new MetaDesignDiagram( set , set.meta );
		MetaDocs docs = set.getDocs();
		
		ActionBase action = loader.getAction();
		try {
			// read
			String filePath = storageMeta.getDesignFile( action , diagramName );
			action.debug( "read design definition file " + filePath + "..." );
			Document doc = action.readXmlFile( filePath );
			Node root = doc.getDocumentElement();
			diagram.load( action , root );
			docs.addDiagram( diagram );
		}
		catch( Throwable e ) {
			loader.setLoadFailed( action , _Error.UnableLoadProductDiagram2 , e , "unable to load design metadata, product=" + set.NAME + ", diagram=" + diagramName , set.NAME , diagramName );
		}
	}

	public void saveDesignDocs( ProductStorage ms ) throws Exception {
		MetaDocs docs = set.getDocs();
		for( String diagramName : docs.getDiagramNames() ) {
			MetaDesignDiagram diagram = docs.findDiagram( diagramName );
			saveDesignData( ms , diagram );
		}
	}
	
	private void saveDesignData( ProductStorage storageMeta , MetaDesignDiagram diagram ) throws Exception {
		ActionBase action = loader.getAction();
		Document doc = Common.xmlCreateDoc( XML_ROOT_DESIGN );
		diagram.save( action , doc , doc.getDocumentElement() );
		String diagramFile = diagram.NAME + ".xml";
		storageMeta.saveEnvConfFile( action , doc , diagramFile );
	}
	
	public void exportxmlAll( ProductStorage ms ) throws Exception {
		DBConnection c = loader.getConnection();
		
		trace( "export product data, name=" + set.NAME + ", version=" + c.getCurrentProductVersion( set ) + " ..." );
		exportxmlSettings( ms );
		exportxmlUnits( ms );
		exportxmlDatabase( ms );
		exportxmlSources( ms );
		exportxmlDocs( ms );
		exportxmlDistr( ms );
		
		saveDesignDocs( ms );
	}
	
	public void createdbAll( ProductContext context ) throws Exception {
		trace( "create product data, name=" + set.NAME + " ..." );
		createdbMeta();
		createdbSettings( context );
		createdbUnits();
		createdbDatabase();
		createdbSources();
		createdbDocs();
		createdbDistr();
	}

	public void loaddbAll( ProductContext context ) throws Exception {
		DBConnection c = loader.getConnection();
		
		trace( "load engine product data, name=" + set.NAME + ", version=" + c.getCurrentProductVersion( set ) + " ..." );
		loaddbSettings( context );
		loaddbUnits();
		loaddbDatabase();
		loaddbSources();
		loaddbDocs();
		loaddbDistr();
	}

	public void importxmlAll( ProductStorage ms , ProductContext context ) throws Exception {
		importxmlSettings( ms , context );
		importxmlUnits( ms );
		importxmlDatabase( ms );
		importxmlSources( ms );
		importxmlDocs( ms );
		importxmlDistr( ms );
	}
	
	public ProductMeta copydbAll( EngineProducts products , ProductContext context ) throws Exception {
		trace( "create product data, name=" + set.NAME + " ..." );
		EngineProduct ep = context.product.findEngineProduct();
		ProductMeta dst = new ProductMeta( ep );
		TransactionBase transaction = loader.getTransaction();
		
		copydbMeta( transaction , dst );
		copydbSettings( transaction , dst , context );
		copydbUnits( transaction , dst );
		copydbDatabase( transaction , dst );
		copydbSources( transaction , dst );
		copydbDocs( transaction , dst );
		copydbDistr( transaction , dst );
		return( dst );
	}

	private void createdbMeta() throws Exception {
		trace( "create product meta data ..." );
		DBMeta.createdb( loader , set );
	}

	private void createdbSettings( ProductContext context ) throws Exception {
		trace( "create product settings data ..." );
		DBMetaSettings.createdb( loader , context.product , set , context );
	}
	
	private void createdbUnits() throws Exception {
		trace( "create product units data ..." );
		DBMetaUnits.createdb( loader , set );
	}
	
	private void createdbDatabase() throws Exception {
		trace( "create product database data ..." );
		DBMetaDatabase.createdb( loader , set );
	}
	
	private void createdbSources() throws Exception {
		trace( "create product source data ..." );
		DBMetaSources.createdb( loader , set );
	}
	
	private void createdbDocs() throws Exception {
		trace( "create product documentation data ..." );
		DBMetaDocs.createdb( loader , set );
	}
	
	private void createdbDistr() throws Exception {
		trace( "create product distributive data ..." );
		DBMetaDistr.createdb( loader , set );
	}
	
	private void loaddbSettings( ProductContext context ) throws Exception {
		trace( "load product settings data ..." );
		DBMetaSettings.loaddb( loader , set , context );
	}
	
	private void loaddbUnits() throws Exception {
		trace( "load product units data ..." );
		DBMetaUnits.loaddb( loader , set );
	}
	
	private void loaddbDatabase() throws Exception {
		trace( "load product database data ..." );
		DBMetaDatabase.loaddb( loader , set );
	}
	
	private void loaddbSources() throws Exception {
		trace( "load product source data ..." );
		DBMetaSources.loaddb( loader , set );
	}
	
	private void loaddbDocs() throws Exception {
		trace( "load product documentation data ..." );
		DBMetaDocs.loaddb( loader , set );
	}
	
	private void loaddbDistr() throws Exception {
		trace( "load product distributive data ..." );
		DBMetaDistr.loaddb( loader , set );
	}
	
	private void importxmlSettings( ProductStorage ms , ProductContext context ) throws Exception {
		ActionBase action = loader.getAction();
		try {
			// read
			String file = ms.getCoreConfFile( action );
			action.debug( "read product settings file " + file + "..." );
			Document doc = ConfReader.readXmlFile( action.session.execrc , file );
			Node root = doc.getDocumentElement();

			DBMeta.importxml( loader , set , root );
			DBMetaSettings.importxml( loader , set , context , root );
		}
		catch( Throwable e ) {
			loader.setLoadFailed( action , _Error.UnableLoadProductSettings1 , e , "unable to import settings metadata, product=" + set.NAME , set.NAME );
		}
	}
	
	private void exportxmlSettings( ProductStorage ms ) throws Exception {
		ActionBase action = loader.getAction();
		String file = ms.getCoreConfFile( action );
		action.debug( "export product settings file " + file + "..." );
		Document doc = Common.xmlCreateDoc( XML_ROOT_SETTINGS );
		Element root = doc.getDocumentElement();

		DBMetaSettings.exportxml( loader , set , doc , root );
		ProductStorage.saveDoc( doc , file );
	}
	
	private void importxmlUnits( ProductStorage ms ) throws Exception {
		ActionBase action = loader.getAction();
		try {
			// read
			String file = ms.getUnitsFile( action );
			action.debug( "read units definition file " + file + "..." );
			Document doc = action.readXmlFile( file );
			Node root = doc.getDocumentElement();
			
			DBMetaUnits.importxml( loader , set , root );
		}
		catch( Throwable e ) {
			loader.setLoadFailed( action , _Error.UnableLoadProductUnits1 , e , "unable to import units metadata, product=" + set.NAME , set.NAME );
		}
	}
	
	private void exportxmlUnits( ProductStorage ms ) throws Exception {
		ActionBase action = loader.getAction();
		String file = ms.getUnitsFile( action );
		action.debug( "export units definition file " + file + "..." );
		Document doc = Common.xmlCreateDoc( XML_ROOT_UNITS );
		Element root = doc.getDocumentElement();
		
		DBMetaUnits.exportxml( loader , set , doc , root );
		ProductStorage.saveDoc( doc , file );
	}
	
	private void importxmlDatabase( ProductStorage ms ) throws Exception {
		ActionBase action = loader.getAction();
		try {
			// read
			String file = ms.getDatabaseConfFile( action );
			action.debug( "read database definition file " + file + "..." );
			Document doc = action.readXmlFile( file );
			Node root = doc.getDocumentElement();
			
			DBMetaDatabase.importxml( loader , set , root );
		}
		catch( Throwable e ) {
			loader.setLoadFailed( action , _Error.UnableLoadProductDatabase1 , e , "unable to import database metadata, product=" + set.NAME , set.NAME );
		}
	}
	
	private void exportxmlDatabase( ProductStorage ms ) throws Exception {
		ActionBase action = loader.getAction();
		String file = ms.getDatabaseConfFile( action );
		action.debug( "export database definition file " + file + "..." );
		Document doc = Common.xmlCreateDoc( XML_ROOT_DATABASE );
		Element root = doc.getDocumentElement();
		
		DBMetaDatabase.exportxml( loader , set , doc , root );
		ProductStorage.saveDoc( doc , file );
	}
	
	private void importxmlSources( ProductStorage ms ) throws Exception {
		ActionBase action = loader.getAction();
		try {
			// read
			String file = ms.getSourcesConfFile( action );
			action.debug( "read source definition file " + file + "..." );
			Document doc = action.readXmlFile( file );
			Node root = doc.getDocumentElement();
			
			DBMetaSources.importxml( loader , set , root );
		}
		catch( Throwable e ) {
			loader.setLoadFailed( action , _Error.UnableLoadProductSources1 , e , "unable to import source metadata, product=" + set.NAME , set.NAME );
		}
	}
	
	private void exportxmlSources( ProductStorage ms ) throws Exception {
		ActionBase action = loader.getAction();
		String file = ms.getSourcesConfFile( action );
		action.debug( "export source definition file " + file + "..." );
		Document doc = Common.xmlCreateDoc( XML_ROOT_SOURCES );
		Element root = doc.getDocumentElement();
		
		DBMetaSources.exportxml( loader , set , doc , root );
		ProductStorage.saveDoc( doc , file );
	}
	
	private void importxmlDocs( ProductStorage ms ) throws Exception {
		ActionBase action = loader.getAction();
		try {
			// read
			String file = ms.getDocumentationFile( action );
			action.debug( "read units definition file " + file + "..." );
			Document doc = action.readXmlFile( file );
			Node root = doc.getDocumentElement();
			
			DBMetaDocs.importxml( loader , set , root );
		}
		catch( Throwable e ) {
			loader.setLoadFailed( action , _Error.UnableLoadProductDocs1 , e , "unable to import documentation metadata, product=" + set.NAME , set.NAME );
		}
	}
	
	private void exportxmlDocs( ProductStorage ms ) throws Exception {
		ActionBase action = loader.getAction();
		String file = ms.getDocumentationFile( action );
		action.debug( "export units definition file " + file + "..." );
		Document doc = Common.xmlCreateDoc( XML_ROOT_DOCS );
		Element root = doc.getDocumentElement();
		
		DBMetaDocs.exportxml( loader , set , doc , root );
		ProductStorage.saveDoc( doc , file );
	}
	
	private void importxmlDistr( ProductStorage ms ) throws Exception {
		ActionBase action = loader.getAction();
		try {
			// read
			String file = ms.getDistrConfFile( action );
			action.debug( "read distributive definition file " + file + "..." );
			Document doc = action.readXmlFile( file );
			Node root = doc.getDocumentElement();
			
			DBMetaDistr.importxml( loader , set , root );
		}
		catch( Throwable e ) {
			loader.setLoadFailed( action , _Error.UnableLoadProductDistr1 , e , "unable to import distributive metadata, product=" + set.NAME , set.NAME );
		}
	}

	private void exportxmlDistr( ProductStorage ms ) throws Exception {
		ActionBase action = loader.getAction();
		String file = ms.getDistrConfFile( action );
		action.debug( "export distributive definition file " + file + "..." );
		Document doc = Common.xmlCreateDoc( XML_ROOT_DISTR );
		Element root = doc.getDocumentElement();
		
		DBMetaDistr.exportxml( loader , set , doc , root );
		ProductStorage.saveDoc( doc , file );
	}
	
	public void trace( String s ) {
		loader.trace( s );
	}

	private void copydbMeta( TransactionBase transaction , ProductMeta dst ) throws Exception {
		trace( "copy product meta data ..." );
		DBMeta.copydb( transaction , set , dst );
	}

	private void copydbSettings( TransactionBase transaction , ProductMeta dst , ProductContext context ) throws Exception {
		trace( "create product settings data ..." );
		DBMetaSettings.copydb( transaction , set , context , dst );
	}
	
	private void copydbUnits( TransactionBase transaction , ProductMeta dst ) throws Exception {
		trace( "copy product units data ..." );
		DBMetaUnits.copydb( transaction , set , dst );
	}

	private void copydbDatabase( TransactionBase transaction , ProductMeta dst ) throws Exception {
		trace( "copy product database data ..." );
		DBMetaDatabase.copydb( transaction , set , dst );
	}

	private void copydbSources( TransactionBase transaction , ProductMeta dst ) throws Exception {
		trace( "copy product sources ..." );
		DBMetaSources.copydb( transaction , set , dst );
	}

	private void copydbDocs( TransactionBase transaction , ProductMeta dst ) throws Exception {
		trace( "copy product docs ..." );
		DBMetaDocs.copydb( transaction , set , dst );
	}

	private void copydbDistr( TransactionBase transaction , ProductMeta dst ) throws Exception {
		trace( "copy product distributive ..." );
		DBMetaDistr.copydb( transaction , set , dst );
	}

}
