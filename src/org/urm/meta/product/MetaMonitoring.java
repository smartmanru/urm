package org.urm.meta.product;

import java.util.HashMap;
import java.util.Map;

import org.urm.action.ActionBase;
import org.urm.common.ConfReader;
import org.urm.common.PropertyController;
import org.urm.engine.ServerTransaction;
import org.urm.engine.TransactionBase;
import org.urm.meta.ServerProductMeta;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class MetaMonitoring extends PropertyController {
	
	public Meta meta;

	Map<String,MetaMonitoringTarget> mapTargets;

	public boolean ENABLED;
	public String DIR_DATA;
	public String DIR_REPORTS;
	public String DIR_RES;
	public String RESOURCE_URL;
	
	public int MAJORINTERVAL;
	public int MINORINTERVAL;
	public int MINSILENT;

	// properties
	public static String PROPERTY_ENABLED = "monitoring.enabled";
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
		mapTargets = new HashMap<String,MetaMonitoringTarget>();
	}
	
	@Override
	public boolean isValid() {
		if( super.isLoadFailed() )
			return( false );
		return( true );
	}
	
	@Override
	public void scatterProperties( ActionBase action ) throws Exception {
		ENABLED = super.getBooleanProperty( action , PROPERTY_ENABLED );
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
		MetaProductSettings product = meta.getProductSettings( action );
		r.initCopyStarted( this , product.getProperties() );
		r.initFinished();
		return( r );
	}
	
	public void createMonitoring( TransactionBase transaction ) throws Exception {
		MetaProductSettings product = meta.getProductSettings( transaction.action );
		if( !super.initCreateStarted( product.getProperties() ) )
			return;
		super.initFinished();
	}
	
	public void load( ActionBase action , Node root ) throws Exception {
		MetaProductSettings product = meta.getProductSettings( action );
		if( !super.initCreateStarted( product.getProperties() ) )
			return;

		properties.loadFromNodeElements( root );
		
		scatterProperties( action );
		super.finishProperties( action );
		
		loadEnvironments( action , ConfReader.xmlGetPathNode( root , "scope" ) );
		
		super.initFinished();
	}

	public Map<String,MetaMonitoringTarget> getTargets( ActionBase action ) throws Exception { 
		return( mapTargets );
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
			mapTargets.put( item.NAME , item );
		}
	}
	
	public void save( ActionBase action , Document doc , Element root ) throws Exception {
		properties.saveAsElements( doc , root );
	}

	public MetaMonitoringTarget findMonitoringTarget( MetaEnvDC dc ) {
		for( MetaMonitoringTarget target : mapTargets.values() ) {
			if( target.ENV.equals( dc.env.ID ) && target.DC.equals( dc.NAME ) )
				return( target );
		}
		return( null );
	}

	public void setMonitoringEnabled( ServerTransaction transaction , boolean enabled ) throws Exception {
		super.setBooleanProperty( PROPERTY_ENABLED , enabled );
		ENABLED = true;
	}
	
}
