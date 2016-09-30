package org.urm.engine.meta;

import java.util.HashMap;
import java.util.Map;

import org.urm.action.ActionBase;
import org.urm.common.ConfReader;
import org.urm.common.PropertyController;
import org.urm.engine.ServerProductMeta;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class MetaMonitoring extends PropertyController {
	
	public Meta meta;

	Map<String,MetaMonitoringTarget> mapEnvs;

	public String DIR_DATA;
	public String DIR_REPORTS;
	public String DIR_RES;
	public String RESOURCE_URL;
	
	public int MAJORINTERVAL;
	public int MINORINTERVAL;
	public int MINSILENT;

	// properties
	public static String PROPERTY_DIR_DATA = "data.path";
	public static String PROPERTY_DIR_REPORTS = "reports.path";
	public static String PROPERTY_DIR_RES = "resources.path";
	public static String PROPERTY_RESOURCE_URL = "resources.url";
	
	public static String PROPERTY_MAJORINTERVAL;
	public static String PROPERTY_MINORINTERVAL;
	public static String PROPERTY_MINSILENT;
	
	public MetaMonitoring( ServerProductMeta storage , Meta meta ) {
		super( storage , "monitoring" );
		
		this.meta = meta;
		mapEnvs = new HashMap<String,MetaMonitoringTarget>();
	}
	
	@Override
	public boolean isValid() {
		if( super.isLoadFailed() )
			return( false );
		return( true );
	}
	
	@Override
	public void scatterProperties( ActionBase action ) throws Exception {
		DIR_DATA = super.getPathProperty( action , PROPERTY_DIR_DATA );
		DIR_REPORTS = super.getPathProperty( action , PROPERTY_DIR_REPORTS );
		DIR_RES = super.getPathProperty( action , PROPERTY_DIR_RES );
		RESOURCE_URL = super.getStringProperty( action , PROPERTY_RESOURCE_URL );
		
		MAJORINTERVAL = super.getIntProperty( action , PROPERTY_MAJORINTERVAL , 300 );
		MINORINTERVAL = super.getIntProperty( action , PROPERTY_MINORINTERVAL , 60 );
		MINSILENT = super.getIntProperty( action , PROPERTY_MINSILENT , 30 );
	}
	
	public MetaMonitoring copy( ActionBase action , Meta meta ) throws Exception {
		MetaMonitoring r = new MetaMonitoring( meta.getStorage( action ) , meta );
		MetaProductSettings product = meta.getProduct( action );
		super.initCopyStarted( this , product.getProperties() );
		return( r );
	}
	
	public void create( ActionBase action ) throws Exception {
		MetaProductSettings product = meta.getProduct( action );
		if( !super.initCreateStarted( product.getProperties() ) )
			return;
		super.initFinished();
	}
	
	public void load( ActionBase action , Node root ) throws Exception {
		MetaProductSettings product = meta.getProduct( action );
		if( !super.initCreateStarted( product.getProperties() ) )
			return;

		properties.loadFromNodeElements( root );
		
		scatterProperties( action );
		super.finishProperties( action );
		
		loadEnvironments( action , ConfReader.xmlGetPathNode( root , "scope" ) );
		
		super.initFinished();
	}

	public Map<String,MetaMonitoringTarget> getTargets( ActionBase action ) throws Exception { 
		return( mapEnvs );
	}
	
	private void loadEnvironments( ActionBase action , Node node ) throws Exception {
		if( node == null ) {
			action.info( "no environments defined for monitoring" );
			return;
		}

		Node[] items = ConfReader.xmlGetChildren( node , "environment" );
		if( items == null )
			return;
		
		for( Node deliveryNode : items ) {
			MetaMonitoringTarget item = new MetaMonitoringTarget( meta , this );
			item.loadEnv( action , deliveryNode );
			mapEnvs.put( item.NAME , item );
		}
	}
	
	public void save( ActionBase action , Document doc , Element root ) throws Exception {
		if( !super.isLoaded() )
			return;

		properties.saveAsElements( doc , root );
	}
	
}
