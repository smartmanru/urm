package org.urm.meta.loader;

import org.urm.common.Common;
import org.urm.common.ConfReader;
import org.urm.common.RunContext;
import org.urm.db.engine.DBEngineDirectory;
import org.urm.db.system.DBAppProduct;
import org.urm.db.system.DBAppProductDumps;
import org.urm.db.system.DBAppProductMonitoring;
import org.urm.engine.DataService;
import org.urm.engine.Engine;
import org.urm.engine.data.EngineDirectory;
import org.urm.meta.system.AppProduct;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class EngineLoaderSystems {

	public static String XML_ROOT_MONITORING = "monitoring";
	public static String XML_ROOT_DUMPS = "dumps";
	public static String ELEMENT_PRODUCT = "product";
	
	private EngineLoader loader;
	private DataService data;
	public RunContext execrc;
	public Engine engine;
	
	private EngineDirectory directoryNew;
	
	public EngineLoaderSystems( EngineLoader loader , DataService data ) {
		this.loader = loader;
		this.data = data;
		this.execrc = loader.execrc;
		this.engine = loader.engine;
	}
	
	public void trace( String s ) {
		loader.trace( s );
	}

	public EngineDirectory getDirectory() {
		if( directoryNew != null )
			return( directoryNew );
		return( data.getDirectory() );
	}

	public void loaddbDirectory() throws Exception {
		trace( "load engine directory data ..." );
		directoryNew = new EngineDirectory( engine , data );
		DBEngineDirectory.loaddb( loader , directoryNew );
		
		for( AppProduct product : directoryNew.getProducts() )
			loaddbProductData( loader , product );
		
		// match systems to engine
		DBEngineDirectory.matchSystems( loader , directoryNew , false );
	}
	
	private static void loaddbProductData( EngineLoader loader , AppProduct product ) throws Exception {
		DBAppProduct.loaddbPolicy( loader , product );
		DBAppProductDumps.loaddbAll( loader , product );
		DBAppProductMonitoring.loaddbAll( loader , product );
	}
	
	private String getDirectoryFile() throws Exception {
		String path = Common.getPath( execrc.installPath , "etc" );
		String propertyFile = Common.getPath( path , "directory.xml" );
		return( propertyFile );
	}

	private String getMonitoringFile() throws Exception {
		String path = Common.getPath( execrc.installPath , "etc" );
		String propertyFile = Common.getPath( path , "monitoring.xml" );
		return( propertyFile );
	}

	private String getDumpsFile() throws Exception {
		String path = Common.getPath( execrc.installPath , "etc" );
		String propertyFile = Common.getPath( path , "dumps.xml" );
		return( propertyFile );
	}

	public void importxmlDirectory() throws Exception {
		trace( "import engine directory data ..." );
		String registryFile = getDirectoryFile();
		Document doc = ConfReader.readXmlFile( execrc , registryFile );
		Node root = doc.getDocumentElement();
		
		directoryNew = new EngineDirectory( engine , data );
		DBEngineDirectory.importxml( loader , directoryNew , root );
		
		importxmlMonitoring();
		importxmlDumps();
	}

	public void exportxmlDirectory() throws Exception {
		trace( "export engine directory data ..." );
		String propertyFile = getDirectoryFile();
		Document doc = Common.xmlCreateDoc( "directory" );
		Element root = doc.getDocumentElement();
		DBEngineDirectory.exportxml( loader , data.getDirectory() , doc , root );
		Common.xmlSaveDoc( doc , propertyFile );
		
		exportxmlMonitoring();
		exportxmlDumps();
	}

	public void setData() {
		if( directoryNew != null ) {
			data.setDirectory( directoryNew );
			directoryNew = null;
		}
	}

	private void importxmlMonitoring() throws Exception {
		// read
		String file = getMonitoringFile();
		trace( "read monitoring definition file " + file + "..." );
		Document doc = ConfReader.readXmlFile( execrc , file );
		Node root = doc.getDocumentElement();
		
		Node[] items = ConfReader.xmlGetChildren( root , ELEMENT_PRODUCT );
		if( items == null )
			return;
		
		EngineDirectory directory = getDirectory();
		for( Node node : items ) {
			String productName = ConfReader.getAttrValue( node , "name" );
			AppProduct product = directory.findProduct( productName );
			if( product == null ) {
				trace( "ignore missing product=" + items );
				continue;
			}
			
			DBAppProductMonitoring.importxmlAll( loader , product , node );
		}
	}
	
	private void importxmlDumps() throws Exception {
		// read
		String file = getDumpsFile();
		trace( "read dump definition file " + file + "..." );
		Document doc = ConfReader.readXmlFile( execrc , file );
		Node root = doc.getDocumentElement();
		
		Node[] items = ConfReader.xmlGetChildren( root , ELEMENT_PRODUCT );
		if( items == null )
			return;
		
		EngineDirectory directory = getDirectory();
		for( Node node : items ) {
			String productName = ConfReader.getAttrValue( node , "name" );
			AppProduct product = directory.findProduct( productName );
			if( product == null ) {
				trace( "ignore missing product=" + items );
				continue;
			}
			
			DBAppProductDumps.importxmlAll( loader , product , node );
		}
	}
	
	public void exportxmlMonitoring() throws Exception {
		String file = getMonitoringFile();
		trace( "export monitoring file " + file + "..." );
		Document doc = Common.xmlCreateDoc( XML_ROOT_MONITORING );
		Element root = doc.getDocumentElement();

		EngineDirectory directory = getDirectory();
		for( String name : directory.getProductNames() ) {
			Element node = Common.xmlCreateElement( doc , root , ELEMENT_PRODUCT );
			Common.xmlSetNameAttr( doc , node , name );
			AppProduct product = directory.getProduct( name );
			DBAppProductMonitoring.exportxmlAll( loader , product , doc , node );
		}
		
		Common.xmlSaveDoc( doc , file );
	}

	public void exportxmlDumps() throws Exception {
		String file = getDumpsFile();
		trace( "export dumps file " + file + "..." );
		Document doc = Common.xmlCreateDoc( XML_ROOT_DUMPS );
		Element root = doc.getDocumentElement();

		EngineDirectory directory = getDirectory();
		for( String name : directory.getProductNames() ) {
			Element node = Common.xmlCreateElement( doc , root , ELEMENT_PRODUCT );
			Common.xmlSetNameAttr( doc , node , name );
			AppProduct product = directory.getProduct( name );
			DBAppProductDumps.exportxmlAll( loader , product , doc , node );
		}
		
		Common.xmlSaveDoc( doc , file );
	}	
	
}
