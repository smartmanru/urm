package org.urm.meta.product;

import java.util.HashMap;
import java.util.Map;

import org.urm.action.ActionBase;
import org.urm.common.Common;
import org.urm.common.ConfReader;
import org.urm.common.PropertyController;
import org.urm.common.PropertySet;
import org.urm.engine.EngineTransaction;
import org.urm.engine.TransactionBase;
import org.urm.engine.schedule.ScheduleProperties;
import org.urm.engine.storage.LocalFolder;
import org.urm.meta.ProductMeta;
import org.urm.meta.engine.EngineMonitoring;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class MetaMonitoring extends PropertyController {
	
	public Meta meta;

	Map<String,MetaMonitoringTarget> mapTargets;

	public boolean ENABLED;
	public String RESOURCE_URL;
	public String DIR_RES;
	public String DIR_DATA;
	public String DIR_REPORTS;
	public String DIR_LOGS;
	
	public int MAJORINTERVAL;
	public int MINORINTERVAL;
	public int MINSILENT;

	// properties
	public static String PROPERTY_ENABLED = "monitoring.enabled";
	public static String PROPERTY_RESOURCE_URL = "resources.url";
	public static String PROPERTY_DIR_RES = "resources.path";
	public static String PROPERTY_DIR_DATA = "data.path";
	public static String PROPERTY_DIR_REPORTS = "reports.path";
	public static String PROPERTY_DIR_LOGS = "logs.path";
	
	public static String PROPERTY_MAJORINTERVAL = "major.interval";
	public static String PROPERTY_MINORINTERVAL = "minor.interval";
	public static String PROPERTY_MINSILENT = "silent.between";
	
	public MetaMonitoring( ProductMeta storage , MetaProductSettings settings , Meta meta ) {
		super( storage , settings , "monitoring" );
		
		this.meta = meta;
		mapTargets = new HashMap<String,MetaMonitoringTarget>();
	}
	
	@Override
	public String getName() {
		return( "meta-monitoring" );
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
		RESOURCE_URL = super.getStringProperty( action , PROPERTY_RESOURCE_URL );
		DIR_RES = super.getPathProperty( action , PROPERTY_DIR_RES );
		DIR_DATA = super.getPathProperty( action , PROPERTY_DIR_DATA );
		DIR_REPORTS = super.getPathProperty( action , PROPERTY_DIR_REPORTS );
		DIR_LOGS = super.getPathProperty( action , PROPERTY_DIR_LOGS );
		
		MAJORINTERVAL = super.getIntProperty( action , PROPERTY_MAJORINTERVAL , 300 );
		MINORINTERVAL = super.getIntProperty( action , PROPERTY_MINORINTERVAL , 60 );
		MINSILENT = super.getIntProperty( action , PROPERTY_MINSILENT , 30 );
	}
	
	public MetaMonitoring copy( ActionBase action , Meta meta ) throws Exception {
		MetaProductSettings product = meta.getProductSettings( action );
		MetaMonitoring r = new MetaMonitoring( meta.getStorage( action ) , product , meta );
		r.initCopyStarted( this , product.getProperties() );
		
		for( MetaMonitoringTarget target : mapTargets.values() ) {
			MetaMonitoringTarget rtarget = target.copy( action , meta , r );
			r.mapTargets.put( target.NAME , rtarget );
		}
		
		r.scatterProperties( action );
		r.initFinished();
		return( r );
	}
	
	public void createMonitoring( TransactionBase transaction ) throws Exception {
		MetaProductSettings product = meta.getProductSettings( transaction.action );
		if( !super.initCreateStarted( product.getProperties() ) )
			return;
		
		ActionBase action = transaction.getAction();
		EngineMonitoring sm = action.getServerMonitoring();
		PropertySet src = sm.properties;
		super.setSystemBooleanProperty( PROPERTY_ENABLED , false );
		super.setSystemUrlProperty( PROPERTY_RESOURCE_URL , src.getExpressionByProperty( EngineMonitoring.PROPERTY_RESOURCE_URL ) );
		super.setSystemPathProperty( PROPERTY_DIR_RES , src.getExpressionByProperty( EngineMonitoring.PROPERTY_RESOURCE_PATH ) );
		super.setSystemPathProperty( PROPERTY_DIR_DATA , src.getExpressionByProperty( EngineMonitoring.PROPERTY_DIR_DATA ) );
		super.setSystemPathProperty( PROPERTY_DIR_REPORTS , src.getExpressionByProperty( EngineMonitoring.PROPERTY_DIR_REPORTS ) );
		super.setSystemPathProperty( PROPERTY_DIR_LOGS , src.getExpressionByProperty( EngineMonitoring.PROPERTY_DIR_LOGS ) );
		scatterProperties( action );
		super.initFinished();
	}
	
	public void load( ActionBase action , Node root ) throws Exception {
		MetaProductSettings product = meta.getProductSettings( action );
		if( !super.initCreateStarted( product.getProperties() ) )
			return;

		super.loadFromNodeElements( action , root , false );
		
		scatterProperties( action );
		super.finishProperties( action );
		
		loadTargets( action , ConfReader.xmlGetPathNode( root , "scope" ) );
		
		super.initFinished();
	}

	public MetaMonitoringTarget[] getTargets() { 
		return( mapTargets.values().toArray( new MetaMonitoringTarget[0] ) );
	}
	
	private void loadTargets( ActionBase action , Node node ) throws Exception {
		mapTargets.clear();
		
		Node[] items = null;
		if( node != null )
			items = ConfReader.xmlGetChildren( node , "target" );
		
		if( items == null ) {
			action.info( "no targets defined for monitoring" );
			return;
		}

		for( Node deliveryNode : items ) {
			MetaMonitoringTarget item = new MetaMonitoringTarget( meta , this );
			item.loadTarget( action , deliveryNode );
			addTarget( item );
		}
	}
	
	private void addTarget( MetaMonitoringTarget target ) {
		mapTargets.put( target.NAME , target );
	}

	public void save( ActionBase action , Document doc , Element root ) throws Exception {
		boolean create = createFolders( action );
		if( !create ) {
			action.error( "monitoring is forced off because folders (res=" + DIR_RES + ", data=" + DIR_DATA + ", reports=" + DIR_REPORTS + ", logs=" + DIR_LOGS + ") are not ready, check settings" );
			super.setBooleanProperty( PROPERTY_ENABLED , false );
			ENABLED = false;
		}
		
		super.saveAsElements( doc , root , false );
		
		Element scope = Common.xmlCreateElement( doc , root , "scope" );
		for( MetaMonitoringTarget target : mapTargets.values() ) {
			Element element = Common.xmlCreateElement( doc , scope , "target" );
			if( create )
				target.createFolders( action );
			target.save( action , doc , element );
		}
	}

	private boolean createFolders( ActionBase action ) throws Exception {
		if( DIR_RES.isEmpty() || 
			DIR_DATA.isEmpty() || 
			DIR_REPORTS.isEmpty() || 
			DIR_LOGS.isEmpty() )
			return( false );
		
		LocalFolder folder = action.getLocalFolder( DIR_DATA );
		folder.ensureExists( action );
		folder = action.getLocalFolder( DIR_REPORTS );
		folder.ensureExists( action );
		folder = action.getLocalFolder( DIR_LOGS );
		folder.ensureExists( action );
		return( true );
	}
	
	public MetaMonitoringTarget findMonitoringTarget( MetaEnvSegment sg ) {
		for( MetaMonitoringTarget target : mapTargets.values() ) {
			if( target.ENV.equals( sg.env.ID ) && target.SG.equals( sg.NAME ) )
				return( target );
		}
		return( null );
	}

	public void setMonitoringEnabled( EngineTransaction transaction , boolean enabled ) throws Exception {
		super.setBooleanProperty( PROPERTY_ENABLED , enabled );
		ENABLED = enabled;
	}

	public MetaMonitoringTarget modifyTarget( EngineTransaction transaction , MetaEnvSegment sg , boolean major , boolean enabled , int maxTime , ScheduleProperties schedule ) throws Exception {
		MetaMonitoringTarget target = findMonitoringTarget( sg );
		if( target == null ) {
			target = new MetaMonitoringTarget( meta , this );
			target.createTarget( transaction , sg );
			addTarget( target );
		}
		
		target.modifyTarget( transaction , major , enabled , schedule , maxTime );
		return( target );
	}

	public void setProductProperties( EngineTransaction transaction , PropertySet props ) throws Exception {
		super.updateProperties( props );
		setMonitoringEnabled( transaction , false );
		scatterProperties( transaction.getAction() );
	}
	
}
