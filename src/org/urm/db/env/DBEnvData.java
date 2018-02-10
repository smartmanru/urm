package org.urm.db.env;

import org.urm.common.Common;
import org.urm.db.DBConnection;
import org.urm.db.DBQueries;
import org.urm.db.EngineDB;
import org.urm.db.core.DBEnums.*;
import org.urm.db.core.DBSettings;
import org.urm.engine.properties.EntityVar;
import org.urm.engine.properties.PropertyEntity;
import org.urm.meta.EngineLoader;
import org.urm.meta.env.MetaEnv;
import org.urm.meta.env.MetaEnvSegment;
import org.urm.meta.env.MetaEnvServer;
import org.urm.meta.env.MetaEnvServerDeployment;
import org.urm.meta.env.MetaEnvServerNode;
import org.urm.meta.env.MetaEnvStartGroup;
import org.urm.meta.env.MetaMonitoringItem;
import org.urm.meta.env.MetaMonitoringTarget;
import org.urm.meta.product.ProductMeta;

public class DBEnvData {

	public static String TABLE_ENV = "urm_env";
	public static String TABLE_SEGMENT = "urm_env_segment";
	public static String TABLE_SERVER = "urm_env_server";
	public static String TABLE_NODE = "urm_env_node";
	public static String TABLE_STARTGROUP = "urm_env_startgroup";
	public static String TABLE_DEPLOYMENT = "urm_env_deployment";
	public static String TABLE_MONTARGET = "urm_env_montarget";
	public static String TABLE_MONITEM = "urm_env_monitem";
	public static String FIELD_ENV_ID = "env_id";
	public static String FIELD_ENV_META_ID = "meta_fkid";
	public static String FIELD_ENV_META_NAME = "meta_fkname";
	public static String FIELD_ENV_MATCHED = "matched";
	public static String FIELD_ENV_DESC = "xdesc";
	public static String FIELD_ENV_ENVTYPE = "env_type";
	public static String FIELD_ENV_BASELINE_ID = "baseline_env_fkid";
	public static String FIELD_ENV_BASELINE_NAME = "baseline_env_fkname";
	public static String FIELD_ENV_ENVKEY_ID = "envkey_resource_fkid";
	public static String FIELD_ENV_ENVKEY_NAME = "envkey_resource_fkname";
	public static String FIELD_ENV_REMOTE = "distr_remote";
	public static String FIELD_ENV_REMOTE_ACCOUNT_ID = "distr_account_fkid";
	public static String FIELD_ENV_REMOTE_ACCOUNT_NAME = "distr_account_fkname";
	public static String FIELD_ENV_REMOTE_PATH = "distr_path";
	public static String FIELD_SEGMENT_ID = "segment_id";
	public static String FIELD_SEGMENT_ENV_ID = "env_id";
	public static String FIELD_SEGMENT_DESC = "xdesc";
	public static String FIELD_SEGMENT_BASELINE_ID = "baseline_segment_fkid";
	public static String FIELD_SEGMENT_BASELINE_NAME = "baseline_segment_fkname";
	public static String FIELD_SEGMENT_DATACENTER_ID = "datacenter_fkid";
	public static String FIELD_SEGMENT_DATACENTER_NAME = "datacenter_fkname";
	public static String FIELD_SERVER_ID = "server_id";
	public static String FIELD_SERVER_ENV_ID = "env_id";
	public static String FIELD_SERVER_SEGMENT_ID = "segment_id";
	public static String FIELD_SERVER_DESC = "xdesc";
	public static String FIELD_SERVER_RUNTYPE = "serverrun_type";
	public static String FIELD_SERVER_ACCESSTYPE = "serveraccess_type";
	public static String FIELD_SERVER_OSTYPE = "os_type";
	public static String FIELD_SERVER_BASELINE_ID = "baseline_server_fkid";
	public static String FIELD_SERVER_BASELINE_NAME = "baseline_server_fkname";
	public static String FIELD_SERVER_DBMSTYPE = "dbms_type";
	public static String FIELD_SERVER_ADMSCHEMA_ID = "db_admschema_fkid";
	public static String FIELD_SERVER_ADMSCHEMA_NAME = "db_admschema_fkname";
	public static String FIELD_SERVER_BASEITEM_ID = "baseitem_fkid";
	public static String FIELD_SERVER_BASEITEM_NAME = "baseitem_fkname";
	public static String FIELD_NODE_ID = "node_id";
	public static String FIELD_NODE_ENV_ID = "env_id";
	public static String FIELD_NODE_SERVER_ID = "server_id";
	public static String FIELD_NODE_TYPE = "node_type";
	public static String FIELD_NODE_ACCOUNT_ID = "account_fkid";
	public static String FIELD_NODE_ACCOUNT_NAME = "account_fkname";
	public static String FIELD_STARTGROUP_ID = "startgroup_id";
	public static String FIELD_STARTGROUP_ENV_ID = "env_id";
	public static String FIELD_STARTGROUP_SEGMENT_ID = "segment_id";
	public static String FIELD_STARTGROUP_DESC = "xdesc";
	public static String FIELD_DEPLOYMENT_ID = "deployment_id";
	public static String FIELD_DEPLOYMENT_ENV_ID = "env_id";
	public static String FIELD_DEPLOYMENT_SERVER_ID = "server_id";
	public static String FIELD_DEPLOYMENT_TYPE = "serverdeployment_type";
	public static String FIELD_DEPLOYMENT_COMP_ID = "comp_fkid";
	public static String FIELD_DEPLOYMENT_COMP_NAME = "comp_fkname";
	public static String FIELD_DEPLOYMENT_BINARY_ID = "binaryitem_fkid";
	public static String FIELD_DEPLOYMENT_BINARY_NAME = "binaryitem_fkname";
	public static String FIELD_DEPLOYMENT_CONF_ID = "confitem_fkid";
	public static String FIELD_DEPLOYMENT_CONF_NAME = "confitem_fkname";
	public static String FIELD_DEPLOYMENT_SCHEMA_ID = "schema_fkid";
	public static String FIELD_DEPLOYMENT_SCHEMA_NAME = "schema_fkname";
	public static String FIELD_DEPLOYMENT_DEPLOYMODE = "deploymode_type";
	public static String FIELD_DEPLOYMENT_NODETYPE = "node_type";
	public static String FIELD_MONTARGET_ID = "montarget_id";
	public static String FIELD_MONTARGET_ENV_ID = "env_id";
	public static String FIELD_MONTARGET_SEGMENT_ID = "segment_id";
	public static String FIELD_MONTARGET_MAJOR_ENABLED = "major_enabled";
	public static String FIELD_MONTARGET_MAJOR_SCHEDULE = "major_schedule";
	public static String FIELD_MONTARGET_MAJOR_MAXTIME = "major_maxtime";
	public static String FIELD_MONTARGET_MINOR_ENABLED = "minor_enabled";
	public static String FIELD_MONTARGET_MINOR_SCHEDULE = "minor_schedule";
	public static String FIELD_MONTARGET_MINOR_MAXTIME = "minor_maxtime";
	public static String FIELD_MONITEM_ID = "monitem_id";
	public static String FIELD_MONITEM_ENV_ID = "env_id";
	public static String FIELD_MONITEM_TARGET_ID = "monitem_id";
	public static String FIELD_MONITEM_TYPE = "monitem_type";

	public static PropertyEntity upgradeEntityEnvPrimary( EngineLoader loader ) throws Exception {
		DBConnection c = loader.getConnection();
		PropertyEntity entity = PropertyEntity.getAppObjectEntity( DBEnumObjectType.ENVIRONMENT , DBEnumParamEntityType.ENV_PRIMARY , DBEnumObjectVersionType.ENVIRONMENT , TABLE_ENV , FIELD_ENV_ID );
		return( DBSettings.savedbObjectEntity( c , entity , new EntityVar[] { 
				EntityVar.metaObjectDatabaseOnly( FIELD_ENV_META_ID , "product meta id" , DBEnumObjectType.META , false ) ,
				EntityVar.metaStringDatabaseOnly( FIELD_ENV_META_NAME , "product meta name" , false , null ) ,
				EntityVar.metaBooleanDatabaseOnly( FIELD_ENV_MATCHED , "environment match status" , false , false ) ,
				EntityVar.metaString( MetaEnv.PROPERTY_NAME , "name" , true , null ) ,
				EntityVar.metaStringVar( MetaEnv.PROPERTY_DESC , FIELD_ENV_DESC , MetaEnv.PROPERTY_DESC , "Description" , false , null ) ,
				EntityVar.metaEnumVar( MetaEnv.PROPERTY_ENVTYPE , FIELD_ENV_ENVTYPE , MetaEnv.PROPERTY_ENVTYPE , "Environment type" , true , DBEnumEnvType.UNKNOWN ) ,
				EntityVar.metaObjectDatabaseOnly( FIELD_ENV_BASELINE_ID , "baseline environment id" , DBEnumObjectType.ENVIRONMENT , false ) ,
				EntityVar.metaStringVar( MetaEnv.PROPERTY_BASELINE , FIELD_ENV_BASELINE_NAME , MetaEnv.PROPERTY_BASELINE , "baseline environment name" , false , null ) ,
				EntityVar.metaBoolean( MetaEnv.PROPERTY_OFFLINE , "Offline" , true , false ) ,
				EntityVar.metaObjectDatabaseOnly( FIELD_ENV_ENVKEY_ID , "environment key resource id" , DBEnumObjectType.RESOURCE , false ) ,
				EntityVar.metaStringVar( MetaEnv.PROPERTY_ENVKEY , FIELD_ENV_ENVKEY_NAME , MetaEnv.PROPERTY_ENVKEY , "environment key resource name" , false , null ) ,
				EntityVar.metaBooleanVar( MetaEnv.PROPERTY_DISTR_REMOTE , FIELD_ENV_REMOTE , MetaEnv.PROPERTY_DISTR_REMOTE , "remote distributive" , false , false ) ,
				EntityVar.metaObjectDatabaseOnly( FIELD_ENV_REMOTE_ACCOUNT_ID , "remote distributive account id" , DBEnumObjectType.HOSTACCOUNT , false ) ,
				EntityVar.metaStringVar( MetaEnv.PROPERTY_DISTR_HOSTLOGIN , FIELD_ENV_REMOTE_ACCOUNT_NAME , MetaEnv.PROPERTY_DISTR_HOSTLOGIN , "remote distributive account name" , false , null ) ,
				EntityVar.metaStringVar( MetaEnv.PROPERTY_DISTR_PATH , FIELD_ENV_REMOTE_PATH , MetaEnv.PROPERTY_DISTR_PATH , "remote distributive path" , false , null ) ,
		} ) );
	}

	public static PropertyEntity loaddbEntityEnvPrimary( DBConnection c ) throws Exception {
		PropertyEntity entity = PropertyEntity.getAppObjectEntity( DBEnumObjectType.ENVIRONMENT , DBEnumParamEntityType.ENV_PRIMARY , DBEnumObjectVersionType.ENVIRONMENT , TABLE_ENV , FIELD_ENV_ID );
		DBSettings.loaddbAppEntity( c , entity );
		return( entity );
	}
	
	public static PropertyEntity upgradeEntityEnvExtra( EngineLoader loader ) throws Exception {
		DBConnection c = loader.getConnection();
		PropertyEntity entity = PropertyEntity.getAppPropsEntity( DBEnumObjectType.ENVIRONMENT , DBEnumParamEntityType.ENV_EXTRA , DBEnumObjectVersionType.ENVIRONMENT );
		return( DBSettings.savedbObjectEntity( c , entity , new EntityVar[] { 
				EntityVar.metaBoolean( MetaEnv.PROPERTY_DB_AUTH , "secured database authentification" , false , false ) ,
				EntityVar.metaBoolean( MetaEnv.PROPERTY_SHOWONLY , "showonly mode" , false , false ) ,
				EntityVar.metaBoolean( MetaEnv.PROPERTY_BACKUP , "backup mode" , false , false ) ,
				EntityVar.metaBoolean( MetaEnv.PROPERTY_CONF_DEPLOY , "configuration deploy" , false , false ) ,
				EntityVar.metaBoolean( MetaEnv.PROPERTY_CONF_KEEPALIVE , "keep live configuration up-to-date" , false , false ) ,
				EntityVar.metaString( MetaEnv.PROPERTY_DB_AUTHFILE , "database authentification file" , false , null ) ,
				EntityVar.metaString( MetaEnv.PROPERTY_CHATROOM , "environment chatroom" , false , null ) ,
				EntityVar.metaString( MetaEnv.PROPERTY_REDISTWIN_PATH , "use specific redist path for windows" , false , null ) ,
				EntityVar.metaString( MetaEnv.PROPERTY_REDISTLINUX_PATH , "use specific redist path for linux" , false , null )
		} ) );
	}

	public static PropertyEntity loaddbEntityEnvExtra( DBConnection c ) throws Exception {
		PropertyEntity entity = PropertyEntity.getAppPropsEntity( DBEnumObjectType.ENVIRONMENT , DBEnumParamEntityType.ENV_EXTRA , DBEnumObjectVersionType.ENVIRONMENT );
		DBSettings.loaddbAppEntity( c , entity );
		return( entity );
	}
	
	public static PropertyEntity upgradeEntitySegmentPrimary( EngineLoader loader ) throws Exception {
		DBConnection c = loader.getConnection();
		PropertyEntity entity = PropertyEntity.getAppObjectEntity( DBEnumObjectType.ENVIRONMENT_SEGMENT , DBEnumParamEntityType.ENV_SEGMENT_PRIMARY , DBEnumObjectVersionType.ENVIRONMENT , TABLE_SEGMENT , FIELD_SEGMENT_ID );
		return( DBSettings.savedbObjectEntity( c , entity , new EntityVar[] { 
				EntityVar.metaObjectDatabaseOnly( FIELD_SEGMENT_ENV_ID , "environment id" , DBEnumObjectType.ENVIRONMENT , false ) ,
				EntityVar.metaString( MetaEnvSegment.PROPERTY_NAME , "name" , true , null ) ,
				EntityVar.metaStringVar( MetaEnvSegment.PROPERTY_DESC , FIELD_SEGMENT_DESC , MetaEnvSegment.PROPERTY_DESC , "Description" , false , null ) ,
				EntityVar.metaObjectDatabaseOnly( FIELD_SEGMENT_BASELINE_ID , "baseline segment id" , DBEnumObjectType.ENVIRONMENT_SEGMENT , false ) ,
				EntityVar.metaStringVar( MetaEnvSegment.PROPERTY_BASELINE , FIELD_SEGMENT_BASELINE_NAME , MetaEnvSegment.PROPERTY_BASELINE , "baseline segment name" , false , null ) ,
				EntityVar.metaBoolean( MetaEnvSegment.PROPERTY_OFFLINE , "Offline" , true , false ) ,
				EntityVar.metaObjectDatabaseOnly( FIELD_SEGMENT_DATACENTER_ID , "segment datacenter id" , DBEnumObjectType.DATACENTER , false ) ,
				EntityVar.metaStringVar( MetaEnvSegment.PROPERTY_DC , FIELD_SEGMENT_DATACENTER_NAME , MetaEnvSegment.PROPERTY_DC , "segment datacenter resource name" , false , null ) ,
		} ) );
	}

	public static PropertyEntity loaddbEntitySegmentPrimary( DBConnection c ) throws Exception {
		PropertyEntity entity = PropertyEntity.getAppObjectEntity( DBEnumObjectType.ENVIRONMENT_SEGMENT , DBEnumParamEntityType.ENV_SEGMENT_PRIMARY , DBEnumObjectVersionType.ENVIRONMENT , TABLE_SEGMENT , FIELD_SEGMENT_ID );
		DBSettings.loaddbAppEntity( c , entity );
		return( entity );
	}
	
	public static PropertyEntity upgradeEntityServerPrimary( EngineLoader loader ) throws Exception {
		DBConnection c = loader.getConnection();
		PropertyEntity entity = PropertyEntity.getAppObjectEntity( DBEnumObjectType.ENVIRONMENT_SERVER , DBEnumParamEntityType.ENV_SERVER_PRIMARY , DBEnumObjectVersionType.ENVIRONMENT , TABLE_SERVER , FIELD_SERVER_ID );
		return( DBSettings.savedbObjectEntity( c , entity , new EntityVar[] { 
				EntityVar.metaObjectDatabaseOnly( FIELD_SERVER_ENV_ID , "environment id" , DBEnumObjectType.ENVIRONMENT , false ) ,
				EntityVar.metaObjectDatabaseOnly( FIELD_SERVER_SEGMENT_ID , "environment segment id" , DBEnumObjectType.ENVIRONMENT_SEGMENT , false ) ,
				EntityVar.metaString( MetaEnvServer.PROPERTY_NAME , "name" , true , null ) ,
				EntityVar.metaStringVar( MetaEnvServer.PROPERTY_DESC , FIELD_SERVER_DESC , MetaEnvServer.PROPERTY_DESC , "Description" , false , null ) ,
				EntityVar.metaEnumVar( MetaEnvServer.PROPERTY_SERVERRUNTYPE , FIELD_SERVER_RUNTYPE , MetaEnvServer.PROPERTY_SERVERRUNTYPE , "Run type" , true , DBEnumServerRunType.UNKNOWN ) ,
				EntityVar.metaEnumVar( MetaEnvServer.PROPERTY_SERVERACCESSTYPE , FIELD_SERVER_ACCESSTYPE , MetaEnvServer.PROPERTY_SERVERACCESSTYPE , "Access type" , true , DBEnumServerAccessType.UNKNOWN ) ,
				EntityVar.metaEnumVar( MetaEnvServer.PROPERTY_OSTYPE , FIELD_SERVER_OSTYPE , MetaEnvServer.PROPERTY_OSTYPE , "Operating system type" , true , DBEnumOSType.UNKNOWN ) ,
				EntityVar.metaString( MetaEnvServer.PROPERTY_SYSNAME , "system name" , false , null ) ,
				EntityVar.metaObjectDatabaseOnly( FIELD_SERVER_BASELINE_ID , "baseline server id" , DBEnumObjectType.ENVIRONMENT_SERVER , false ) ,
				EntityVar.metaStringVar( MetaEnvServer.PROPERTY_BASELINE , FIELD_SERVER_BASELINE_NAME , MetaEnvServer.PROPERTY_BASELINE , "baseline server name" , false , null ) ,
				EntityVar.metaBoolean( MetaEnvServer.PROPERTY_OFFLINE , "Offline" , true , false ) ,
				EntityVar.metaEnumVar( MetaEnvServer.PROPERTY_DBMSTYPE , FIELD_SERVER_DBMSTYPE , MetaEnvServer.PROPERTY_DBMSTYPE , "Database system type" , true , DBEnumDbmsType.UNKNOWN ) ,
				EntityVar.metaObjectDatabaseOnly( FIELD_SERVER_ADMSCHEMA_ID , "administrative database schema id" , DBEnumObjectType.DBSCHEMA , false ) ,
				EntityVar.metaStringVar( MetaEnvServer.PROPERTY_ADMSCHEMA , FIELD_SERVER_ADMSCHEMA_NAME , MetaEnvServer.PROPERTY_ADMSCHEMA , "administrative database schema name" , false , null ) ,
				EntityVar.metaObjectDatabaseOnly( FIELD_SERVER_BASEITEM_ID , "base software item id" , DBEnumObjectType.BASE_ITEM , false ) ,
				EntityVar.metaStringVar( MetaEnvServer.PROPERTY_BASEITEM , FIELD_SERVER_BASEITEM_NAME , MetaEnvServer.PROPERTY_BASEITEM , "base software item name" , false , null ) ,
		} ) );
	}

	public static PropertyEntity loaddbEntityServerPrimary( DBConnection c ) throws Exception {
		PropertyEntity entity = PropertyEntity.getAppObjectEntity( DBEnumObjectType.ENVIRONMENT_SERVER , DBEnumParamEntityType.ENV_SERVER_PRIMARY , DBEnumObjectVersionType.ENVIRONMENT , TABLE_SERVER , FIELD_SERVER_ID );
		DBSettings.loaddbAppEntity( c , entity );
		return( entity );
	}
	
	public static PropertyEntity upgradeEntityServerExtra( EngineLoader loader ) throws Exception {
		DBConnection c = loader.getConnection();
		PropertyEntity entity = PropertyEntity.getAppPropsEntity( DBEnumObjectType.ENVIRONMENT_SERVER , DBEnumParamEntityType.ENV_SERVER_EXTRA , DBEnumObjectVersionType.ENVIRONMENT );
		return( DBSettings.savedbObjectEntity( c , entity , new EntityVar[] { 
				EntityVar.metaString( MetaEnvServer.PROPERTY_XDOC , "design identifier" , false , null ) ,
				EntityVar.metaString( MetaEnvServer.PROPERTY_ROOTPATH , "environment chatroom" , false , null ) ,
				EntityVar.metaString( MetaEnvServer.PROPERTY_BINPATH , "use specific redist path for windows" , false , null ) ,
				EntityVar.metaInteger( MetaEnvServer.PROPERTY_PORT , "use specific redist path for linux" , false , null ) ,
				EntityVar.metaInteger( MetaEnvServer.PROPERTY_STARTTIME , "use specific redist path for linux" , false , 60 ) ,
				EntityVar.metaInteger( MetaEnvServer.PROPERTY_STOPTIME , "use specific redist path for linux" , false , 60 ) ,
				EntityVar.metaString( MetaEnvServer.PROPERTY_DEPLOYPATH , "use specific redist path for windows" , false , null ) ,
				EntityVar.metaString( MetaEnvServer.PROPERTY_LINKFROMPATH , "use specific redist path for windows" , false , null ) ,
				EntityVar.metaString( MetaEnvServer.PROPERTY_DEPLOYSCRIPT , "use specific redist path for windows" , false , null ) ,
				EntityVar.metaString( MetaEnvServer.PROPERTY_HOTDEPLOYPATH , "use specific redist path for windows" , false , null ) ,
				EntityVar.metaString( MetaEnvServer.PROPERTY_HOTDEPLOYDATA , "use specific redist path for windows" , false , null ) ,
				EntityVar.metaString( MetaEnvServer.PROPERTY_WEBSERVICEURL , "use specific redist path for windows" , false , null ) ,
				EntityVar.metaString( MetaEnvServer.PROPERTY_WEBMAINURL , "use specific redist path for windows" , false , null ) ,
				EntityVar.metaString( MetaEnvServer.PROPERTY_LOGPATH , "use specific redist path for windows" , false , null ) ,
				EntityVar.metaString( MetaEnvServer.PROPERTY_LOGFILEPATH , "use specific redist path for windows" , false , null ) ,
				EntityVar.metaBoolean( MetaEnvServer.PROPERTY_NOPIDS , "Offline" , true , false ) ,
				EntityVar.metaString( MetaEnvServer.PROPERTY_DBMSADDR , "use specific redist path for windows" , false , null ) ,
				EntityVar.metaString( MetaEnvServer.PROPERTY_ALIGNED , "use specific redist path for windows" , false , null ) ,
				EntityVar.metaString( MetaEnvServer.PROPERTY_REGIONS , "use specific redist path for windows" , false , null )
		} ) );
	}

	public static PropertyEntity loaddbEntityServerExtra( DBConnection c ) throws Exception {
		PropertyEntity entity = PropertyEntity.getAppPropsEntity( DBEnumObjectType.ENVIRONMENT_SERVER , DBEnumParamEntityType.ENV_SERVER_EXTRA , DBEnumObjectVersionType.ENVIRONMENT );
		DBSettings.loaddbAppEntity( c , entity );
		return( entity );
	}
	
	public static PropertyEntity upgradeEntityNodePrimary( EngineLoader loader ) throws Exception {
		DBConnection c = loader.getConnection();
		PropertyEntity entity = PropertyEntity.getAppObjectEntity( DBEnumObjectType.ENVIRONMENT_NODE , DBEnumParamEntityType.ENV_NODE_PRIMARY , DBEnumObjectVersionType.ENVIRONMENT , TABLE_NODE , FIELD_NODE_ID );
		return( DBSettings.savedbObjectEntity( c , entity , new EntityVar[] { 
				EntityVar.metaObjectDatabaseOnly( FIELD_NODE_ENV_ID , "environment id" , DBEnumObjectType.ENVIRONMENT , true ) ,
				EntityVar.metaObjectDatabaseOnly( FIELD_NODE_SERVER_ID , "environment server id" , DBEnumObjectType.ENVIRONMENT_SERVER , true ) ,
				EntityVar.metaInteger( MetaEnvServerNode.PROPERTY_POS , "position" , true , null ) ,
				EntityVar.metaEnumVar( MetaEnvServerNode.PROPERTY_NODETYPE , FIELD_NODE_TYPE , MetaEnvServerNode.PROPERTY_NODETYPE , "Node type" , true , DBEnumNodeType.UNKNOWN ) ,
				EntityVar.metaObjectDatabaseOnly( FIELD_NODE_ACCOUNT_ID , "infrastructure account id" , DBEnumObjectType.HOSTACCOUNT , false ) ,
				EntityVar.metaStringVar( MetaEnvServerNode.PROPERTY_HOSTLOGIN , FIELD_NODE_ACCOUNT_NAME , MetaEnvServerNode.PROPERTY_HOSTLOGIN , "infrastructure account name" , false , null ) ,
				EntityVar.metaString( MetaEnvServerNode.PROPERTY_DEPLOYGROUP , "deploy group" , false , null ) ,
				EntityVar.metaBoolean( MetaEnvServerNode.PROPERTY_OFFLINE , "Offline" , true , false ) ,
				EntityVar.metaString( MetaEnvServerNode.PROPERTY_DBINSTANCE , "database instance node code" , false , null ) ,
				EntityVar.metaBoolean( MetaEnvServerNode.PROPERTY_DBSTANDBY , "standby node" , false , false ) ,
		} ) );
	}

	public static PropertyEntity loaddbEntityNodePrimary( DBConnection c ) throws Exception {
		PropertyEntity entity = PropertyEntity.getAppObjectEntity( DBEnumObjectType.ENVIRONMENT_NODE , DBEnumParamEntityType.ENV_NODE_PRIMARY , DBEnumObjectVersionType.ENVIRONMENT , TABLE_NODE , FIELD_NODE_ID );
		DBSettings.loaddbAppEntity( c , entity );
		return( entity );
	}
	
	public static PropertyEntity upgradeEntityStartGroup( EngineLoader loader ) throws Exception {
		DBConnection c = loader.getConnection();
		PropertyEntity entity = PropertyEntity.getAppObjectEntity( DBEnumObjectType.ENVIRONMENT_STARTGROUP , DBEnumParamEntityType.ENV_SEGMENT_STARTGROUP , DBEnumObjectVersionType.ENVIRONMENT , TABLE_STARTGROUP , FIELD_STARTGROUP_ID );
		return( DBSettings.savedbObjectEntity( c , entity , new EntityVar[] { 
				EntityVar.metaObjectDatabaseOnly( FIELD_STARTGROUP_ENV_ID , "environment id" , DBEnumObjectType.ENVIRONMENT , true ) ,
				EntityVar.metaObjectDatabaseOnly( FIELD_STARTGROUP_SEGMENT_ID , "segment id" , DBEnumObjectType.ENVIRONMENT_SEGMENT , true ) ,
				EntityVar.metaString( MetaEnvStartGroup.PROPERTY_NAME , "name" , true , null ) ,
				EntityVar.metaStringVar( MetaEnvStartGroup.PROPERTY_DESC , FIELD_SERVER_DESC , MetaEnvStartGroup.PROPERTY_DESC , "Description" , false , null ) ,
				EntityVar.metaStringXmlOnly( MetaEnvStartGroup.PROPERTY_SERVERS , "spece-delimited list of servers" , false , null ) ,
		} ) );
	}

	public static PropertyEntity loaddbEntityStartGroup( DBConnection c ) throws Exception {
		PropertyEntity entity = PropertyEntity.getAppObjectEntity( DBEnumObjectType.ENVIRONMENT_STARTGROUP , DBEnumParamEntityType.ENV_SEGMENT_STARTGROUP , DBEnumObjectVersionType.ENVIRONMENT , TABLE_STARTGROUP , FIELD_STARTGROUP_ID );
		DBSettings.loaddbAppEntity( c , entity );
		return( entity );
	}
	
	public static PropertyEntity upgradeEntityServerDeployment( EngineLoader loader ) throws Exception {
		DBConnection c = loader.getConnection();
		PropertyEntity entity = PropertyEntity.getAppObjectEntity( DBEnumObjectType.ENVIRONMENT_DEPLOYMENT , DBEnumParamEntityType.ENV_DEPLOYMENT , DBEnumObjectVersionType.ENVIRONMENT , TABLE_DEPLOYMENT , FIELD_DEPLOYMENT_ID );
		return( DBSettings.savedbObjectEntity( c , entity , new EntityVar[] { 
				EntityVar.metaObjectDatabaseOnly( FIELD_DEPLOYMENT_ENV_ID , "environment id" , DBEnumObjectType.ENVIRONMENT , true ) ,
				EntityVar.metaObjectDatabaseOnly( FIELD_DEPLOYMENT_SERVER_ID , "environment server id" , DBEnumObjectType.ENVIRONMENT_SERVER , true ) ,
				EntityVar.metaEnumDatabaseOnly( FIELD_DEPLOYMENT_TYPE , "deployment type" , true , DBEnumObjectType.ENVIRONMENT_DEPLOYMENT ) ,
				EntityVar.metaObjectDatabaseOnly( FIELD_DEPLOYMENT_COMP_ID , "component id" , DBEnumObjectType.META_DIST_COMPONENT , false ) ,
				EntityVar.metaStringVar( MetaEnvServerDeployment.PROPERTY_COMPONENT , FIELD_DEPLOYMENT_COMP_NAME , MetaEnvServerDeployment.PROPERTY_COMPONENT , "component name" , false , null ) ,
				EntityVar.metaObjectDatabaseOnly( FIELD_DEPLOYMENT_BINARY_ID , "binary item id" , DBEnumObjectType.META_DIST_BINARYITEM , false ) ,
				EntityVar.metaStringVar( MetaEnvServerDeployment.PROPERTY_DISTITEM , FIELD_DEPLOYMENT_BINARY_NAME , MetaEnvServerDeployment.PROPERTY_DISTITEM , "binary item name" , false , null ) ,
				EntityVar.metaObjectDatabaseOnly( FIELD_DEPLOYMENT_CONF_ID , "conf item id" , DBEnumObjectType.META_DIST_CONFITEM , false ) ,
				EntityVar.metaStringVar( MetaEnvServerDeployment.PROPERTY_CONFITEM , FIELD_DEPLOYMENT_CONF_NAME , MetaEnvServerDeployment.PROPERTY_CONFITEM , "conf item name" , false , null ) ,
				EntityVar.metaObjectDatabaseOnly( FIELD_DEPLOYMENT_SCHEMA_ID , "schema id" , DBEnumObjectType.META_SCHEMA , false ) ,
				EntityVar.metaStringVar( MetaEnvServerDeployment.PROPERTY_SCHEMA , FIELD_DEPLOYMENT_SCHEMA_NAME , MetaEnvServerDeployment.PROPERTY_SCHEMA , "schema name" , false , null ) ,
				EntityVar.metaEnumVar( MetaEnvServerDeployment.PROPERTY_DEPLOYMODE , FIELD_DEPLOYMENT_DEPLOYMODE , MetaEnvServerDeployment.PROPERTY_DEPLOYMODE , "deploy mode" , true , DBEnumDeployModeType.UNKNOWN ) ,
				EntityVar.metaString( MetaEnvServerDeployment.PROPERTY_DEPLOYPATH , "deployment path" , false , null ) ,
				EntityVar.metaString( MetaEnvServerDeployment.PROPERTY_DBNAME , "database schema name" , false , null ) ,
				EntityVar.metaString( MetaEnvServerDeployment.PROPERTY_DBUSER , "database schema user" , false , null ) ,
				EntityVar.metaEnumVar( MetaEnvServerDeployment.PROPERTY_NODETYPE , FIELD_DEPLOYMENT_NODETYPE , MetaEnvServerDeployment.PROPERTY_NODETYPE , "node type to deploy" , true , DBEnumNodeType.UNKNOWN ) ,
		} ) );
	}

	public static PropertyEntity loaddbEntityServerDeployment( DBConnection c ) throws Exception {
		PropertyEntity entity = PropertyEntity.getAppObjectEntity( DBEnumObjectType.ENVIRONMENT_DEPLOYMENT , DBEnumParamEntityType.ENV_DEPLOYMENT , DBEnumObjectVersionType.ENVIRONMENT , TABLE_DEPLOYMENT , FIELD_DEPLOYMENT_ID );
		DBSettings.loaddbAppEntity( c , entity );
		return( entity );
	}
	
	public static PropertyEntity upgradeEntityMonitoringTarget( EngineLoader loader ) throws Exception {
		DBConnection c = loader.getConnection();
		PropertyEntity entity = PropertyEntity.getAppObjectEntity( DBEnumObjectType.ENVIRONMENT_MONTARGET , DBEnumParamEntityType.ENV_SEGMENT_MONTARGET , DBEnumObjectVersionType.ENVIRONMENT , TABLE_MONTARGET , FIELD_MONTARGET_ID );
		return( DBSettings.savedbObjectEntity( c , entity , new EntityVar[] { 
				EntityVar.metaObjectDatabaseOnly( FIELD_MONTARGET_ENV_ID , "environment id" , DBEnumObjectType.ENVIRONMENT , true ) ,
				EntityVar.metaObjectDatabaseOnly( FIELD_MONTARGET_SEGMENT_ID , "environment segment id" , DBEnumObjectType.ENVIRONMENT_SEGMENT , true ) ,
				EntityVar.metaBooleanVar( MetaMonitoringTarget.PROPERTY_MAJOR_ENABLED , FIELD_MONTARGET_MAJOR_ENABLED , MetaMonitoringTarget.PROPERTY_MAJOR_ENABLED , "Enabled major monitoring" , true , false ) ,
				EntityVar.metaStringVar( MetaMonitoringTarget.PROPERTY_MAJOR_SCHEDULE , FIELD_MONTARGET_MAJOR_SCHEDULE , MetaMonitoringTarget.PROPERTY_MAJOR_SCHEDULE , "major schedule" , false , null ) ,
				EntityVar.metaIntegerVar( MetaMonitoringTarget.PROPERTY_MAJOR_MAXTIME , FIELD_MONTARGET_MAJOR_MAXTIME , MetaMonitoringTarget.PROPERTY_MAJOR_MAXTIME , "major max time" , true , 300000 ) ,
				EntityVar.metaBooleanVar( MetaMonitoringTarget.PROPERTY_MINOR_ENABLED , FIELD_MONTARGET_MINOR_ENABLED , MetaMonitoringTarget.PROPERTY_MINOR_ENABLED , "Enabled minor monitoring" , true , false ) ,
				EntityVar.metaStringVar( MetaMonitoringTarget.PROPERTY_MINOR_SCHEDULE , FIELD_MONTARGET_MINOR_SCHEDULE , MetaMonitoringTarget.PROPERTY_MINOR_SCHEDULE , "minor schedule" , false , null ) ,
				EntityVar.metaIntegerVar( MetaMonitoringTarget.PROPERTY_MINOR_MAXTIME , FIELD_MONTARGET_MINOR_MAXTIME , MetaMonitoringTarget.PROPERTY_MINOR_MAXTIME , "minor max time" , true , 300000 ) ,
		} ) );
	}

	public static PropertyEntity loaddbEntityMonitoringTarget( DBConnection c ) throws Exception {
		PropertyEntity entity = PropertyEntity.getAppObjectEntity( DBEnumObjectType.ENVIRONMENT_MONTARGET , DBEnumParamEntityType.ENV_SEGMENT_MONTARGET , DBEnumObjectVersionType.ENVIRONMENT , TABLE_MONTARGET , FIELD_MONTARGET_ID );
		DBSettings.loaddbAppEntity( c , entity );
		return( entity );
	}
	
	public static PropertyEntity upgradeEntityMonitoringItem( EngineLoader loader ) throws Exception {
		DBConnection c = loader.getConnection();
		PropertyEntity entity = PropertyEntity.getAppObjectEntity( DBEnumObjectType.ENVIRONMENT_MONITEM , DBEnumParamEntityType.ENV_SEGMENT_MONITEM , DBEnumObjectVersionType.ENVIRONMENT , TABLE_MONITEM , FIELD_MONITEM_ID );
		return( DBSettings.savedbObjectEntity( c , entity , new EntityVar[] { 
				EntityVar.metaObjectDatabaseOnly( FIELD_MONITEM_ENV_ID , "environment id" , DBEnumObjectType.ENVIRONMENT , true ) ,
				EntityVar.metaObjectDatabaseOnly( FIELD_MONITEM_TARGET_ID , "monitoring target id" , DBEnumObjectType.ENVIRONMENT_MONTARGET , true ) ,
				EntityVar.metaEnumVar( MetaMonitoringItem.PROPERTY_TYPE , FIELD_MONITEM_TYPE , MetaMonitoringItem.PROPERTY_TYPE , "monitoring item type" , true , DBEnumMonItemType.UNKNOWN ) ,
				EntityVar.metaString( MetaMonitoringItem.PROPERTY_URL , "check url" , false , null ) ,
				EntityVar.metaString( MetaMonitoringItem.PROPERTY_WSDATA , "check request" , false , null ) ,
				EntityVar.metaString( MetaMonitoringItem.PROPERTY_WSCHECK , "check request response" , false , null )
		} ) );
	}

	public static PropertyEntity loaddbEntityMonitoringItem( DBConnection c ) throws Exception {
		PropertyEntity entity = PropertyEntity.getAppObjectEntity( DBEnumObjectType.ENVIRONMENT_MONITEM , DBEnumParamEntityType.ENV_SEGMENT_MONITEM , DBEnumObjectVersionType.ENVIRONMENT , TABLE_MONITEM , FIELD_MONITEM_ID );
		DBSettings.loaddbAppEntity( c , entity );
		return( entity );
	}
	
	public static void dropEnvData( DBConnection c , ProductMeta storage ) throws Exception {
		dropEnvDumpData( c , storage );
		dropEnvDesignData( c , storage );
		dropEnvMonData( c , storage );
		dropEnvCoreData( c , storage );
	}

	public static void dropEnvDumpData( DBConnection c , ProductMeta storage ) throws Exception {
	}
	
	public static void dropEnvDesignData( DBConnection c , ProductMeta storage ) throws Exception {
	}
	
	public static void dropEnvMonData( DBConnection c , ProductMeta storage ) throws Exception {
		if( !c.modify( DBQueries.MODIFY_ENVALL_DELETEALL_MONITEM1 , new String[] { EngineDB.getInteger( storage.ID ) } ) )
			Common.exitUnexpected();
		if( !c.modify( DBQueries.MODIFY_ENVALL_DELETEALL_MONTARGET1 , new String[] { EngineDB.getInteger( storage.ID ) } ) )
			Common.exitUnexpected();
	}
	
	public static void dropEnvCoreData( DBConnection c , ProductMeta storage ) throws Exception {
		if( !c.modify( DBQueries.MODIFY_ENVALL_DELETEALL_PARAMVALUES1 , new String[] { EngineDB.getInteger( storage.ID ) } ) )
			Common.exitUnexpected();
		if( !c.modify( DBQueries.MODIFY_ENVALL_DELETEALL_NODES1 , new String[] { EngineDB.getInteger( storage.ID ) } ) )
			Common.exitUnexpected();
		if( !c.modify( DBQueries.MODIFY_ENVALL_DELETEALL_DEPLOYMENTS1 , new String[] { EngineDB.getInteger( storage.ID ) } ) )
			Common.exitUnexpected();
		if( !c.modify( DBQueries.MODIFY_ENVALL_DELETEALL_SERVERDEPS1 , new String[] { EngineDB.getInteger( storage.ID ) } ) )
			Common.exitUnexpected();
		if( !c.modify( DBQueries.MODIFY_ENVALL_DELETEALL_STARTGROUPSERVERS1 , new String[] { EngineDB.getInteger( storage.ID ) } ) )
			Common.exitUnexpected();
		if( !c.modify( DBQueries.MODIFY_ENVALL_DELETEALL_STARTGROUPS1 , new String[] { EngineDB.getInteger( storage.ID ) } ) )
			Common.exitUnexpected();
		if( !c.modify( DBQueries.MODIFY_ENVALL_DELETEALL_SERVERS1 , new String[] { EngineDB.getInteger( storage.ID ) } ) )
			Common.exitUnexpected();
		if( !c.modify( DBQueries.MODIFY_ENVALL_DELETEALL_SEGMENTS1 , new String[] { EngineDB.getInteger( storage.ID ) } ) )
			Common.exitUnexpected();
		if( !c.modify( DBQueries.MODIFY_ENVALL_DELETEALL_ENVS1 , new String[] { EngineDB.getInteger( storage.ID ) } ) )
			Common.exitUnexpected();
	}
	
}
