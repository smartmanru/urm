package org.urm.db.system;

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
import org.urm.meta.env.MetaEnvSegment;
import org.urm.meta.loader.EngineLoader;
import org.urm.meta.loader._Error;
import org.urm.meta.system.AppProduct;
import org.urm.meta.system.AppProductMonitoring;
import org.urm.meta.system.AppProductMonitoringItem;
import org.urm.meta.system.AppProductMonitoringTarget;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class DBAppProductMonitoring {

	public static String ELEMENT_TARGET = "target";
	public static String ELEMENT_CHECKURL = "checkurl";
	public static String ELEMENT_CHECKWS = "checkws";
	
	public static void importxmlAll( EngineLoader loader , AppProduct product , Node root ) throws Exception {
		ActionBase action = loader.getAction();
		try {
			// monitoring settings
			DBAppProductMonitoring.importxmlTargets( loader , product , root );
		}
		catch( Throwable e ) {
			loader.log( "import monitoring data" , e );
			loader.setLoadFailed( action , _Error.UnableLoadProductMonitoring1 , e , "unable to import monitoring metadata, product=" + product.NAME , product.NAME );
		}
	}

	public static void importxmlTargets( EngineLoader loader , AppProduct product , Node root ) throws Exception {
		AppProductMonitoring mon = product.getMonitoring();
		
		Node[] items = ConfReader.xmlGetChildren( root , ELEMENT_TARGET );
		if( items == null )
			return;
		
		for( Node node : items ) {
			AppProductMonitoringTarget target = importxmlTarget( loader , product , mon , node );
			mon.addTarget( target );
		}
	}
	
	public static AppProductMonitoringTarget importxmlTarget( EngineLoader loader , AppProduct product , AppProductMonitoring mon , Node root ) throws Exception {
		DBConnection c = loader.getConnection();
		ActionBase action = loader.getAction();
		EngineEntities entities = loader.getEntities();
		PropertyEntity entity = entities.entityAppProductMonTarget;
		
		AppProductMonitoringTarget target = new AppProductMonitoringTarget( product , mon );
		
		String envName = entity.importxmlStringProperty( root , AppProductMonitoringTarget.PROPERTY_ENV );
		String sgName = entity.importxmlStringProperty( root , AppProductMonitoringTarget.PROPERTY_SEGMENT );
		
		target.createTarget( null , envName , sgName );
		
		ScheduleProperties propsMajor = new ScheduleProperties();
		propsMajor.setScheduleData( action , entity.importxmlStringProperty( root , AppProductMonitoringTarget.PROPERTY_MAJOR_SCHEDULE ) );
		target.modifyTarget( 
				true , 
				entity.importxmlBooleanProperty( root , AppProductMonitoringTarget.PROPERTY_MAJOR_ENABLED , false ) ,
				propsMajor ,
				entity.importxmlIntProperty( root , AppProductMonitoringTarget.PROPERTY_MAJOR_MAXTIME ) );
		
		ScheduleProperties propsMinor = new ScheduleProperties();
		propsMinor.setScheduleData( action , entity.importxmlStringAttr( root , AppProductMonitoringTarget.PROPERTY_MINOR_SCHEDULE ) );
		target.modifyTarget( 
				false , 
				entity.importxmlBooleanProperty( root , AppProductMonitoringTarget.PROPERTY_MINOR_ENABLED , false ) ,
				propsMajor ,
				entity.importxmlIntProperty( root , AppProductMonitoringTarget.PROPERTY_MINOR_MAXTIME ) );
		modifyTarget( c , product , target , true );
		
		importxmlTargetUrls( loader , product , mon , target , root );
		importxmlTargetWSs( loader , product , mon , target , root );
		return( target );
	}

	public static void importxmlTargetUrls( EngineLoader loader , AppProduct product , AppProductMonitoring mon , AppProductMonitoringTarget target , Node root ) throws Exception {
		DBConnection c = loader.getConnection();
		EngineEntities entities = loader.getEntities();
		PropertyEntity entity = entities.entityAppProductMonItem;
		
		Node[] items = ConfReader.xmlGetChildren( root , ELEMENT_CHECKURL );
		if( items == null )
			return;
		
		for( Node node : items ) {
			AppProductMonitoringItem item = new AppProductMonitoringItem( product , target );
			item.create(
					DBEnumMonItemType.CHECKURL ,
					entity.importxmlStringAttr( node , AppProductMonitoringItem.PROPERTY_URL ) ,
					entity.importxmlStringAttr( node , AppProductMonitoringItem.PROPERTY_DESC ) ,
					"" , 
					""
					);
			
			modifyItem( c , product , target , item , true );
			target.addUrl( item );
		}
	}
	
	public static void importxmlTargetWSs( EngineLoader loader , AppProduct product , AppProductMonitoring mon , AppProductMonitoringTarget target , Node root ) throws Exception {
		DBConnection c = loader.getConnection();
		ActionBase action = loader.getAction();
		EngineEntities entities = loader.getEntities();
		PropertyEntity entity = entities.entityAppProductMonItem;
		
		Node[] items = ConfReader.xmlGetChildren( root , ELEMENT_CHECKWS );
		if( items == null )
			return;
		
		for( Node node : items ) {
			AppProductMonitoringItem item = new AppProductMonitoringItem( product , target );
			item.create( 
					DBEnumMonItemType.CHECKWS ,
					entity.importxmlStringAttr( node , AppProductMonitoringItem.PROPERTY_URL ) ,
					entity.importxmlStringAttr( node , AppProductMonitoringItem.PROPERTY_DESC ) ,
					getNodeSubTree( action , node , AppProductMonitoringItem.PROPERTY_WSDATA ) ,
					getNodeSubTree( action , node , AppProductMonitoringItem.PROPERTY_WSCHECK )
					);
			
			modifyItem( c , product , target , item , true );
			target.addWS( item );
		}
	}
	
	public static void loaddbAll( EngineLoader loader , AppProduct product ) throws Exception {
		loaddbProductMonitoringTargets( loader , product );
		loaddbProductMonitoringItems( loader , product );
	}
	
	private static void loaddbProductMonitoringTargets( EngineLoader loader , AppProduct product ) throws Exception {
		DBConnection c = loader.getConnection();
		ActionBase action = loader.getAction();
		EngineEntities entities = loader.getEntities();
		AppProductMonitoring mon = product.getMonitoring();
		PropertyEntity entity = entities.entityAppProductMonTarget;

		// targets
		ResultSet rs = DBEngineEntities.listAppObjectsFiltered( c , entity , DBQueries.FILTER_PRODUCT_ID1 , new String[] { 
				EngineDB.getInteger( product.ID ) 
				} );
		try {
			while( rs.next() ) {
				AppProductMonitoringTarget target = new AppProductMonitoringTarget( product , mon );
				target.ID = entity.loaddbId( rs );
				target.SV = entity.loaddbVersion( rs );
				
				target.createTarget( 
						entity.loaddbObject( rs , DBSystemData.FIELD_MONTARGET_SEGMENT_ID ) ,
						entity.loaddbString( rs , AppProductMonitoringTarget.PROPERTY_ENV ) ,
						entity.loaddbString( rs , AppProductMonitoringTarget.PROPERTY_SEGMENT )
						);

				target.majorSchedule.setScheduleData( action , entity.loaddbString( rs , AppProductMonitoringTarget.PROPERTY_MAJOR_SCHEDULE ) );
				target.modifyTarget( true ,  
						entity.loaddbBoolean( rs , AppProductMonitoringTarget.PROPERTY_MAJOR_ENABLED ) ,
						target.majorSchedule ,
						entity.loaddbInt( rs , AppProductMonitoringTarget.PROPERTY_MAJOR_MAXTIME )
						);
				
				target.minorSchedule.setScheduleData( action , entity.loaddbString( rs , AppProductMonitoringTarget.PROPERTY_MINOR_SCHEDULE ) );
				target.modifyTarget( false ,  
						entity.loaddbBoolean( rs , AppProductMonitoringTarget.PROPERTY_MINOR_ENABLED ) ,
						target.minorSchedule ,
						entity.loaddbInt( rs , AppProductMonitoringTarget.PROPERTY_MINOR_MAXTIME )
						);
				
				mon.addTarget( target );
			}
		}
		finally {
			c.closeQuery();
		}
	}
	
	private static void loaddbProductMonitoringItems( EngineLoader loader , AppProduct product ) throws Exception {
		DBConnection c = loader.getConnection();
		EngineEntities entities = loader.getEntities();
		AppProductMonitoring mon = product.getMonitoring();
		PropertyEntity entity = entities.entityAppProductMonItem;

		// targets
		ResultSet rs = DBEngineEntities.listAppObjectsFiltered( c , entity , DBQueries.FILTER_PRODUCT_ID1 , new String[] { 
				EngineDB.getInteger( product.ID ) 
				} );
		try {
			while( rs.next() ) {
				int targetId = entity.loaddbObject( rs , DBSystemData.FIELD_MONITEM_TARGET_ID );
				AppProductMonitoringTarget target = mon.getTarget( targetId );
				
				AppProductMonitoringItem item = new AppProductMonitoringItem( product , target );
				item.ID = entity.loaddbId( rs );
				item.SV = entity.loaddbVersion( rs );

				item.create(
						DBEnumMonItemType.getValue( entity.loaddbEnum( rs , AppProductMonitoringItem.PROPERTY_TYPE ) , true ) ,
						entity.loaddbString( rs , AppProductMonitoringItem.PROPERTY_URL ) ,
						entity.loaddbString( rs , AppProductMonitoringItem.PROPERTY_DESC ) ,
						entity.loaddbString( rs , AppProductMonitoringItem.PROPERTY_WSDATA ) ,
						entity.loaddbString( rs , AppProductMonitoringItem.PROPERTY_WSCHECK )
						);
				
				target.addItem( item );
			}
		}
		finally {
			c.closeQuery();
		}
	}
	
	private static void modifyTarget( DBConnection c , AppProduct product , AppProductMonitoringTarget target , boolean insert ) throws Exception {
		if( insert )
			target.ID = c.getNextSequenceValue();
		
		target.SV = c.getNextSystemVersion( product.system );
		EngineEntities entities = c.getEntities();
		DBEngineEntities.modifyAppObject( c , entities.entityAppProductMonTarget , target.ID , target.SV , new String[] {
				EngineDB.getObject( product.ID ) ,
				EngineDB.getMatchId( target.SEGMENT ) ,
				EngineDB.getString( target.FKENV ) ,
				EngineDB.getString( target.FKSG ) ,
				EngineDB.getBoolean( target.MAJOR_ENABLED ) ,
				EngineDB.getString( target.majorSchedule.getScheduleData() ) ,
				EngineDB.getInteger( target.MAJOR_MAXTIME ) ,
				EngineDB.getBoolean( target.MINOR_ENABLED ) ,
				EngineDB.getString( target.minorSchedule.getScheduleData() ) ,
				EngineDB.getInteger( target.MINOR_MAXTIME ) ,
				} , insert );
	}
	
	private static void modifyItem( DBConnection c , AppProduct product , AppProductMonitoringTarget target , AppProductMonitoringItem item , boolean insert ) throws Exception {
		if( insert )
			item.ID = c.getNextSequenceValue();
		
		item.SV = c.getNextSystemVersion( product.system );
		EngineEntities entities = c.getEntities();
		DBEngineEntities.modifyAppObject( c , entities.entityAppProductMonItem , item.ID , item.SV , new String[] {
				EngineDB.getObject( product.ID ) ,
				EngineDB.getObject( item.target.ID ) ,
				EngineDB.getString( item.DESC ) ,
				EngineDB.getEnum( item.MONITEM_TYPE ) ,
				EngineDB.getString( item.URL ) ,
				EngineDB.getString( item.WSDATA ) ,
				EngineDB.getString( item.WSCHECK )
				} , insert );
	}
	
	public static void exportxmlAll( EngineLoader loader , AppProduct product , Document doc , Element root ) throws Exception {
		AppProductMonitoring mon = product.getMonitoring();
		
		for( AppProductMonitoringTarget target : mon.getTargets() ) {
			Element node = Common.xmlCreateElement( doc , root , ELEMENT_TARGET );
			exportxmlTarget( loader , product , target , doc , node );
		}
	}

	private static void exportxmlTarget( EngineLoader loader , AppProduct product , AppProductMonitoringTarget target , Document doc , Element root ) throws Exception {
		EngineEntities entities = loader.getEntities();
		PropertyEntity entity = entities.entityAppProductMonTarget;
		
		MetaEnvSegment sg = target.findSegment();
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
		
		for( AppProductMonitoringItem item : target.getUrlsList() ) {
			Element node = Common.xmlCreateElement( doc , root , ELEMENT_CHECKURL );
			exportxmlTargetItem( loader , product , target , item , doc , node );
		}
	}

	private static void exportxmlTargetItem( EngineLoader loader , AppProduct product , AppProductMonitoringTarget target , AppProductMonitoringItem item , Document doc , Element root ) throws Exception {
		Common.xmlSetElementAttr( doc , root , AppProductMonitoringItem.PROPERTY_URL , item.URL );
		Common.xmlSetElementAttr( doc , root , AppProductMonitoringItem.PROPERTY_DESC , item.DESC );
		
		if( item.MONITEM_TYPE == DBEnumMonItemType.CHECKWS ) {
			Common.xmlSetElementAttr( doc , root , AppProductMonitoringItem.PROPERTY_WSDATA , item.WSDATA );
			Common.xmlSetElementAttr( doc , root , AppProductMonitoringItem.PROPERTY_WSCHECK , item.WSCHECK );
		}
	}
	
	public static AppProductMonitoringTarget modifyTarget( EngineTransaction transaction , AppProduct product , MetaEnvSegment sg , boolean major , boolean enabled , int maxTime , ScheduleProperties schedule ) throws Exception {
		DBConnection c = transaction.getConnection();
		AppProductMonitoring mon = product.getMonitoring();
				
		AppProductMonitoringTarget target = mon.findTarget( sg );
		if( target == null ) {
			target = new AppProductMonitoringTarget( product , mon );
			target.createTarget( sg );
			target.modifyTarget( major , enabled , schedule , maxTime );
			
			modifyTarget( c , product , target , true );
			mon.addTarget( target );
		}
		else {
			target.modifyTarget( major , enabled , schedule , maxTime );
			modifyTarget( c , product , target , false );
		}
		
		return( target );
	}

	public static AppProductMonitoringItem createTargetItem( EngineTransaction transaction , AppProduct product , AppProductMonitoringTarget target , DBEnumMonItemType type , String url , String desc , String wsdata , String wscheck ) throws Exception {
		DBConnection c = transaction.getConnection();
				
		AppProductMonitoringItem item = new AppProductMonitoringItem( product , target );
		item.create( type , url , desc , wsdata , wscheck );
		modifyItem( c , product , target , item , true );
		
		if( type == DBEnumMonItemType.CHECKURL )
			target.addUrl( item );
		else
		if( type == DBEnumMonItemType.CHECKWS )
			target.addWS( item );
		else
			Common.exitUnexpected();
			
		return( item );
	}

	public static void modifyTargetItem( EngineTransaction transaction , AppProduct product , AppProductMonitoringTarget target , AppProductMonitoringItem item , String url , String desc , String wsdata , String wscheck ) throws Exception {
		DBConnection c = transaction.getConnection();
				
		if( item.MONITEM_TYPE == DBEnumMonItemType.CHECKURL )
			item.modifyPage( url , desc );
		else
		if( item.MONITEM_TYPE == DBEnumMonItemType.CHECKWS )
			item.modifyWebService( url , desc , wsdata , wscheck );
		else
			Common.exitUnexpected();
			
		modifyItem( c , product , target , item , true );
	}

	public static void deleteTargetItem( EngineTransaction transaction , AppProduct product , AppProductMonitoringTarget target , AppProductMonitoringItem item ) throws Exception {
		DBConnection c = transaction.getConnection();
		EngineEntities entities = c.getEntities();

		int version = c.getNextSystemVersion( product.system );
		DBEngineEntities.deleteAppObject( c , entities.entityAppProductMonItem , item.ID , version );
		
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
