package org.urm.db.engine;

import org.urm.common.RunContext;
import org.urm.db.DBConnection;
import org.urm.db.core.DBEnums.*;
import org.urm.db.core.DBSettings;
import org.urm.db.core.DBVersions;
import org.urm.engine.data.EngineContext;
import org.urm.engine.properties.EntityVar;
import org.urm.engine.properties.PropertyEntity;

public abstract class DBEngineContext {

	public static PropertyEntity makeEntityRC( DBConnection c , boolean upgrade ) throws Exception {
		PropertyEntity entity = PropertyEntity.getAppPropsEntity( DBEnumObjectType.ROOT , DBEnumParamEntityType.RC , DBEnumObjectVersionType.CORE );
		if( !upgrade ) {
			DBSettings.loaddbAppEntity( c , entity );
			return( entity );
		}
		
		return( DBSettings.savedbObjectEntity( c , entity , new EntityVar[] { 
				EntityVar.metaString( RunContext.PROPERTY_HOSTNAME , "Server Host" , true , null ) ,
				EntityVar.metaPathAbsolute( RunContext.PROPERTY_USER_HOME , "Server User Home" , false , null , null ) ,
				EntityVar.metaString( RunContext.PROPERTY_OS_TYPE , "Server Operating System" , true , null ) ,
				EntityVar.metaPathAbsolute( RunContext.PROPERTY_INSTALL_PATH , "Server Install Path" , true , null , null ) ,
				EntityVar.metaPathAbsolute( RunContext.PROPERTY_WORK_PATH , "Server Work Path" , false , null , null ) ,
				EntityVar.metaPathAbsolute( RunContext.PROPERTY_AUTH_PATH , "Authorization Data Path" , false , null , null ) ,
				EntityVar.metaPathAbsolute( RunContext.PROPERTY_DB_PATH , "Database Properties Path" , false , null , null ) ,
				EntityVar.metaPathAbsolute( RunContext.PROPERTY_SERVER_CONFPATH , "Server Configuration Path" , false , null , null ) ,
				EntityVar.metaPathAbsolute( RunContext.PROPERTY_SERVER_PRODUCTSPATH , "Products Home Path" , false , null , null ) ,
		} ) );
	}

	public static PropertyEntity makeEntityEngine( DBConnection c , boolean upgrade ) throws Exception {
		PropertyEntity entity = PropertyEntity.getAppPropsEntity( DBEnumObjectType.ROOT , DBEnumParamEntityType.ENGINE , DBEnumObjectVersionType.CORE );
		if( !upgrade ) {
			DBSettings.loaddbAppEntity( c , entity );
			return( entity );
		}
		
		return( DBSettings.savedbObjectEntity( c , entity , new EntityVar[] { 
				EntityVar.metaInteger( EngineContext.PROPERTY_CONNECTION_JMX_PORT , "Server Engine JMX Port" , true , 6000 ) ,
				EntityVar.metaInteger( EngineContext.PROPERTY_CONNECTION_JMXWEB_PORT , "Server Engine JMX HTTP Port" , true , 6001 ) ,
				EntityVar.metaPathAbsolute( EngineContext.PROPERTY_DIST_ROOT , "Central Distributive Repository Path" , true , EntityVar.p( RunContext.PROPERTY_INSTALL_PATH ) + "/dist" , null ) ,
				EntityVar.metaPathRelative( EngineContext.PROPERTY_DIST_APPFOLDER , "Central Application Folder" , true , "systems" , null ) ,
				EntityVar.metaPathAbsolute( EngineContext.PROPERTY_DIST_PLATFORMPATH , "Central Platform Software Metadata Path" , true , "platform" , null ) ,
				EntityVar.metaPathAbsolute( EngineContext.PROPERTY_SECURE_CONFPATH , "Secured Data Path" , true , "secured" , null ) ,
				EntityVar.metaPathAbsolute( EngineContext.PROPERTY_WORK_ARTEFACTS , "Build Artefacts Location" , true , EntityVar.p( RunContext.PROPERTY_INSTALL_PATH ) + "/artefacts" , null ) ,
				EntityVar.metaPathAbsolute( EngineContext.PROPERTY_WORK_MIRRORPATH , "Repository Working Copies" , true , EntityVar.p( RunContext.PROPERTY_INSTALL_PATH ) + "/mirror" , null ) ,
				EntityVar.metaPathAbsolute( EngineContext.PROPERTY_WORK_BUILDLOGS , "Location of Build Logs" , true , EntityVar.p( RunContext.PROPERTY_INSTALL_PATH ) + "/logs/build" , null ) ,
				EntityVar.metaPathAbsolute( EngineContext.PROPERTY_WORK_DEPLOYLOGS , "Location of Deployment Logs" , true , EntityVar.p( RunContext.PROPERTY_INSTALL_PATH ) + "/logs/deploy" , null ) , 
				EntityVar.metaPathAbsolute( EngineContext.PROPERTY_STAGING_LINUXPATH , "Linux Staging Directory" , true , "/redist" , DBEnumOSType.LINUX ) ,
				EntityVar.metaPathAbsolute( EngineContext.PROPERTY_STAGING_WINPATH , "Windows Staging Directory" , true , "C:/redist" , DBEnumOSType.WINDOWS ) , 
				EntityVar.metaPathAbsolute( EngineContext.PROPERTY_MON_RESPATH , "Monitoring Resources Path" , true , EntityVar.p( RunContext.PROPERTY_INSTALL_PATH ) + "/monitoring/resources" , null ) , 
				EntityVar.metaString( EngineContext.PROPERTY_MON_RESURL , "Monitoring Resources URL" , false , null ) ,
				EntityVar.metaPathAbsolute( EngineContext.PROPERTY_MON_DATAPATH , "Monitoring Database Path" , true , EntityVar.p( RunContext.PROPERTY_INSTALL_PATH ) + "/monitoring/data" , null )  ,
				EntityVar.metaPathAbsolute( EngineContext.PROPERTY_MON_REPORTPATH , "Monitoring Reports Path" , true , EntityVar.p( RunContext.PROPERTY_INSTALL_PATH ) + "/monitoring/reports" , null ) , 
				EntityVar.metaPathAbsolute( EngineContext.PROPERTY_MON_LOGPATH , "Monitoring Logs" , true , EntityVar.p( RunContext.PROPERTY_INSTALL_PATH ) + "/logs/monitoring" , null ) , 
				EntityVar.metaInteger( EngineContext.PROPERTY_SHELL_HOUSEKEEP_TIME , "Housekeeping Interval (ms)" , true , 30000 ) ,
				EntityVar.metaInteger( EngineContext.PROPERTY_SHELL_SILENTMAX , "Silent Max (ms)" , true , 60000 ) ,
				EntityVar.metaInteger( EngineContext.PROPERTY_SHELL_UNAVAILABLE_SKIPTIME , "Ignore Unavailable (ms)" , true , 30000 ) ,
				EntityVar.metaBoolean( EngineContext.PROPERTY_CHAT_USING , "Using Notifications" , true , false ) ,
				EntityVar.metaEnum( EngineContext.PROPERTY_CHAT_TYPE , "Chat Type" , false , DBEnumChatType.UNKNOWN ) ,
				EntityVar.metaObject( EngineContext.PROPERTY_CHAT_JABBER_RESOURCE , "Jabber Server Resource" , DBEnumObjectType.RESOURCE , false ) ,
				EntityVar.metaString( EngineContext.PROPERTY_CHAT_JABBER_CONFERENCESERVER , "Jabber Conference Server" , false , null ) ,
				EntityVar.metaObject( EngineContext.PROPERTY_CHAT_ROCKET_RESOURCE , "Rocket Server Resource" , DBEnumObjectType.RESOURCE , false )
		} ) );
	}

	public static PropertyEntity makeEntityCustomRC( DBConnection c , boolean upgrade ) throws Exception {
		PropertyEntity entity = PropertyEntity.getCustomEntity( DBVersions.LOCAL_ID , DBEnumObjectType.ROOT , DBEnumParamEntityType.RC_CUSTOM , DBVersions.LOCAL_ID , DBEnumObjectVersionType.LOCAL );
		if( !upgrade ) {
			DBSettings.loaddbCustomEntity( c , entity );
			return( entity );
		}
		
		return( DBSettings.savedbObjectEntity( c , entity , new EntityVar[0] ) ); 
	}

	public static PropertyEntity makeEntityCustomEngine( DBConnection c , boolean upgrade ) throws Exception {
		PropertyEntity entity = PropertyEntity.getCustomEntity( DBVersions.CORE_ID , DBEnumObjectType.ROOT , DBEnumParamEntityType.ENGINE_CUSTOM , DBVersions.CORE_ID , DBEnumObjectVersionType.CORE );
		if( !upgrade ) {
			DBSettings.loaddbCustomEntity( c , entity );
			return( entity );
		}
		
		return( DBSettings.savedbObjectEntity( c , entity , new EntityVar[0] ) ); 
	}

}
