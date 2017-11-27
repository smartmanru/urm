package org.urm.db.engine;

import org.urm.db.DBConnection;
import org.urm.db.core.DBSettings;
import org.urm.db.core.DBVersions;
import org.urm.db.core.DBEnums.DBEnumObjectType;
import org.urm.db.core.DBEnums.DBEnumObjectVersionType;
import org.urm.db.core.DBEnums.DBEnumParamEntityType;
import org.urm.engine.properties.EntityVar;
import org.urm.engine.properties.PropertyEntity;
import org.urm.meta.EngineLoader;
import org.urm.meta.engine.EngineContext;
import org.urm.meta.engine.EngineMonitoring;
import org.urm.meta.product.MetaProductCoreSettings;

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
	
}
