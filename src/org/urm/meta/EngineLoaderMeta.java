package org.urm.meta;

import org.urm.action.ActionBase;
import org.urm.common.Common;
import org.urm.common.ConfReader;
import org.urm.db.DBConnection;
import org.urm.db.product.DBMeta;
import org.urm.db.product.DBMetaDatabase;
import org.urm.db.product.DBMetaDistr;
import org.urm.db.product.DBMetaDocs;
import org.urm.db.product.DBMetaSettings;
import org.urm.db.product.DBMetaPolicy;
import org.urm.db.product.DBMetaSources;
import org.urm.db.product.DBMetaUnits;
import org.urm.engine.storage.MetadataStorage;
import org.urm.meta.product.Meta;
import org.urm.meta.product.MetaMonitoring;
import org.urm.meta.product.MetaProductSettings;
import org.urm.meta.product.MetaProductVersion;
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
		saveMonitoring( ms );
	}
	
	public void exportAll( MetadataStorage ms ) throws Exception {
		DBConnection c = loader.getConnection();
		
		trace( "export engine product data, name=" + set.name + ", version=" + c.getCurrentProductVersion( set ) + " ..." );
		exportxmlMeta( ms );
		exportxmlSettings( ms );
		exportxmlPolicy( ms );
		exportxmlUnits( ms );
		exportxmlDatabase( ms );
		exportxmlSources( ms );
		exportxmlDocs( ms );
		exportxmlDistr( ms );
		exportxmlMonitoring( ms );
	}
	
	public void loaddbAll( ProductContext context ) throws Exception {
		DBConnection c = loader.getConnection();
		
		trace( "load engine product data, name=" + set.name + ", version=" + c.getCurrentProductVersion( set ) + " ..." );
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

	public void importxmlAll( MetadataStorage ms , ProductContext context ) throws Exception {
		importxmlMeta( ms );
		importxmlSettings( ms , context );
		importxmlPolicy( ms );
		importxmlUnits( ms );
		importxmlDatabase( ms );
		importxmlSources( ms );
		importxmlDocs( ms );
		importxmlDistr( ms );
		importxmlMonitoring( ms );
	}
	
	private void saveMonitoring( MetadataStorage ms ) throws Exception {
		ActionBase action = loader.getAction();
		Document doc = Common.xmlCreateDoc( XML_ROOT_MONITORING );
		MetaMonitoring mon = set.getMonitoring();
		mon.save( action , doc , doc.getDocumentElement() );
		ms.saveMonitoringConfFile( action , doc );
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
	
	private void importxmlMeta( MetadataStorage ms ) throws Exception {
		ActionBase action = loader.getAction();
		try {
			// read
			String file = ms.getVersionConfFile( action );
			action.debug( "read product version file " + file + "..." );
			Document doc = ConfReader.readXmlFile( action.session.execrc , file );
			Node root = doc.getDocumentElement();

			DBMeta.importxml( loader , set , root );
		}
		catch( Throwable e ) {
			setLoadFailed( action , _Error.UnableLoadProductVersion1 , e , "unable to import version metadata, product=" + set.name , set.name );
		}
	}

	private void exportxmlMeta( MetadataStorage ms ) throws Exception {
		ActionBase action = loader.getAction();
		String file = ms.getVersionConfFile( action );
		action.debug( "export product version file " + file + "..." );
		Document doc = Common.xmlCreateDoc( "version" );
		Element root = doc.getDocumentElement();

		DBMeta.exportxml( loader , set , doc , root );
		Common.xmlSaveDoc( doc , file );
	}

	private void importxmlSettings( MetadataStorage ms , ProductContext context ) throws Exception {
		ActionBase action = loader.getAction();
		try {
			// read
			String file = ms.getCoreConfFile( action );
			action.debug( "read product settings file " + file + "..." );
			Document doc = ConfReader.readXmlFile( action.session.execrc , file );
			Node root = doc.getDocumentElement();

			DBMetaSettings.importxml( loader , set , context , root );
		}
		catch( Throwable e ) {
			setLoadFailed( action , _Error.UnableLoadProductSettings1 , e , "unable to import settings metadata, product=" + set.name , set.name );
		}
	}
	
	private void exportxmlSettings( MetadataStorage ms ) throws Exception {
		ActionBase action = loader.getAction();
		String file = ms.getCoreConfFile( action );
		action.debug( "export product settings file " + file + "..." );
		Document doc = Common.xmlCreateDoc( "settings" );
		Element root = doc.getDocumentElement();

		DBMetaSettings.exportxml( loader , set , doc , root );
		Common.xmlSaveDoc( doc , file );
	}
	
	private void importxmlPolicy( MetadataStorage ms ) throws Exception {
		ActionBase action = loader.getAction();
		try {
			// read
			String file = ms.getPolicyConfFile( action );
			action.debug( "read product policy file " + file + "..." );
			Document doc = ConfReader.readXmlFile( action.session.execrc , file );
			Node root = doc.getDocumentElement();
			
			DBMetaPolicy.importxml( loader , set , root );
		}
		catch( Throwable e ) {
			setLoadFailed( action , _Error.UnableLoadProductVersion1 , e , "unable to import version metadata, product=" + set.name , set.name );
		}
	}

	private void exportxmlPolicy( MetadataStorage ms ) throws Exception {
		ActionBase action = loader.getAction();
		String file = ms.getPolicyConfFile( action );
		action.debug( "export product policy file " + file + "..." );
		Document doc = Common.xmlCreateDoc( "policy" );
		Element root = doc.getDocumentElement();
		
		DBMetaPolicy.exportxml( loader , set , doc , root );
		Common.xmlSaveDoc( doc , file );
	}

	private void importxmlUnits( MetadataStorage ms ) throws Exception {
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
			setLoadFailed( action , _Error.UnableLoadProductUnits1 , e , "unable to import units metadata, product=" + set.name , set.name );
		}
	}
	
	private void exportxmlUnits( MetadataStorage ms ) throws Exception {
		ActionBase action = loader.getAction();
		String file = ms.getUnitsFile( action );
		action.debug( "export units definition file " + file + "..." );
		Document doc = Common.xmlCreateDoc( "units" );
		Element root = doc.getDocumentElement();
		
		DBMetaUnits.exportxml( loader , set , doc , root );
		Common.xmlSaveDoc( doc , file );
	}
	
	private void importxmlDatabase( MetadataStorage ms ) throws Exception {
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
			setLoadFailed( action , _Error.UnableLoadProductDatabase1 , e , "unable to import database metadata, product=" + set.name , set.name );
		}
	}
	
	private void exportxmlDatabase( MetadataStorage ms ) throws Exception {
		ActionBase action = loader.getAction();
		String file = ms.getDatabaseConfFile( action );
		action.debug( "export database definition file " + file + "..." );
		Document doc = Common.xmlCreateDoc( "database" );
		Element root = doc.getDocumentElement();
		
		DBMetaDatabase.exportxml( loader , set , doc , root );
		Common.xmlSaveDoc( doc , file );
	}
	
	private void importxmlSources( MetadataStorage ms ) throws Exception {
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
			setLoadFailed( action , _Error.UnableLoadProductSources1 , e , "unable to import source metadata, product=" + set.name , set.name );
		}
	}
	
	private void exportxmlSources( MetadataStorage ms ) throws Exception {
		ActionBase action = loader.getAction();
		String file = ms.getSourcesConfFile( action );
		action.debug( "export source definition file " + file + "..." );
		Document doc = Common.xmlCreateDoc( "sources" );
		Element root = doc.getDocumentElement();
		
		DBMetaSources.exportxml( loader , set , doc , root );
		Common.xmlSaveDoc( doc , file );
	}
	
	private void importxmlDocs( MetadataStorage ms ) throws Exception {
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
			setLoadFailed( action , _Error.UnableLoadProductDocs1 , e , "unable to import documentation metadata, product=" + set.name , set.name );
		}
	}
	
	private void exportxmlDocs( MetadataStorage ms ) throws Exception {
		ActionBase action = loader.getAction();
		String file = ms.getDocumentationFile( action );
		action.debug( "export units definition file " + file + "..." );
		Document doc = Common.xmlCreateDoc( "docs" );
		Element root = doc.getDocumentElement();
		
		DBMetaDocs.exportxml( loader , set , doc , root );
		Common.xmlSaveDoc( doc , file );
	}
	
	private void importxmlDistr( MetadataStorage ms ) throws Exception {
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
			setLoadFailed( action , _Error.UnableLoadProductDistr1 , e , "unable to import distributive metadata, product=" + set.name , set.name );
		}
	}

	private void exportxmlDistr( MetadataStorage ms ) throws Exception {
		ActionBase action = loader.getAction();
		String file = ms.getDistrConfFile( action );
		action.debug( "export distributive definition file " + file + "..." );
		Document doc = Common.xmlCreateDoc( "distr" );
		Element root = doc.getDocumentElement();
		
		DBMetaDistr.exportxml( loader , set , doc , root );
		Common.xmlSaveDoc( doc , file );
	}
	
	private void importxmlMonitoring( MetadataStorage ms ) throws Exception {
		MetaProductSettings settings = set.getSettings();
		MetaMonitoring mon = new MetaMonitoring( set , settings , set.meta );
		set.setMonitoring( mon );
		
		ActionBase action = loader.getAction();
		try {
			// read
			String file = ms.getMonitoringConfFile( action );
			action.debug( "read monitoring definition file " + file + "..." );
			Document doc = action.readXmlFile( file );
			Node root = doc.getDocumentElement();
			mon.load( action , root );
		}
		catch( Throwable e ) {
			setLoadFailed( action , _Error.UnableLoadProductMonitoring1 , e , "unable to import monitoring metadata, product=" + set.name , set.name );
		}
	}

	private void exportxmlMonitoring( MetadataStorage ms ) throws Exception {
	}
	
	public void trace( String s ) {
		loader.trace( s );
	}

	private void setLoadFailed( ActionBase action , int error , Throwable e , String msg , String product ) throws Exception {
		loader.log( msg ,  e );
		Common.exit1( error , msg , product );
	}
	
}
