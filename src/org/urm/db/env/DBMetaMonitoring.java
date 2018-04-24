package org.urm.db.env;

import java.sql.ResultSet;

import org.urm.action.ActionBase;
import org.urm.common.Common;
import org.urm.common.ConfReader;
import org.urm.db.DBConnection;
import org.urm.db.DBQueries;
import org.urm.db.EngineDB;
import org.urm.db.core.DBEnums.*;
import org.urm.db.engine.DBEngineEntities;
import org.urm.engine.data.EngineEntities;
import org.urm.engine.properties.PropertyEntity;
import org.urm.engine.schedule.ScheduleProperties;
import org.urm.engine.transaction.EngineTransaction;
import org.urm.meta.env.MetaEnv;
import org.urm.meta.env.MetaEnvSegment;
import org.urm.meta.env.MetaMonitoring;
import org.urm.meta.env.MetaMonitoringItem;
import org.urm.meta.env.MetaMonitoringTarget;
import org.urm.meta.env.ProductEnvs;
import org.urm.meta.loader.EngineLoader;
import org.urm.meta.product.ProductMeta;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class DBMetaMonitoring {

	public static String ELEMENT_TARGET = "target";
	public static String ELEMENT_CHECKURL = "checkurl";
	public static String ELEMENT_CHECKWS = "checkws";
	
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
		
		MetaMonitoringTarget target = new MetaMonitoringTarget( storage.getEnviroments() , mon );
		
		String envName = entity.importxmlStringProperty( root , MetaMonitoringTarget.PROPERTY_ENV );
		String sgName = entity.importxmlStringProperty( root , MetaMonitoringTarget.PROPERTY_SEGMENT );
		
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
			MetaMonitoringItem item = new MetaMonitoringItem( storage.getEnviroments() , target );
			item.create(
					DBEnumMonItemType.CHECKURL ,
					entity.importxmlStringAttr( node , MetaMonitoringItem.PROPERTY_URL ) ,
					entity.importxmlStringAttr( node , MetaMonitoringItem.PROPERTY_DESC ) ,
					"" , 
					""
					);
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
			MetaMonitoringItem item = new MetaMonitoringItem( storage.getEnviroments() , target );
			item.create( 
					DBEnumMonItemType.CHECKWS ,
					entity.importxmlStringAttr( node , MetaMonitoringItem.PROPERTY_URL ) ,
					entity.importxmlStringAttr( node , MetaMonitoringItem.PROPERTY_DESC ) ,
					getNodeSubTree( action , node , MetaMonitoringItem.PROPERTY_WSDATA ) ,
					getNodeSubTree( action , node , MetaMonitoringItem.PROPERTY_WSCHECK )
					);
			target.addWS( item );
			modifyItem( c , storage , env , item , true );
		}
	}
	
	public static void loaddbProductMonitoring( EngineLoader loader , ProductMeta storage , ProductEnvs envs ) throws Exception {
		loaddbProductMonitoringTargets( loader , storage , envs );
		loaddbProductMonitoringItems( loader , storage , envs );
	}
	
	private static void loaddbProductMonitoringTargets( EngineLoader loader , ProductMeta storage , ProductEnvs envs ) throws Exception {
		DBConnection c = loader.getConnection();
		ActionBase action = loader.getAction();
		EngineEntities entities = loader.getEntities();
		MetaMonitoring mon = envs.getMonitoring();
		PropertyEntity entity = entities.entityAppSegmentMonTarget;

		// targets
		ResultSet rs = DBEngineEntities.listAppObjectsFiltered( c , entity , DBQueries.FILTER_ENV_META1 , new String[] { 
				EngineDB.getInteger( storage.ID ) 
				} );
		try {
			while( rs.next() ) {
				MetaMonitoringTarget target = new MetaMonitoringTarget( envs , mon );
				target.ID = entity.loaddbId( rs );
				target.EV = entity.loaddbVersion( rs );
				
				int envId = entity.loaddbObject( rs , DBEnvData.FIELD_ENV_ID );
				MetaEnv env = envs.getMetaEnv( envId );
				
				int segmentId = entity.loaddbObject( rs , DBEnvData.FIELD_MONTARGET_SEGMENT_ID );
				MetaEnvSegment sg = env.getSegment( segmentId );
				target.createTarget( sg );

				target.majorSchedule.setScheduleData( action , entity.loaddbString( rs , DBEnvData.FIELD_MONTARGET_MAJOR_SCHEDULE ) );
				target.modifyTarget( true ,  
						entity.loaddbBoolean( rs , MetaMonitoringTarget.PROPERTY_MAJOR_ENABLED ) ,
						target.majorSchedule ,
						entity.loaddbInt( rs , MetaMonitoringTarget.PROPERTY_MAJOR_MAXTIME )
						);
				
				target.minorSchedule.setScheduleData( action , entity.loaddbString( rs , DBEnvData.FIELD_MONTARGET_MINOR_SCHEDULE ) );
				target.modifyTarget( false ,  
						entity.loaddbBoolean( rs , MetaMonitoringTarget.PROPERTY_MINOR_ENABLED ) ,
						target.minorSchedule ,
						entity.loaddbInt( rs , MetaMonitoringTarget.PROPERTY_MINOR_MAXTIME )
						);
				
				mon.addTarget( target );
			}
		}
		finally {
			c.closeQuery();
		}
	}
	
	private static void loaddbProductMonitoringItems( EngineLoader loader , ProductMeta storage , ProductEnvs envs ) throws Exception {
		DBConnection c = loader.getConnection();
		EngineEntities entities = loader.getEntities();
		MetaMonitoring mon = envs.getMonitoring();
		PropertyEntity entity = entities.entityAppSegmentMonItem;

		// targets
		ResultSet rs = DBEngineEntities.listAppObjectsFiltered( c , entity , DBQueries.FILTER_ENV_META1 , new String[] { 
				EngineDB.getInteger( storage.ID ) 
				} );
		try {
			while( rs.next() ) {
				int targetId = entity.loaddbObject( rs , DBEnvData.FIELD_MONITEM_TARGET_ID );
				MetaMonitoringTarget target = mon.getTarget( targetId );
				
				MetaMonitoringItem item = new MetaMonitoringItem( envs , target );
				item.ID = entity.loaddbId( rs );
				item.EV = entity.loaddbVersion( rs );

				item.create(
						DBEnumMonItemType.getValue( entity.loaddbEnum( rs , MetaMonitoringItem.PROPERTY_TYPE ) , true ) ,
						entity.loaddbString( rs , MetaMonitoringItem.PROPERTY_URL ) ,
						entity.loaddbString( rs , MetaMonitoringItem.PROPERTY_DESC ) ,
						entity.loaddbString( rs , MetaMonitoringItem.PROPERTY_WSDATA ) ,
						entity.loaddbString( rs , MetaMonitoringItem.PROPERTY_WSCHECK )
						);
				
				target.addItem( item );
			}
		}
		finally {
			c.closeQuery();
		}
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
				EngineDB.getString( item.DESC ) ,
				EngineDB.getEnum( item.MONITEM_TYPE ) ,
				EngineDB.getString( item.URL ) ,
				EngineDB.getString( item.WSDATA ) ,
				EngineDB.getString( item.WSCHECK )
				} , insert );
	}
	
	public static void exportxml( EngineLoader loader , ProductMeta storage , Document doc , Element root ) throws Exception {
		ProductEnvs envs = storage.getEnviroments();
		MetaMonitoring mon = envs.getMonitoring();
		
		for( MetaMonitoringTarget target : mon.getTargets() ) {
			Element node = Common.xmlCreateElement( doc , root , ELEMENT_TARGET );
			exportxmlTarget( loader , storage , target , doc , node );
		}
	}

	private static void exportxmlTarget( EngineLoader loader , ProductMeta storage , MetaMonitoringTarget target , Document doc , Element root ) throws Exception {
		EngineEntities entities = loader.getEntities();
		PropertyEntity entity = entities.entityAppSegmentMonTarget;
		
		MetaEnvSegment sg = target.getSegment();
		DBEngineEntities.exportxmlAppObject( doc , root , entity , new String[] {
				entity.exportxmlString( sg.env.NAME ) ,
				entity.exportxmlString( sg.NAME ) ,
				entity.exportxmlBoolean( target.MAJOR_ENABLED ) ,
				entity.exportxmlString( target.majorSchedule.getScheduleData() ) ,
				entity.exportxmlInt( target.MAJOR_MAXTIME ) ,
				entity.exportxmlBoolean( target.MINOR_ENABLED ) ,
				entity.exportxmlString( target.minorSchedule.getScheduleData() ) ,
				entity.exportxmlInt( target.MINOR_MAXTIME )
		} , false );
		
		for( MetaMonitoringItem item : target.getUrlsList() ) {
			Element node = Common.xmlCreateElement( doc , root , ELEMENT_CHECKURL );
			exportxmlTargetItem( loader , storage , item , doc , node );
		}
	}

	private static void exportxmlTargetItem( EngineLoader loader , ProductMeta storage , MetaMonitoringItem item , Document doc , Element root ) throws Exception {
		Common.xmlSetElementAttr( doc , root , MetaMonitoringItem.PROPERTY_URL , item.URL );
		Common.xmlSetElementAttr( doc , root , MetaMonitoringItem.PROPERTY_DESC , item.DESC );
		
		if( item.MONITEM_TYPE == DBEnumMonItemType.CHECKWS ) {
			Common.xmlSetElementAttr( doc , root , MetaMonitoringItem.PROPERTY_WSDATA , item.WSDATA );
			Common.xmlSetElementAttr( doc , root , MetaMonitoringItem.PROPERTY_WSCHECK , item.WSCHECK );
		}
	}
	
	public static MetaMonitoringTarget modifyTarget( EngineTransaction transaction , ProductMeta storage , MetaEnv env , MetaEnvSegment sg , boolean major , boolean enabled , int maxTime , ScheduleProperties schedule ) throws Exception {
		DBConnection c = transaction.getConnection();
		ProductEnvs envs = storage.getEnviroments();
		MetaMonitoring mon = envs.getMonitoring();
				
		MetaMonitoringTarget target = mon.findTarget( sg );
		if( target == null ) {
			target = new MetaMonitoringTarget( storage.getEnviroments() , mon );
			target.createTarget( sg );
			target.modifyTarget( major , enabled , schedule , maxTime );
			modifyTarget( c , storage , env , target , true );
			mon.addTarget( target );
		}
		else {
			target.modifyTarget( major , enabled , schedule , maxTime );
			modifyTarget( c , storage , env , target , false );
		}
		
		return( target );
	}

	public static MetaMonitoringItem createTargetItem( EngineTransaction transaction , ProductMeta storage , MetaEnv env , MetaMonitoringTarget target , DBEnumMonItemType type , String url , String desc , String wsdata , String wscheck ) throws Exception {
		DBConnection c = transaction.getConnection();
				
		MetaMonitoringItem item = new MetaMonitoringItem( storage.getEnviroments() , target );
		item.create( type , url , desc , wsdata , wscheck );
		modifyItem( c , storage , env , item , true );
		
		if( type == DBEnumMonItemType.CHECKURL )
			target.addUrl( item );
		else
		if( type == DBEnumMonItemType.CHECKWS )
			target.addUrl( item );
		else
			Common.exitUnexpected();
			
		return( item );
	}

	public static void modifyTargetItem( EngineTransaction transaction , ProductMeta storage , MetaEnv env , MetaMonitoringTarget target , MetaMonitoringItem item , String url , String desc , String wsdata , String wscheck ) throws Exception {
		DBConnection c = transaction.getConnection();
				
		if( item.MONITEM_TYPE == DBEnumMonItemType.CHECKURL )
			item.modifyPage( url , desc );
		else
		if( item.MONITEM_TYPE == DBEnumMonItemType.CHECKWS )
			item.modifyWebService( url , desc , wsdata , wscheck );
		else
			Common.exitUnexpected();
			
		modifyItem( c , storage , env , item , true );
	}

	public static void deleteTargetItem( EngineTransaction transaction , ProductMeta storage , MetaEnv env , MetaMonitoringTarget target , MetaMonitoringItem item ) throws Exception {
		DBConnection c = transaction.getConnection();
		EngineEntities entities = c.getEntities();

		int version = c.getNextEnvironmentVersion( env );
		DBEngineEntities.deleteAppObject( c , entities.entityAppSegmentMonItem , item.ID , version );
		
		target.removeItem( item );
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
