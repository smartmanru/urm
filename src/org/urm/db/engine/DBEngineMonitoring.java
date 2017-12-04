package org.urm.db.engine;

import org.urm.db.DBConnection;
import org.urm.db.core.DBSettings;
import org.urm.db.core.DBVersions;
import org.urm.db.core.DBEnums.DBEnumObjectType;
import org.urm.db.core.DBEnums.DBEnumObjectVersionType;
import org.urm.db.core.DBEnums.DBEnumParamEntityType;
import org.urm.engine.properties.EngineEntities;
import org.urm.engine.properties.EntityVar;
import org.urm.engine.properties.ObjectProperties;
import org.urm.engine.properties.PropertyEntity;
import org.urm.meta.EngineLoader;
import org.urm.meta.engine.EngineContext;
import org.urm.meta.engine.EngineMonitoring;
import org.urm.meta.engine.EngineSettings;
import org.urm.meta.product.MetaProductCoreSettings;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class DBEngineMonitoring {

	public static PropertyEntity upgradeEntityEngineMonitoring( EngineLoader loader ) throws Exception {
		DBConnection c = loader.getConnection();
		PropertyEntity entity = PropertyEntity.getAppPropsEntity( DBEnumObjectType.ROOT , DBEnumParamEntityType.MONITORING , DBEnumObjectVersionType.CORE );
		return( DBSettings.savedbObjectEntity( c , entity , new EntityVar[] { 
				EntityVar.metaBoolean( EngineMonitoring.PROPERTY_ENABLED , "Instance Monitoring Enabled" , true , false ) ,
				EntityVar.metaString( EngineMonitoring.PROPERTY_RESOURCE_URL , "Monitoring Resources URL" , true , getProductPath( EngineContext.PROPERTY_MON_RESURL ) ) ,
				EntityVar.metaPathAbsolute( EngineMonitoring.PROPERTY_RESOURCE_PATH , "Monitoring Resources Path" , true , getProductPath( EngineContext.PROPERTY_MON_RESPATH ) ) ,
				EntityVar.metaPathAbsolute( EngineMonitoring.PROPERTY_DIR_DATA , "Monitoring Database Path" , true , getProductPath( EngineContext.PROPERTY_MON_DATAPATH ) ) ,
				EntityVar.metaPathAbsolute( EngineMonitoring.PROPERTY_DIR_REPORTS , "Monitoring Reports Path" , true , getProductPath( EngineContext.PROPERTY_MON_REPORTPATH ) ) ,
				EntityVar.metaPathAbsolute( EngineMonitoring.PROPERTY_DIR_LOGS , "Monitoring Logs" , true , getProductPath( EngineContext.PROPERTY_MON_LOGPATH ) )
		} ) );
	}

	public static PropertyEntity loaddbEntityEngineMonitoring( EngineLoader loader ) throws Exception {
		PropertyEntity entity = PropertyEntity.getAppPropsEntity( DBEnumObjectType.ROOT , DBEnumParamEntityType.MONITORING , DBEnumObjectVersionType.CORE );
		DBSettings.loaddbEntity( loader , entity , DBVersions.APP_ID );
		return( entity );
	}
	
	private static String getProductPath( String var ) {
		return( EntityVar.p( var ) + "/" + EntityVar.p( MetaProductCoreSettings.PROPERTY_PRODUCT_NAME ) );
	}
	
	public static void importxml( EngineLoader loader , EngineMonitoring mon , Node root ) throws Exception {
		DBConnection c = loader.getConnection();
		EngineSettings settings = loader.getSettings();
		EngineEntities entities = loader.getEntities();
		ObjectProperties properties = entities.createEngineMonitoringProps( settings.getEngineProperties() );
		
		int version = c.getNextCoreVersion();
		DBSettings.importxml( loader , root , properties , DBVersions.CORE_ID , DBVersions.CORE_ID , true , version );
		mon.setProperties( properties );
	}
	
	public static void savexml( EngineLoader loader , EngineMonitoring mon , Document doc , Element root ) throws Exception {
		ObjectProperties properties = mon.properties;
		DBSettings.exportxml( loader , doc , root , properties , true );
	}

	public static void loaddb( EngineLoader loader , EngineMonitoring mon ) throws Exception {
		EngineSettings settings = loader.getSettings();
		EngineEntities entities = loader.getEntities();
		ObjectProperties properties = entities.createEngineMonitoringProps( settings.getEngineProperties() );
		DBSettings.loaddbValues( loader , DBVersions.CORE_ID , properties , true );
		mon.setProperties( properties );
	}
	
}
