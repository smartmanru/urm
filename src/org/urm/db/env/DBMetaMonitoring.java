package org.urm.db.env;

import org.urm.action.ActionBase;
import org.urm.common.Common;
import org.urm.common.ConfReader;
import org.urm.db.DBConnection;
import org.urm.db.EngineDB;
import org.urm.db.engine.DBEngineEntities;
import org.urm.engine.data.EngineEntities;
import org.urm.engine.properties.PropertyEntity;
import org.urm.engine.schedule.ScheduleProperties;
import org.urm.engine.transaction.EngineTransaction;
import org.urm.meta.EngineLoader;
import org.urm.meta.env.MetaEnv;
import org.urm.meta.env.MetaEnvSegment;
import org.urm.meta.env.MetaMonitoring;
import org.urm.meta.env.MetaMonitoringItem;
import org.urm.meta.env.MetaMonitoringTarget;
import org.urm.meta.env.ProductEnvs;
import org.urm.meta.product.ProductMeta;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class DBMetaMonitoring {

	public static String ELEMENT_TARGET = "target";
	public static String ELEMENT_CHECKURL = "checkurl";
	public static String ELEMENT_CHECKWS = "checkws";
	public static String ATTR_ENV = "env";
	public static String ATTR_SEGMENT = "segment";
	
	public static void importxml( EngineLoader loader , ProductMeta storage , ProductEnvs envs , Node root ) throws Exception {
		MetaMonitoring mon = envs.getMonitoring();
		
		Node[] items = ConfReader.xmlGetChildren( root , ELEMENT_TARGET );
		if( items == null )
			return;
		
		for( Node node : items ) {
			MetaMonitoringTarget target = importxmlTarget( loader , storage , mon , node );
			mon.addTarget( target );
		}
	}
	
	public static MetaMonitoringTarget importxmlTarget( EngineLoader loader , ProductMeta storage , MetaMonitoring mon , Node root ) throws Exception {
		DBConnection c = loader.getConnection();
		ActionBase action = loader.getAction();
		EngineEntities entities = loader.getEntities();
		PropertyEntity entity = entities.entityAppSegmentMonTarget;
		
		MetaMonitoringTarget target = new MetaMonitoringTarget( storage.meta , mon );
		
		String envName = ConfReader.getRequiredAttrValue( root , ATTR_ENV );
		String sgName = ConfReader.getRequiredAttrValue( root , ATTR_SEGMENT );
		
		ProductEnvs envs = storage.getEnviroments();
		MetaEnv env = envs.findMetaEnv( envName );
		if( env == null )
			Common.exitUnexpected();
		
		MetaEnvSegment sg = env.getSegment( sgName );
		target.createTarget( sg );
		
		ScheduleProperties propsMajor = new ScheduleProperties();
		propsMajor.setScheduleData( action , entity.importxmlStringProperty( root , MetaMonitoringTarget.PROPERTY_MAJOR_SCHEDULE ) );
		target.modifyTarget( 
				true , 
				entity.importxmlBooleanProperty( root , MetaMonitoringTarget.PROPERTY_MAJOR_ENABLED , false ) ,
				propsMajor ,
				entity.importxmlIntProperty( root , MetaMonitoringTarget.PROPERTY_MAJOR_MAXTIME ) );
		
		ScheduleProperties propsMinor = new ScheduleProperties();
		propsMinor.setScheduleData( action , entity.importxmlStringAttr( root , MetaMonitoringTarget.PROPERTY_MINOR_SCHEDULE ) );
		target.modifyTarget( 
				false , 
				entity.importxmlBooleanProperty( root , MetaMonitoringTarget.PROPERTY_MINOR_ENABLED , false ) ,
				propsMajor ,
				entity.importxmlIntProperty( root , MetaMonitoringTarget.PROPERTY_MINOR_MAXTIME ) );
		modifyTarget( c , storage , env , target , true );
		
		importxmlTargetUrls( loader , storage , mon , env , target , root );
		importxmlTargetWSs( loader , storage , mon , env , target , root );
		return( target );
	}

	public static void importxmlTargetUrls( EngineLoader loader , ProductMeta storage , MetaMonitoring mon , MetaEnv env , MetaMonitoringTarget target , Node root ) throws Exception {
		DBConnection c = loader.getConnection();
		EngineEntities entities = loader.getEntities();
		PropertyEntity entity = entities.entityAppSegmentMonItem;
		
		Node[] items = ConfReader.xmlGetChildren( root , ELEMENT_CHECKURL );
		if( items == null )
			return;
		
		for( Node node : items ) {
			MetaMonitoringItem item = new MetaMonitoringItem( storage.meta , target );
			item.create( entity.importxmlStringAttr( node , MetaMonitoringItem.PROPERTY_URL ) , "" , "" );
			target.addUrl( item );
			modifyItem( c , storage , env , item , true );
		}
	}
	
	public static void importxmlTargetWSs( EngineLoader loader , ProductMeta storage , MetaMonitoring mon , MetaEnv env , MetaMonitoringTarget target , Node root ) throws Exception {
		DBConnection c = loader.getConnection();
		ActionBase action = loader.getAction();
		EngineEntities entities = loader.getEntities();
		PropertyEntity entity = entities.entityAppSegmentMonItem;
		
		Node[] items = ConfReader.xmlGetChildren( root , ELEMENT_CHECKWS );
		if( items == null )
			return;
		
		for( Node node : items ) {
			MetaMonitoringItem item = new MetaMonitoringItem( storage.meta , target );
			item.create( 
					entity.importxmlStringAttr( node , MetaMonitoringItem.PROPERTY_URL ) , 
					getNodeSubTree( action , node , MetaMonitoringItem.PROPERTY_WSDATA ) ,
					getNodeSubTree( action , node , MetaMonitoringItem.PROPERTY_WSCHECK ) );
			target.addWS( item );
			modifyItem( c , storage , env , item , true );
		}
	}
	
	public static void loaddbProductMonitoring( EngineLoader loader , ProductMeta storage , ProductEnvs envs ) throws Exception {
	}
	
	private static void modifyTarget( DBConnection c , ProductMeta storage , MetaEnv env , MetaMonitoringTarget target , boolean insert ) throws Exception {
		if( insert )
			target.ID = c.getNextSequenceValue();
		
		target.EV = c.getNextEnvironmentVersion( env );
		EngineEntities entities = c.getEntities();
		DBEngineEntities.modifyAppObject( c , entities.entityAppSegmentMonTarget , target.ID , target.EV , new String[] {
				EngineDB.getObject( env.ID ) ,
				EngineDB.getObject( target.SEGMENT_ID ) ,
				EngineDB.getBoolean( target.MAJOR_ENABLED ) ,
				EngineDB.getString( target.majorSchedule.getScheduleData() ) ,
				EngineDB.getInteger( target.MAJOR_MAXTIME ) ,
				EngineDB.getBoolean( target.MINOR_ENABLED ) ,
				EngineDB.getString( target.minorSchedule.getScheduleData() ) ,
				EngineDB.getInteger( target.MINOR_MAXTIME ) ,
				} , insert );
	}
	
	private static void modifyItem( DBConnection c , ProductMeta storage , MetaEnv env , MetaMonitoringItem item , boolean insert ) throws Exception {
		if( insert )
			item.ID = c.getNextSequenceValue();
		
		item.EV = c.getNextEnvironmentVersion( env );
		EngineEntities entities = c.getEntities();
		DBEngineEntities.modifyAppObject( c , entities.entityAppSegmentMonItem , item.ID , item.EV , new String[] {
				EngineDB.getObject( env.ID ) ,
				EngineDB.getObject( item.target.ID ) ,
				EngineDB.getEnum( item.MONITEM_TYPE ) ,
				EngineDB.getString( item.URL ) ,
				EngineDB.getString( item.WSDATA ) ,
				EngineDB.getString( item.WSCHECK )
				} , insert );
	}
	
	public static void exportxml( EngineLoader loader , ProductMeta storage , Document doc , Element root ) throws Exception {
	}

	public static MetaMonitoringTarget modifyTarget( EngineTransaction transaction , ProductMeta storage , MetaEnv env , MetaEnvSegment sg , boolean major , boolean enabled , int maxTime , ScheduleProperties schedule ) throws Exception {
		DBConnection c = transaction.getConnection();
		ProductEnvs envs = storage.getEnviroments();
		MetaMonitoring mon = envs.getMonitoring();
				
		MetaMonitoringTarget target = mon.findMonitoringTarget( sg );
		if( target == null ) {
			target = new MetaMonitoringTarget( storage.meta , mon );
			target.createTarget( sg );
			target.modifyTarget( major , enabled , schedule , maxTime );
			mon.addTarget( target );
			modifyTarget( c , storage , env , target , true );
		}
		else {
			target.modifyTarget( major , enabled , schedule , maxTime );
			modifyTarget( c , storage , env , target , false );
		}
		
		return( target );
	}

	private static String getNodeSubTree( ActionBase action , Node node , String name ) throws Exception {
		Node parent = ConfReader.xmlGetFirstChild( node , name );
		if( parent == null )
			return( null );
		
		Node content = parent.getFirstChild();
		if( content == null )
			return( null );
		
		return( ConfReader.getNodeSubTree( content ) );
	}
	
}
