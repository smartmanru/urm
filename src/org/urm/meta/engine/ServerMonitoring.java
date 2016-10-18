package org.urm.meta.engine;

import java.util.HashMap;
import java.util.Map;

import org.urm.action.ActionBase;
import org.urm.action.ActionCore;
import org.urm.common.Common;
import org.urm.common.ConfReader;
import org.urm.common.RunContext;
import org.urm.engine.ServerEngine;
import org.urm.meta.ServerLoader;
import org.urm.meta.ServerObject;
import org.urm.meta.ServerProductMeta;
import org.urm.meta.product.MetaMonitoring;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class ServerMonitoring extends ServerObject {

	ServerLoader loader;
	ServerEngine engine;

	Map<String,ServerMonitoringProduct> mapProduct;
	
	public boolean ENABLED;
	public String DIR_DATA;
	public String DIR_REPORTS;
	public String DIR_RES;
	public String RESOURCE_URL;
	
	// properties
	public static String PROPERTY_ENABLED = "server.monitoring.enabled";
	public static String PROPERTY_DIR_DATA = "server.data.path";
	public static String PROPERTY_DIR_REPORTS = "server.reports.path";
	public static String PROPERTY_DIR_RES = "server.resources.path";
	public static String PROPERTY_RESOURCE_URL = "server.resources.url";
	
	public ServerMonitoring( ServerLoader loader ) {
		super( null );
		this.loader = loader; 
		this.engine = loader.engine;
		
		mapProduct = new HashMap<String,ServerMonitoringProduct>(); 
	}
	
	public void load( String monFile , RunContext execrc ) throws Exception {
		Document doc = ConfReader.readXmlFile( execrc , monFile );
		Node root = doc.getDocumentElement();

		String rootPath = Common.getPath( engine.execrc.installPath , "monitoring" );
		ENABLED = Common.getBooleanValue( ConfReader.getPropertyValue( root , PROPERTY_ENABLED , "no" ) );
		DIR_DATA = ConfReader.getPropertyValue( root , PROPERTY_DIR_DATA , Common.getPath( rootPath , "data" ) );
		DIR_REPORTS = ConfReader.getPropertyValue( root , PROPERTY_DIR_REPORTS , Common.getPath( rootPath , "reports" ) );
		DIR_RES = ConfReader.getPropertyValue( root , PROPERTY_DIR_RES , Common.getPath( rootPath , "res" ) );
		RESOURCE_URL = ConfReader.getPropertyValue( root , PROPERTY_RESOURCE_URL , "" );
	}
	
	public void save( ActionCore action , String path , RunContext execrc ) throws Exception {
		Document doc = Common.xmlCreateDoc( "infrastructure" );
		Element root = doc.getDocumentElement();
		
		Common.xmlCreatePropertyElement( doc , root , PROPERTY_DIR_DATA , DIR_DATA );
		Common.xmlCreatePropertyElement( doc , root , PROPERTY_DIR_REPORTS , DIR_REPORTS );
		Common.xmlCreatePropertyElement( doc , root , PROPERTY_DIR_RES , DIR_REPORTS );
		Common.xmlCreatePropertyElement( doc , root , PROPERTY_RESOURCE_URL , DIR_RES );
		
		Common.xmlSaveDoc( doc , path );
	}

	public void start() {
		ServerRegistry registry = loader.getRegistry();
		for( String productName : registry.directory.getProducts() )
			startProduct( productName );
	}

	public void stop() {
		for( ServerMonitoringProduct mon : mapProduct.values() )
			mon.stop();
		
		mapProduct.clear();
	}

	public void startProduct( String productName ) {
		ActionBase action = engine.serverAction;
		ServerProductMeta storage = loader.findProductStorage( productName );
		if( storage == null || storage.loadFailed ) {
			action.trace( "ignore monitoring for non-healthy product=" + productName );
			return;
		}
		
		MetaMonitoring meta = storage.getMonitoring();
		if( !meta.ENABLED ) {
			action.trace( "monitoring is turned off for product=" + productName );
			return;
		}

		ServerMonitoringProduct mon = new ServerMonitoringProduct( this , meta );
		mapProduct.put( productName , mon );
		mon.start();
		action.trace( "monitoring started for product=" + productName );
	}

	public void stopProduct( String productName ) {
		ServerMonitoringProduct mon = mapProduct.get( productName );
		if( mon == null )
			return;
		
		mon.stop();
		mapProduct.remove( productName );
	}
	
}
