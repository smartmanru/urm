package org.urm.db.engine;

import org.urm.common.RunContext;
import org.urm.db.DBConnection;
import org.urm.db.core.DBEnums.DBEnumObjectVersionType;
import org.urm.db.core.DBEnums.DBEnumParamEntityType;
import org.urm.db.core.DBSettings;
import org.urm.db.core.DBVersions;
import org.urm.engine.EngineDB;
import org.urm.engine.properties.EntityVar;
import org.urm.engine.properties.PropertyEntity;

public abstract class DBEngineContext {

	public static PropertyEntity upgradeEntityRC( DBConnection c ) throws Exception {
		return( DBSettings.savedbEntity( c , DBEnumObjectVersionType.APP , DBVersions.APP_ID , DBEnumParamEntityType.RC , false , EngineDB.APP_VERSION , new EntityVar[] { 
				EntityVar.metaString( RunContext.PROPERTY_HOSTNAME , "Server Host" , true , null ) ,
				EntityVar.metaPathAbsolute( RunContext.PROPERTY_USER_HOME , "Server User Home" , false , null ) ,
				EntityVar.metaString( RunContext.PROPERTY_OS_TYPE , "Server Operating System" , true , null ) ,
				EntityVar.metaPathAbsolute( RunContext.PROPERTY_INSTALL_PATH , "Server Install Path" , true , null ) ,
				EntityVar.metaPathAbsolute( RunContext.PROPERTY_SERVER_CONFPATH , "Server Configuration Path" , false , null ) ,
				EntityVar.metaPathAbsolute( RunContext.PROPERTY_SERVER_PRODUCTSPATH , "Products Home Path" , false , null ) ,
		} ) );
	}

}
