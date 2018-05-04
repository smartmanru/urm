package org.urm.db.env;

import org.urm.common.Common;
import org.urm.db.DBConnection;
import org.urm.db.DBQueries;
import org.urm.db.EngineDB;
import org.urm.db.core.DBEnums.*;
import org.urm.db.core.DBSettings;
import org.urm.db.engine.DBEngineEntities;
import org.urm.engine.data.EngineEntities;
import org.urm.engine.properties.EntityVar;
import org.urm.engine.properties.PropertyEntity;
import org.urm.meta.env.MetaDump;
import org.urm.meta.env.MetaDumpMask;
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
	public static String TABLE_STARTGROUPSERVER = "urm_env_startgroup_server";
	public static String TABLE_DEPLOYMENT = "urm_env_deployment";
	public static String TABLE_MONTARGET = "urm_env_montarget";
	public static String TABLE_MONITEM = "urm_env_monitem";
	public static String TABLE_SERVERDEP = "urm_env_server_deps";
	public static String TABLE_DUMP = "urm_env_dbdump";
	public static String TABLE_DUMPMASK = "urm_env_tablemask";
	public static String FIELD_ENV_ID = "env_id";
	public static String FIELD_ENV_META_ID = "meta_id";
	public static String FIELD_ENV_TRANSITION_META_ID = "transition_meta_id";
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
	public static String FIELD_SEGMENT_DESC = "xdesc";
	public static String FIELD_SEGMENT_BASELINE_ID = "baseline_segment_fkid";
	public static String FIELD_SEGMENT_BASELINE_NAME = "baseline_segment_fkname";
	public static String FIELD_SEGMENT_DATACENTER_ID = "datacenter_fkid";
	public static String FIELD_SEGMENT_DATACENTER_NAME = "datacenter_fkname";
	public static String FIELD_SERVER_ID = "server_id";
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
	public static String FIELD_NODE_SERVER_ID = "server_id";
	public static String FIELD_NODE_TYPE = "node_type";
	public static String FIELD_NODE_ACCOUNT_ID = "account_fkid";
	public static String FIELD_NODE_ACCOUNT_NAME = "account_fkname";
	public static String FIELD_STARTGROUP_ID = "startgroup_id";
	public static String FIELD_STARTGROUP_SEGMENT_ID = "segment_id";
	public static String FIELD_STARTGROUP_DESC = "xdesc";
	public static String FIELD_STARTGROUP_POS = "pos";
	public static String FIELD_STARTGROUPSERVER_GROUP_ID = "startgroup_id";
	public static String FIELD_STARTGROUPSERVER_SERVER_ID = "server_id";
	public static String FIELD_DEPLOYMENT_ID = "deployment_id";
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
	public static String FIELD_SERVERDEP_SERVER_ID = "server_id";
	public static String FIELD_SERVERDEP_SERVER_DEP_ID = "dep_server_id";
	public static String FIELD_SERVERDEP_TYPE = "serverdependency_type";
	public static String FIELD_MONTARGET_ID = "montarget_id";
	public static String FIELD_MONTARGET_SEGMENT_ID = "segment_id";
	public static String FIELD_MONTARGET_MAJOR_ENABLED = "major_enabled";
	public static String FIELD_MONTARGET_MAJOR_SCHEDULE = "major_schedule";
	public static String FIELD_MONTARGET_MAJOR_MAXTIME = "major_maxtime";
	public static String FIELD_MONTARGET_MINOR_ENABLED = "minor_enabled";
	public static String FIELD_MONTARGET_MINOR_SCHEDULE = "minor_schedule";
	public static String FIELD_MONTARGET_MINOR_MAXTIME = "minor_maxtime";
	public static String FIELD_MONITEM_ID = "monitem_id";
	public static String FIELD_MONITEM_TARGET_ID = "montarget_id";
	public static String FIELD_MONITEM_TYPE = "monitem_type";
	public static String FIELD_MONITEM_DESC = "xdesc";
	public static String FIELD_DUMP_ID = "dump_id";
	public static String FIELD_DUMP_DESC = "xdesc";
	public static String FIELD_DUMP_SERVER_ID = "server_id";
	public static String FIELD_DUMP_EXPORT = "modeexport";
	public static String FIELD_DUMP_SETDBENV = "remote_setdbenv";
	public static String FIELD_DUMP_DATAPUMPDIR = "database_datapumpdir";
	public static String FIELD_DUMP_STANDBY = "usestandby";
	public static String FIELD_DUMP_NFS = "usenfs";
	public static String FIELD_DUMPMASK_ID = "tablemask_id";
	public static String FIELD_DUMPMASK_DUMP_ID = "dump_id";
	public static String FIELD_DUMPMASK_INCLUDE = "modeinclude";
	public static String FIELD_DUMPMASK_SCHEMA_ID = "schema_fkid";
	public static String FIELD_DUMPMASK_SCHEMA_NAME = "schema_fkname";
	public static String FIELD_DUMPMASK_MASK = "tablemask";
	
	public static PropertyEntity makeEntityEnvPrimary( DBConnection c , boolean upgrade ) throws Exception {
		PropertyEntity entity = PropertyEntity.getAppObjectEntity( DBEnumObjectType.ENVIRONMENT , DBEnumParamEntityType.ENV_PRIMARY , DBEnumObjectVersionType.ENVIRONMENT , TABLE_ENV , FIELD_ENV_ID , false );
		if( !upgrade ) {
			DBSettings.loaddbAppEntity( c , entity );
			return( entity );
		}
		
		return( DBSettings.savedbObjectEntity( c , entity , new EntityVar[] { 
				EntityVar.metaObjectDatabaseOnly( FIELD_ENV_META_ID , "product meta id" , DBEnumObjectType.META , true ) ,
				EntityVar.metaObjectDatabaseOnly( FIELD_ENV_TRANSITION_META_ID , "transition meta id" , DBEnumObjectType.META , false ) ,
				EntityVar.metaBooleanDatabaseOnly( FIELD_ENV_MATCHED , "environment match status" , false , false ) ,
				EntityVar.metaString( MetaEnv.PROPERTY_NAME , "name" , true , null ) ,
				EntityVar.metaStringVar( MetaEnv.PROPERTY_DESC , FIELD_ENV_DESC , "Description" , false , null ) ,
				EntityVar.metaEnumVar( MetaEnv.PROPERTY_ENVTYPE , FIELD_ENV_ENVTYPE , "Environment type" , true , DBEnumEnvType.UNKNOWN ) ,
				EntityVar.metaObjectDatabaseOnly( FIELD_ENV_BASELINE_ID , "baseline environment id" , DBEnumObjectType.ENVIRONMENT , false ) ,
				EntityVar.metaStringVar( MetaEnv.PROPERTY_BASELINE , FIELD_ENV_BASELINE_NAME , "baseline environment name" , false , null ) ,
				EntityVar.metaBoolean( MetaEnv.PROPERTY_OFFLINE , "Offline" , true , false ) ,
				EntityVar.metaObjectDatabaseOnly( FIELD_ENV_ENVKEY_ID , "environment key resource id" , DBEnumObjectType.RESOURCE , false ) ,
				EntityVar.metaStringVar( MetaEnv.PROPERTY_ENVKEY , FIELD_ENV_ENVKEY_NAME , "environment key resource name" , false , null ) ,
				EntityVar.metaBooleanVar( MetaEnv.PROPERTY_DISTR_REMOTE , FIELD_ENV_REMOTE , "remote distributive" , false , false ) ,
				EntityVar.metaObjectDatabaseOnly( FIELD_ENV_REMOTE_ACCOUNT_ID , "remote distributive account id" , DBEnumObjectType.HOSTACCOUNT , false ) ,
				EntityVar.metaStringVar( MetaEnv.PROPERTY_DISTR_HOSTLOGIN , FIELD_ENV_REMOTE_ACCOUNT_NAME , "remote distributive account name" , false , null ) ,
				EntityVar.metaStringVar( MetaEnv.PROPERTY_DISTR_PATH , FIELD_ENV_REMOTE_PATH , "remote distributive path" , false , null ) ,
		} ) );
	}

	public static PropertyEntity makeEntityEnvExtra( DBConnection c , boolean upgrade ) throws Exception {
		PropertyEntity entity = PropertyEntity.getAppPropsEntity( DBEnumObjectType.ENVIRONMENT , DBEnumParamEntityType.ENV_EXTRA , DBEnumObjectVersionType.ENVIRONMENT );
		if( !upgrade ) {
			DBSettings.loaddbAppEntity( c , entity );
			return( entity );
		}
		
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

	public static PropertyEntity makeEntitySegmentPrimary( DBConnection c , boolean upgrade ) throws Exception {
		PropertyEntity entity = PropertyEntity.getAppObjectEntity( DBEnumObjectType.ENVIRONMENT_SEGMENT , DBEnumParamEntityType.ENV_SEGMENT_PRIMARY , DBEnumObjectVersionType.ENVIRONMENT , TABLE_SEGMENT , FIELD_SEGMENT_ID , false );
		if( !upgrade ) {
			DBSettings.loaddbAppEntity( c , entity );
			return( entity );
		}
		
		return( DBSettings.savedbObjectEntity( c , entity , new EntityVar[] { 
				EntityVar.metaObjectDatabaseOnly( FIELD_ENV_ID , "environment id" , DBEnumObjectType.ENVIRONMENT , false ) ,
				EntityVar.metaString( MetaEnvSegment.PROPERTY_NAME , "name" , true , null ) ,
				EntityVar.metaStringVar( MetaEnvSegment.PROPERTY_DESC , FIELD_SEGMENT_DESC , "Description" , false , null ) ,
				EntityVar.metaObjectDatabaseOnly( FIELD_SEGMENT_BASELINE_ID , "baseline segment id" , DBEnumObjectType.ENVIRONMENT_SEGMENT , false ) ,
				EntityVar.metaStringVar( MetaEnvSegment.PROPERTY_BASELINE , FIELD_SEGMENT_BASELINE_NAME , "baseline segment name" , false , null ) ,
				EntityVar.metaBoolean( MetaEnvSegment.PROPERTY_OFFLINE , "Offline" , true , false ) ,
				EntityVar.metaObjectDatabaseOnly( FIELD_SEGMENT_DATACENTER_ID , "segment datacenter id" , DBEnumObjectType.DATACENTER , false ) ,
				EntityVar.metaStringVar( MetaEnvSegment.PROPERTY_DC , FIELD_SEGMENT_DATACENTER_NAME , "segment datacenter resource name" , false , null ) ,
		} ) );
	}

	public static PropertyEntity makeEntityServerPrimary( DBConnection c , boolean upgrade ) throws Exception {
		PropertyEntity entity = PropertyEntity.getAppObjectEntity( DBEnumObjectType.ENVIRONMENT_SERVER , DBEnumParamEntityType.ENV_SERVER_PRIMARY , DBEnumObjectVersionType.ENVIRONMENT , TABLE_SERVER , FIELD_SERVER_ID , false );
		if( !upgrade ) {
			DBSettings.loaddbAppEntity( c , entity );
			return( entity );
		}
		
		return( DBSettings.savedbObjectEntity( c , entity , new EntityVar[] { 
				EntityVar.metaObjectDatabaseOnly( FIELD_ENV_ID , "environment id" , DBEnumObjectType.ENVIRONMENT , false ) ,
				EntityVar.metaObjectDatabaseOnly( FIELD_SERVER_SEGMENT_ID , "environment segment id" , DBEnumObjectType.ENVIRONMENT_SEGMENT , false ) ,
				EntityVar.metaString( MetaEnvServer.PROPERTY_NAME , "name" , true , null ) ,
				EntityVar.metaStringVar( MetaEnvServer.PROPERTY_DESC , FIELD_SERVER_DESC , "Description" , false , null ) ,
				EntityVar.metaEnumVar( MetaEnvServer.PROPERTY_SERVERRUNTYPE , FIELD_SERVER_RUNTYPE , "Run type" , true , DBEnumServerRunType.UNKNOWN ) ,
				EntityVar.metaEnumVar( MetaEnvServer.PROPERTY_SERVERACCESSTYPE , FIELD_SERVER_ACCESSTYPE , "Access type" , true , DBEnumServerAccessType.UNKNOWN ) ,
				EntityVar.metaEnumVar( MetaEnvServer.PROPERTY_OSTYPE , FIELD_SERVER_OSTYPE , "Operating system type" , true , DBEnumOSType.UNKNOWN ) ,
				EntityVar.metaString( MetaEnvServer.PROPERTY_SYSNAME , "system name" , false , null ) ,
				EntityVar.metaObjectDatabaseOnly( FIELD_SERVER_BASELINE_ID , "baseline server id" , DBEnumObjectType.ENVIRONMENT_SERVER , false ) ,
				EntityVar.metaStringVar( MetaEnvServer.PROPERTY_BASELINE , FIELD_SERVER_BASELINE_NAME , "baseline server name" , false , null ) ,
				EntityVar.metaBoolean( MetaEnvServer.PROPERTY_OFFLINE , "Offline" , true , false ) ,
				EntityVar.metaEnumVar( MetaEnvServer.PROPERTY_DBMSTYPE , FIELD_SERVER_DBMSTYPE , "Database system type" , true , DBEnumDbmsType.UNKNOWN ) ,
				EntityVar.metaObjectDatabaseOnly( FIELD_SERVER_ADMSCHEMA_ID , "administrative database schema id" , DBEnumObjectType.DBSCHEMA , false ) ,
				EntityVar.metaStringVar( MetaEnvServer.PROPERTY_ADMSCHEMA , FIELD_SERVER_ADMSCHEMA_NAME , "administrative database schema name" , false , null ) ,
				EntityVar.metaObjectDatabaseOnly( FIELD_SERVER_BASEITEM_ID , "base software item id" , DBEnumObjectType.BASE_ITEM , false ) ,
				EntityVar.metaStringVar( MetaEnvServer.PROPERTY_BASEITEM , FIELD_SERVER_BASEITEM_NAME , "base software item name" , false , null ) ,
		} ) );
	}

	public static PropertyEntity makeEntityServerExtra( DBConnection c , boolean upgrade ) throws Exception {
		PropertyEntity entity = PropertyEntity.getAppPropsEntity( DBEnumObjectType.ENVIRONMENT_SERVER , DBEnumParamEntityType.ENV_SERVER_EXTRA , DBEnumObjectVersionType.ENVIRONMENT );
		if( !upgrade ) {
			DBSettings.loaddbAppEntity( c , entity );
			return( entity );
		}
		
		return( DBSettings.savedbObjectEntity( c , entity , new EntityVar[] { 
				EntityVar.metaString( MetaEnvServer.PROPERTY_XDOC , "design identifier" , false , null ) ,
				EntityVar.metaString( MetaEnvServer.PROPERTY_ROOTPATH , "environment chatroom" , false , null ) ,
				EntityVar.metaString( MetaEnvServer.PROPERTY_BINPATH , "server scripts path" , false , null ) ,
				EntityVar.metaInteger( MetaEnvServer.PROPERTY_PORT , "primary server port" , false , null ) ,
				EntityVar.metaInteger( MetaEnvServer.PROPERTY_STARTTIME , "maximum start time" , false , 60 ) ,
				EntityVar.metaInteger( MetaEnvServer.PROPERTY_STOPTIME , "maximum stop time" , false , 60 ) ,
				EntityVar.metaString( MetaEnvServer.PROPERTY_DEPLOYPATH , "deploy path" , false , null ) ,
				EntityVar.metaString( MetaEnvServer.PROPERTY_LINKFROMPATH , "runtime area links" , false , null ) ,
				EntityVar.metaString( MetaEnvServer.PROPERTY_DEPLOYSCRIPT , "specific remote deploy script path" , false , null ) ,
				EntityVar.metaString( MetaEnvServer.PROPERTY_HOTDEPLOYPATH , "hot deploy path" , false , null ) ,
				EntityVar.metaString( MetaEnvServer.PROPERTY_HOTDEPLOYDATA , "hot deploy state folder" , false , null ) ,
				EntityVar.metaString( MetaEnvServer.PROPERTY_WEBSERVICEURL , "web service url" , false , null ) ,
				EntityVar.metaString( MetaEnvServer.PROPERTY_WEBMAINURL , "web frontend url" , false , null ) ,
				EntityVar.metaString( MetaEnvServer.PROPERTY_LOGPATH , "log directory path" , false , null ) ,
				EntityVar.metaString( MetaEnvServer.PROPERTY_LOGFILEPATH , "primary log file path" , false , null ) ,
				EntityVar.metaBoolean( MetaEnvServer.PROPERTY_NOPIDS , "do not select os processes by program name" , true , false ) ,
				EntityVar.metaString( MetaEnvServer.PROPERTY_DBMSADDR , "database remote access url" , false , null ) ,
				EntityVar.metaString( MetaEnvServer.PROPERTY_ALIGNED , "aligned data set" , false , null ) ,
				EntityVar.metaString( MetaEnvServer.PROPERTY_REGIONS , "regions set" , false , null )
		} ) );
	}

	public static PropertyEntity makeEntityNodePrimary( DBConnection c , boolean upgrade ) throws Exception {
		PropertyEntity entity = PropertyEntity.getAppObjectEntity( DBEnumObjectType.ENVIRONMENT_NODE , DBEnumParamEntityType.ENV_NODE_PRIMARY , DBEnumObjectVersionType.ENVIRONMENT , TABLE_NODE , FIELD_NODE_ID , false );
		if( !upgrade ) {
			DBSettings.loaddbAppEntity( c , entity );
			return( entity );
		}
		
		return( DBSettings.savedbObjectEntity( c , entity , new EntityVar[] { 
				EntityVar.metaObjectDatabaseOnly( FIELD_ENV_ID , "environment id" , DBEnumObjectType.ENVIRONMENT , true ) ,
				EntityVar.metaObjectDatabaseOnly( FIELD_NODE_SERVER_ID , "environment server id" , DBEnumObjectType.ENVIRONMENT_SERVER , true ) ,
				EntityVar.metaInteger( MetaEnvServerNode.PROPERTY_POS , "position" , true , null ) ,
				EntityVar.metaEnumVar( MetaEnvServerNode.PROPERTY_NODETYPE , FIELD_NODE_TYPE , "Node type" , true , DBEnumNodeType.UNKNOWN ) ,
				EntityVar.metaObjectDatabaseOnly( FIELD_NODE_ACCOUNT_ID , "infrastructure account id" , DBEnumObjectType.HOSTACCOUNT , false ) ,
				EntityVar.metaStringVar( MetaEnvServerNode.PROPERTY_HOSTLOGIN , FIELD_NODE_ACCOUNT_NAME , "infrastructure account name" , false , null ) ,
				EntityVar.metaString( MetaEnvServerNode.PROPERTY_DEPLOYGROUP , "deploy group" , false , null ) ,
				EntityVar.metaBoolean( MetaEnvServerNode.PROPERTY_OFFLINE , "Offline" , true , false ) ,
				EntityVar.metaString( MetaEnvServerNode.PROPERTY_DBINSTANCE , "database instance node code" , false , null ) ,
				EntityVar.metaBoolean( MetaEnvServerNode.PROPERTY_DBSTANDBY , "standby node" , false , false ) ,
		} ) );
	}

	public static PropertyEntity makeEntityStartGroup( DBConnection c , boolean upgrade ) throws Exception {
		PropertyEntity entity = PropertyEntity.getAppObjectEntity( DBEnumObjectType.ENVIRONMENT_STARTGROUP , DBEnumParamEntityType.ENV_SEGMENT_STARTGROUP , DBEnumObjectVersionType.ENVIRONMENT , TABLE_STARTGROUP , FIELD_STARTGROUP_ID , false );
		if( !upgrade ) {
			DBSettings.loaddbAppEntity( c , entity );
			return( entity );
		}
		
		return( DBSettings.savedbObjectEntity( c , entity , new EntityVar[] { 
				EntityVar.metaObjectDatabaseOnly( FIELD_ENV_ID , "environment id" , DBEnumObjectType.ENVIRONMENT , true ) ,
				EntityVar.metaObjectDatabaseOnly( FIELD_STARTGROUP_SEGMENT_ID , "segment id" , DBEnumObjectType.ENVIRONMENT_SEGMENT , true ) ,
				EntityVar.metaString( MetaEnvStartGroup.PROPERTY_NAME , "name" , true , null ) ,
				EntityVar.metaStringVar( MetaEnvStartGroup.PROPERTY_DESC , FIELD_STARTGROUP_DESC , "Description" , false , null ) ,
				EntityVar.metaIntegerDatabaseOnly( FIELD_STARTGROUP_POS , "group order" , true , 0 ) ,
				EntityVar.metaStringXmlOnly( MetaEnvStartGroup.PROPERTY_SERVERS , "spece-delimited list of servers" , false , null ) ,
		} ) );
	}

	public static PropertyEntity makeEntityStartGroupServer( DBConnection c , boolean upgrade ) throws Exception {
		PropertyEntity entity = PropertyEntity.getAppAssociativeEntity( DBEnumObjectType.ENVIRONMENT_STARTGROUP , DBEnumParamEntityType.ENV_SEGMENT_STARTGROUPSERVER , DBEnumObjectVersionType.ENVIRONMENT , TABLE_STARTGROUPSERVER , false , 2 );
		if( !upgrade ) {
			DBSettings.loaddbAppEntity( c , entity );
			return( entity );
		}
		
		return( DBSettings.savedbObjectEntity( c , entity , new EntityVar[] { 
				EntityVar.metaObjectDatabaseOnly( FIELD_STARTGROUPSERVER_GROUP_ID , "start group id" , DBEnumObjectType.ENVIRONMENT_STARTGROUP , true ) ,
				EntityVar.metaObjectDatabaseOnly( FIELD_STARTGROUPSERVER_SERVER_ID , "start group server id" , DBEnumObjectType.ENVIRONMENT_SERVER , true ) ,
				EntityVar.metaObjectDatabaseOnly( FIELD_ENV_ID , "environment id" , DBEnumObjectType.ENVIRONMENT , true )
		} ) );
	}

	public static PropertyEntity makeEntityServerDeployment( DBConnection c , boolean upgrade ) throws Exception {
		PropertyEntity entity = PropertyEntity.getAppObjectEntity( DBEnumObjectType.ENVIRONMENT_DEPLOYMENT , DBEnumParamEntityType.ENV_DEPLOYMENT , DBEnumObjectVersionType.ENVIRONMENT , TABLE_DEPLOYMENT , FIELD_DEPLOYMENT_ID , false );
		if( !upgrade ) {
			DBSettings.loaddbAppEntity( c , entity );
			return( entity );
		}
		
		return( DBSettings.savedbObjectEntity( c , entity , new EntityVar[] { 
				EntityVar.metaObjectDatabaseOnly( FIELD_ENV_ID , "environment id" , DBEnumObjectType.ENVIRONMENT , true ) ,
				EntityVar.metaObjectDatabaseOnly( FIELD_DEPLOYMENT_SERVER_ID , "environment server id" , DBEnumObjectType.ENVIRONMENT_SERVER , true ) ,
				EntityVar.metaEnumDatabaseOnly( FIELD_DEPLOYMENT_TYPE , "deployment type" , true , DBEnumServerDeploymentType.UNKNOWN ) ,
				EntityVar.metaObjectDatabaseOnly( FIELD_DEPLOYMENT_COMP_ID , "component id" , DBEnumObjectType.META_DIST_COMPONENT , false ) ,
				EntityVar.metaStringVar( MetaEnvServerDeployment.PROPERTY_COMPONENT , FIELD_DEPLOYMENT_COMP_NAME , "component name" , false , null ) ,
				EntityVar.metaObjectDatabaseOnly( FIELD_DEPLOYMENT_BINARY_ID , "binary item id" , DBEnumObjectType.META_DIST_BINARYITEM , false ) ,
				EntityVar.metaStringVar( MetaEnvServerDeployment.PROPERTY_DISTITEM , FIELD_DEPLOYMENT_BINARY_NAME , "binary item name" , false , null ) ,
				EntityVar.metaObjectDatabaseOnly( FIELD_DEPLOYMENT_CONF_ID , "conf item id" , DBEnumObjectType.META_DIST_CONFITEM , false ) ,
				EntityVar.metaStringVar( MetaEnvServerDeployment.PROPERTY_CONFITEM , FIELD_DEPLOYMENT_CONF_NAME , "conf item name" , false , null ) ,
				EntityVar.metaObjectDatabaseOnly( FIELD_DEPLOYMENT_SCHEMA_ID , "schema id" , DBEnumObjectType.META_SCHEMA , false ) ,
				EntityVar.metaStringVar( MetaEnvServerDeployment.PROPERTY_SCHEMA , FIELD_DEPLOYMENT_SCHEMA_NAME , "schema name" , false , null ) ,
				EntityVar.metaEnumVar( MetaEnvServerDeployment.PROPERTY_DEPLOYMODE , FIELD_DEPLOYMENT_DEPLOYMODE , "deploy mode" , true , DBEnumDeployModeType.UNKNOWN ) ,
				EntityVar.metaString( MetaEnvServerDeployment.PROPERTY_DEPLOYPATH , "deployment path" , false , null ) ,
				EntityVar.metaString( MetaEnvServerDeployment.PROPERTY_DBNAME , "database schema name" , false , null ) ,
				EntityVar.metaString( MetaEnvServerDeployment.PROPERTY_DBUSER , "database schema user" , false , null ) ,
				EntityVar.metaEnumVar( MetaEnvServerDeployment.PROPERTY_NODETYPE , FIELD_DEPLOYMENT_NODETYPE , "node type to deploy" , true , DBEnumNodeType.UNKNOWN )
		} ) );
	}

	public static PropertyEntity makeEntityServerDependency( DBConnection c , boolean upgrade ) throws Exception {
		PropertyEntity entity = PropertyEntity.getAppAssociativeEntity( DBEnumObjectType.ENVIRONMENT_SERVERDEP , DBEnumParamEntityType.ENV_SERVER_DEPENDENCY , DBEnumObjectVersionType.ENVIRONMENT , TABLE_SERVERDEP , false , 2 );
		if( !upgrade ) {
			DBSettings.loaddbAppEntity( c , entity );
			return( entity );
		}
		
		return( DBSettings.savedbObjectEntity( c , entity , new EntityVar[] { 
				EntityVar.metaObjectDatabaseOnly( FIELD_SERVERDEP_SERVER_ID , "environment server id" , DBEnumObjectType.ENVIRONMENT_SERVER , true ) ,
				EntityVar.metaObjectDatabaseOnly( FIELD_SERVERDEP_SERVER_DEP_ID , "environment dependency server id" , DBEnumObjectType.ENVIRONMENT_SERVER , true ) ,
				EntityVar.metaObjectDatabaseOnly( FIELD_ENV_ID , "environment id" , DBEnumObjectType.ENVIRONMENT , true ) ,
				EntityVar.metaEnumDatabaseOnly( FIELD_SERVERDEP_TYPE , "server dependency type" , true , DBEnumServerDependencyType.UNKNOWN )
		} ) );
	}

	public static PropertyEntity makeEntityMonitoringTarget( DBConnection c , boolean upgrade ) throws Exception {
		PropertyEntity entity = PropertyEntity.getAppObjectEntity( DBEnumObjectType.ENVIRONMENT_MONTARGET , DBEnumParamEntityType.ENV_SEGMENT_MONTARGET , DBEnumObjectVersionType.ENVIRONMENT , TABLE_MONTARGET , FIELD_MONTARGET_ID , false );
		if( !upgrade ) {
			DBSettings.loaddbAppEntity( c , entity );
			return( entity );
		}
		
		return( DBSettings.savedbObjectEntity( c , entity , new EntityVar[] { 
				EntityVar.metaObjectDatabaseOnly( FIELD_ENV_ID , "environment id" , DBEnumObjectType.ENVIRONMENT , true ) ,
				EntityVar.metaObjectDatabaseOnly( FIELD_MONTARGET_SEGMENT_ID , "segment id" , DBEnumObjectType.ENVIRONMENT_SEGMENT , true ) ,
				EntityVar.metaStringXmlOnly( MetaMonitoringTarget.PROPERTY_ENV , "environment name" , true , null ) ,
				EntityVar.metaStringXmlOnly( MetaMonitoringTarget.PROPERTY_SEGMENT , "segment name" , true , null ) ,
				EntityVar.metaBooleanVar( MetaMonitoringTarget.PROPERTY_MAJOR_ENABLED , FIELD_MONTARGET_MAJOR_ENABLED , "Enabled major monitoring" , true , false ) ,
				EntityVar.metaStringVar( MetaMonitoringTarget.PROPERTY_MAJOR_SCHEDULE , FIELD_MONTARGET_MAJOR_SCHEDULE , "major schedule" , false , null ) ,
				EntityVar.metaIntegerVar( MetaMonitoringTarget.PROPERTY_MAJOR_MAXTIME , FIELD_MONTARGET_MAJOR_MAXTIME , "major max time" , true , 300000 ) ,
				EntityVar.metaBooleanVar( MetaMonitoringTarget.PROPERTY_MINOR_ENABLED , FIELD_MONTARGET_MINOR_ENABLED , "Enabled minor monitoring" , true , false ) ,
				EntityVar.metaStringVar( MetaMonitoringTarget.PROPERTY_MINOR_SCHEDULE , FIELD_MONTARGET_MINOR_SCHEDULE , "minor schedule" , false , null ) ,
				EntityVar.metaIntegerVar( MetaMonitoringTarget.PROPERTY_MINOR_MAXTIME , FIELD_MONTARGET_MINOR_MAXTIME , "minor max time" , true , 300000 ) ,
		} ) );
	}

	public static PropertyEntity makeEntityMonitoringItem( DBConnection c , boolean upgrade ) throws Exception {
		PropertyEntity entity = PropertyEntity.getAppObjectEntity( DBEnumObjectType.ENVIRONMENT_MONITEM , DBEnumParamEntityType.ENV_SEGMENT_MONITEM , DBEnumObjectVersionType.ENVIRONMENT , TABLE_MONITEM , FIELD_MONITEM_ID , false );
		if( !upgrade ) {
			DBSettings.loaddbAppEntity( c , entity );
			return( entity );
		}
		
		return( DBSettings.savedbObjectEntity( c , entity , new EntityVar[] { 
				EntityVar.metaObjectDatabaseOnly( FIELD_ENV_ID , "environment id" , DBEnumObjectType.ENVIRONMENT , true ) ,
				EntityVar.metaObjectDatabaseOnly( FIELD_MONITEM_TARGET_ID , "monitoring target id" , DBEnumObjectType.ENVIRONMENT_MONTARGET , true ) ,
				EntityVar.metaStringVar( MetaMonitoringItem.PROPERTY_DESC , FIELD_MONITEM_DESC , "description" , false , null ) ,
				EntityVar.metaEnumVar( MetaMonitoringItem.PROPERTY_TYPE , FIELD_MONITEM_TYPE , "monitoring item type" , true , DBEnumMonItemType.UNKNOWN ) ,
				EntityVar.metaString( MetaMonitoringItem.PROPERTY_URL , "check url" , false , null ) ,
				EntityVar.metaString( MetaMonitoringItem.PROPERTY_WSDATA , "check request" , false , null ) ,
				EntityVar.metaString( MetaMonitoringItem.PROPERTY_WSCHECK , "check request response" , false , null )
		} ) );
	}

	public static PropertyEntity makeEntityDump( DBConnection c , boolean upgrade ) throws Exception {
		PropertyEntity entity = PropertyEntity.getAppObjectEntity( DBEnumObjectType.ENVIRONMENT_DUMP , DBEnumParamEntityType.ENV_DUMP , DBEnumObjectVersionType.ENVIRONMENT , TABLE_DUMP , FIELD_DUMP_ID , false );
		if( !upgrade ) {
			DBSettings.loaddbAppEntity( c , entity );
			return( entity );
		}
		
		return( DBSettings.savedbObjectEntity( c , entity , new EntityVar[] { 
				EntityVar.metaObjectDatabaseOnly( FIELD_ENV_ID , "environment id" , DBEnumObjectType.ENVIRONMENT , true ) ,
				EntityVar.metaString( MetaDump.PROPERTY_NAME , "name" , true , null ) ,
				EntityVar.metaStringVar( MetaDump.PROPERTY_DESC , FIELD_DUMP_DESC , "description" , false , null ) ,
				EntityVar.metaObjectDatabaseOnly( FIELD_DUMP_SERVER_ID , "server id" , DBEnumObjectType.ENVIRONMENT_SERVER , true ) ,
				EntityVar.metaStringXmlOnly( MetaDump.PROPERTY_SEGMENT , "segment name" , true , null ) ,
				EntityVar.metaStringXmlOnly( MetaDump.PROPERTY_SERVER , "server name" , true , null ) ,
				EntityVar.metaBooleanVar( MetaDump.PROPERTY_EXPORT , FIELD_DUMP_EXPORT , "export direction" , true , true ) ,
				EntityVar.metaString( MetaDump.PROPERTY_DATASET , "dataset folder" , true , null ) ,
				EntityVar.metaBoolean( MetaDump.PROPERTY_OWNTABLESET , "own table set" , true , false ) ,
				EntityVar.metaString( MetaDump.PROPERTY_DUMPDIR , "dump directory to put/get files" , false , null ) ,
				EntityVar.metaStringVar( MetaDump.PROPERTY_SETDBENV , FIELD_DUMP_SETDBENV , "context setup script" , false , null ) ,
				EntityVar.metaStringVar( MetaDump.PROPERTY_DATAPUMPDIR , FIELD_DUMP_DATAPUMPDIR , "dump directory seen from database" , false , null ) ,
				EntityVar.metaString( MetaDump.PROPERTY_POSTREFRESH , "product data folder where there are postrefresh scripts to be applied after import" , false , null ) ,
				EntityVar.metaString( MetaDump.PROPERTY_SCHEDULE , "schedule to perform dump" , false , null ) ,
				EntityVar.metaBooleanVar( MetaDump.PROPERTY_STANDBY , FIELD_DUMP_STANDBY , "use stand by node to export dump" , true , false ) ,
				EntityVar.metaBooleanVar( MetaDump.PROPERTY_NFS , FIELD_DUMP_NFS , "assume nfs is used and avoid copy dump files" , true , false ) ,
				EntityVar.metaBoolean( MetaDump.PROPERTY_OFFLINE , "dump operation is offline" , true , true )
		} ) );
	}

	public static PropertyEntity makeEntityDumpMask( DBConnection c , boolean upgrade ) throws Exception {
		PropertyEntity entity = PropertyEntity.getAppObjectEntity( DBEnumObjectType.ENVIRONMENT_DUMPMASK , DBEnumParamEntityType.ENV_DUMPMASK , DBEnumObjectVersionType.ENVIRONMENT , TABLE_DUMPMASK , FIELD_DUMPMASK_ID , false );
		if( !upgrade ) {
			DBSettings.loaddbAppEntity( c , entity );
			return( entity );
		}
		
		return( DBSettings.savedbObjectEntity( c , entity , new EntityVar[] { 
				EntityVar.metaObjectDatabaseOnly( FIELD_ENV_ID , "environment id" , DBEnumObjectType.ENVIRONMENT , true ) ,
				EntityVar.metaObjectDatabaseOnly( FIELD_DUMPMASK_DUMP_ID , "dump id" , DBEnumObjectType.ENVIRONMENT_DUMP , true ) ,
				EntityVar.metaBooleanVar( MetaDumpMask.PROPERTY_INCLUDE , FIELD_DUMPMASK_INCLUDE , "if tables by mask are included" , true , true ) ,
				EntityVar.metaObjectDatabaseOnly( FIELD_DUMPMASK_SCHEMA_ID , "database schema id" , DBEnumObjectType.DBSCHEMA , false ) ,
				EntityVar.metaStringVar( MetaDumpMask.PROPERTY_SCHEMA , FIELD_DUMPMASK_SCHEMA_NAME , "database schema name" , false , null ) ,
				EntityVar.metaStringVar( MetaDumpMask.PROPERTY_MASK , FIELD_DUMPMASK_MASK , "table mask" , false , null )
		} ) );
	}

	public static void dropEnvData( DBConnection c , ProductMeta storage ) throws Exception {
		EngineEntities entities = c.getEntities();
		
		// dump data
		DBEngineEntities.dropAppObjects( c , entities.entityAppDump , DBQueries.FILTER_ENV_META1 , new String[] { EngineDB.getInteger( storage.ID ) } );
		DBEngineEntities.dropAppObjects( c , entities.entityAppDumpMask , DBQueries.FILTER_ENV_META1 , new String[] { EngineDB.getInteger( storage.ID ) } );
		// design data
		// mon data
		DBEngineEntities.dropAppObjects( c , entities.entityAppSegmentMonItem , DBQueries.FILTER_ENV_META1 , new String[] { EngineDB.getInteger( storage.ID ) } );
		DBEngineEntities.dropAppObjects( c , entities.entityAppSegmentMonTarget , DBQueries.FILTER_ENV_META1 , new String[] { EngineDB.getInteger( storage.ID ) } );
		// core data
		if( !c.modify( DBQueries.MODIFY_ENVALL_DELETEALL_PARAMVALUES1 , new String[] { EngineDB.getInteger( storage.ID ) } ) )
			Common.exitUnexpected();
		DBEngineEntities.dropAppObjects( c , entities.entityAppNodePrimary , DBQueries.FILTER_ENV_META1 , new String[] { EngineDB.getInteger( storage.ID ) } );
		DBEngineEntities.dropAppObjects( c , entities.entityAppServerDeployment , DBQueries.FILTER_ENV_META1 , new String[] { EngineDB.getInteger( storage.ID ) } );
		DBEngineEntities.dropAppObjects( c , entities.entityAppServerDependency , DBQueries.FILTER_ENV_META1 , new String[] { EngineDB.getInteger( storage.ID ) } );
		DBEngineEntities.dropAppObjects( c , entities.entityAppSegmentStartGroupServer , DBQueries.FILTER_ENV_META1 , new String[] { EngineDB.getInteger( storage.ID ) } );
		DBEngineEntities.dropAppObjects( c , entities.entityAppSegmentStartGroup , DBQueries.FILTER_ENV_META1 , new String[] { EngineDB.getInteger( storage.ID ) } );
		DBEngineEntities.dropAppObjects( c , entities.entityAppServerPrimary , DBQueries.FILTER_ENV_META1 , new String[] { EngineDB.getInteger( storage.ID ) } );
		DBEngineEntities.dropAppObjects( c , entities.entityAppSegmentPrimary , DBQueries.FILTER_ENV_META1 , new String[] { EngineDB.getInteger( storage.ID ) } );
		DBEngineEntities.dropAppObjects( c , entities.entityAppEnvPrimary , DBQueries.FILTER_ENV_META1 , new String[] { EngineDB.getInteger( storage.ID ) } );
	}

	public static void dropEnvData( DBConnection c , MetaEnv env ) throws Exception {
		EngineEntities entities = c.getEntities();
		
		// dump data
		DBEngineEntities.dropAppObjects( c , entities.entityAppDump , DBQueries.FILTER_ENV_ID1 , new String[] { EngineDB.getInteger( env.ID ) } );
		DBEngineEntities.dropAppObjects( c , entities.entityAppDumpMask , DBQueries.FILTER_ENV_ID1 , new String[] { EngineDB.getInteger( env.ID ) } );
		// design data
		// mon data
		DBEngineEntities.dropAppObjects( c , entities.entityAppSegmentMonItem , DBQueries.FILTER_ENV_ID1 , new String[] { EngineDB.getInteger( env.ID ) } );
		DBEngineEntities.dropAppObjects( c , entities.entityAppSegmentMonTarget , DBQueries.FILTER_ENV_META1 , new String[] { EngineDB.getInteger( env.ID ) } );
		// core data
		if( !c.modify( DBQueries.MODIFY_ENV_DELETEALL_PARAMVALUES1 , new String[] { EngineDB.getInteger( env.ID ) } ) )
			Common.exitUnexpected();
		DBEngineEntities.dropAppObjects( c , entities.entityAppNodePrimary , DBQueries.FILTER_ENV_ID1 , new String[] { EngineDB.getInteger( env.ID ) } );
		DBEngineEntities.dropAppObjects( c , entities.entityAppServerDeployment , DBQueries.FILTER_ENV_ID1 , new String[] { EngineDB.getInteger( env.ID ) } );
		DBEngineEntities.dropAppObjects( c , entities.entityAppServerDependency , DBQueries.FILTER_ENV_ID1 , new String[] { EngineDB.getInteger( env.ID ) } );
		DBEngineEntities.dropAppObjects( c , entities.entityAppSegmentStartGroupServer , DBQueries.FILTER_ENV_ID1 , new String[] { EngineDB.getInteger( env.ID ) } );
		DBEngineEntities.dropAppObjects( c , entities.entityAppSegmentStartGroup , DBQueries.FILTER_ENV_ID1 , new String[] { EngineDB.getInteger( env.ID ) } );
		DBEngineEntities.dropAppObjects( c , entities.entityAppServerPrimary , DBQueries.FILTER_ENV_ID1 , new String[] { EngineDB.getInteger( env.ID ) } );
		DBEngineEntities.dropAppObjects( c , entities.entityAppSegmentPrimary , DBQueries.FILTER_ENV_ID1 , new String[] { EngineDB.getInteger( env.ID ) } );
		DBEngineEntities.dropAppObjects( c , entities.entityAppEnvPrimary , DBQueries.FILTER_ENV_ID1 , new String[] { EngineDB.getInteger( env.ID ) } );
	}

}
