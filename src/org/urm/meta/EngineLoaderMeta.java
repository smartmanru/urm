package org.urm.meta;

import org.urm.action.ActionBase;
import org.urm.common.Common;
import org.urm.common.ConfReader;
import org.urm.db.DBConnection;
import org.urm.db.product.DBMeta;
import org.urm.db.product.DBMetaDatabase;
import org.urm.db.product.DBMetaDocs;
import org.urm.db.product.DBMetaSettings;
import org.urm.db.product.DBMetaPolicy;
import org.urm.db.product.DBMetaSources;
import org.urm.db.product.DBMetaUnits;
import org.urm.engine.storage.MetadataStorage;
import org.urm.meta.product.Meta;
import org.urm.meta.product.MetaDatabase;
import org.urm.meta.product.MetaDistr;
import org.urm.meta.product.MetaDocs;
import org.urm.meta.product.MetaMonitoring;
import org.urm.meta.product.MetaProductSettings;
import org.urm.meta.product.MetaProductVersion;
import org.w3c.dom.Document;
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
		saveDistr( ms );
		saveMonitoring( ms );
	}
	
	public void loaddbAll( ProductContext context ) throws Exception {
		DBConnection c = loader.getConnection();
		
		trace( "load engine product data, name=" + context.product.NAME + ", version=" + c.getCurrentProductVersion( context.product.ID ) + " ..." );
		loaddbMeta();
		loaddbSettings( context );
		loaddbPolicy();
		loaddbUnits();
		loaddbDatabase();
		loaddbSources();
		loaddbDocs();
		loaddbDistr();
		loaddbMonitoring();
	}

	public void importxmlAll( MetadataStorage storageMeta , ProductContext context ) throws Exception {
		importxmlMeta( storageMeta );
		importxmlSettings( storageMeta , context );
		importxmlPolicy( storageMeta );
		importxmlUnits( storageMeta );
		importxmlDatabase( storageMeta );
		importxmlSources( storageMeta );
		importxmlDocs( storageMeta );
		importxmlDistr( storageMeta );
		importxmlMonitoring( storageMeta );
	}
	
	private void saveDistr( MetadataStorage storageMeta ) throws Exception {
		ActionBase action = loader.getAction();
		Document doc = Common.xmlCreateDoc( XML_ROOT_DISTR );
		MetaDistr distr = set.getDistr();
		distr.save( action , doc , doc.getDocumentElement() );
		storageMeta.saveDistrConfFile( action , doc );
	}
	
	private void saveMonitoring( MetadataStorage storageMeta ) throws Exception {
		ActionBase action = loader.getAction();
		Document doc = Common.xmlCreateDoc( XML_ROOT_MONITORING );
		MetaMonitoring mon = set.getMonitoring();
		mon.save( action , doc , doc.getDocumentElement() );
		storageMeta.saveMonitoringConfFile( action , doc );
	}
	
	private void loaddbMeta() throws Exception {
		trace( "load product meta data ..." );
		DBMeta.loaddb( loader , set );
	}

	private void loaddbSettings( ProductContext context ) throws Exception {
		trace( "load product settings data ..." );
		DBMetaSettings.loaddb( loader , set , context );
	}
	
	private void loaddbPolicy() throws Exception {
		trace( "load product policy data ..." );
		DBMetaPolicy.loaddb( loader , set );
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
	}
	
	private void loaddbMonitoring() throws Exception {
	}
	
	private void importxmlMeta( MetadataStorage storageMeta ) throws Exception {
		ActionBase action = loader.getAction();
		try {
			// read
			String file = storageMeta.getVersionConfFile( action );
			action.debug( "read product version file " + file + "..." );
			Document doc = ConfReader.readXmlFile( action.session.execrc , file );
			Node root = doc.getDocumentElement();

			DBMeta.importxml( loader , set , root );
		}
		catch( Throwable e ) {
			setLoadFailed( action , _Error.UnableLoadProductVersion1 , e , "unable to import version metadata, product=" + set.name , set.name );
		}
	}

	private void importxmlSettings( MetadataStorage storageMeta , ProductContext context ) throws Exception {
		ActionBase action = loader.getAction();
		try {
			// read
			String file = storageMeta.getCoreConfFile( action );
			action.debug( "read product definition file " + file + "..." );
			Document doc = ConfReader.readXmlFile( action.session.execrc , file );
			Node root = doc.getDocumentElement();

			DBMetaSettings.importxml( loader , set , context , root );
		}
		catch( Throwable e ) {
			setLoadFailed( action , _Error.UnableLoadProductSettings1 , e , "unable to import settings metadata, product=" + set.name , set.name );
		}
	}
	
	private void importxmlPolicy( MetadataStorage storageMeta ) throws Exception {
		ActionBase action = loader.getAction();
		try {
			// read
			String file = storageMeta.getPolicyConfFile( action );
			action.debug( "read product policy file " + file + "..." );
			Document doc = ConfReader.readXmlFile( action.session.execrc , file );
			Node root = doc.getDocumentElement();
			
			DBMetaPolicy.importxml( loader , set , root );
		}
		catch( Throwable e ) {
			setLoadFailed( action , _Error.UnableLoadProductVersion1 , e , "unable to import version metadata, product=" + set.name , set.name );
		}
	}

	private void importxmlUnits( MetadataStorage storageMeta ) throws Exception {
		ActionBase action = loader.getAction();
		try {
			// read
			String file = storageMeta.getCoreConfFile( action );
			action.debug( "read units definition file " + file + "..." );
			Document doc = action.readXmlFile( file );
			Node root = doc.getDocumentElement();
			Node node = ConfReader.xmlGetFirstChild( root , "units" );
			
			DBMetaUnits.importxml( loader , set , node );
		}
		catch( Throwable e ) {
			setLoadFailed( action , _Error.UnableLoadProductUnits1 , e , "unable to import units metadata, product=" + set.name , set.name );
		}
	}
	
	private void importxmlDatabase( MetadataStorage storageMeta ) throws Exception {
		ActionBase action = loader.getAction();
		try {
			// read
			String file = storageMeta.getDatabaseConfFile( action );
			action.debug( "read database definition file " + file + "..." );
			Document doc = action.readXmlFile( file );
			Node root = doc.getDocumentElement();
			
			DBMetaDatabase.importxml( loader , set , root );
		}
		catch( Throwable e ) {
			setLoadFailed( action , _Error.UnableLoadProductDatabase1 , e , "unable to import database metadata, product=" + set.name , set.name );
		}
	}
	
	private void importxmlSources( MetadataStorage storageMeta ) throws Exception {
		ActionBase action = loader.getAction();
		try {
			// read
			String file = storageMeta.getSourcesConfFile( action );
			action.debug( "read source definition file " + file + "..." );
			Document doc = action.readXmlFile( file );
			Node root = doc.getDocumentElement();
			
			DBMetaSources.importxml( loader , set , root );
		}
		catch( Throwable e ) {
			setLoadFailed( action , _Error.UnableLoadProductSources1 , e , "unable to import source metadata, product=" + set.name , set.name );
		}
	}
	
	private void importxmlDocs( MetadataStorage storageMeta ) throws Exception {
		ActionBase action = loader.getAction();
		try {
			// read
			String file = storageMeta.getDocumentationFile( action );
			action.debug( "read units definition file " + file + "..." );
			Document doc = action.readXmlFile( file );
			Node root = doc.getDocumentElement();
			
			DBMetaDocs.importxml( loader , set , root );
		}
		catch( Throwable e ) {
			setLoadFailed( action , _Error.UnableLoadProductDocs1 , e , "unable to import documentation metadata, product=" + set.name , set.name );
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
			setLoadFailed( action , _Error.UnableLoadProductDistr1 , e , "unable to import distributive metadata, product=" + set.name , set.name );
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
			setLoadFailed( action , _Error.UnableLoadProductMonitoring1 , e , "unable to import monitoring metadata, product=" + set.name , set.name );
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
