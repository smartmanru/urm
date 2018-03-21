package org.urm.db.release;

import org.urm.db.DBConnection;
import org.urm.db.DBQueries;
import org.urm.db.EngineDB;
import org.urm.db.core.DBEnums.DBEnumBuildModeType;
import org.urm.db.core.DBEnums.DBEnumLifecycleStageType;
import org.urm.db.core.DBEnums.DBEnumReleaseTargetType;
import org.urm.db.core.DBEnums.DBEnumScopeCategoryType;
import org.urm.db.core.DBEnums.DBEnumTicketSetStatusType;
import org.urm.db.core.DBEnums.DBEnumTicketStatusType;
import org.urm.db.core.DBEnums.DBEnumTicketType;
import org.urm.db.core.DBSettings;
import org.urm.db.core.DBEnums.DBEnumLifecycleType;
import org.urm.db.core.DBEnums.DBEnumObjectType;
import org.urm.db.core.DBEnums.DBEnumObjectVersionType;
import org.urm.db.core.DBEnums.DBEnumParamEntityType;
import org.urm.db.engine.DBEngineEntities;
import org.urm.engine.data.EngineEntities;
import org.urm.engine.properties.EntityVar;
import org.urm.engine.properties.PropertyEntity;
import org.urm.meta.EngineLoader;
import org.urm.meta.release.ProductReleases;
import org.urm.meta.release.Release;
import org.urm.meta.release.ReleaseDist;
import org.urm.meta.release.ReleaseRepository;
import org.urm.meta.release.ReleaseSchedule;
import org.urm.meta.release.ReleaseSchedulePhase;
import org.urm.meta.release.ReleaseTarget;
import org.urm.meta.release.ReleaseTicket;
import org.urm.meta.release.ReleaseTicketSet;
import org.urm.meta.release.ReleaseTicketSetTarget;

public class DBReleaseData {

	public static String TABLE_REPOSITORY = "urm_rel_repository";
	public static String TABLE_MAIN = "urm_rel_main";
	public static String TABLE_DIST = "urm_rel_dist";
	public static String TABLE_TARGET = "urm_rel_target";
	public static String TABLE_SCOPESET = "urm_rel_scopeset";
	public static String TABLE_SCOPETARGET = "urm_rel_scopetarget";
	public static String TABLE_SCOPEITEM = "urm_rel_scopeitem";
	public static String TABLE_SCHEDULE = "urm_rel_schedule";
	public static String TABLE_PHASE = "urm_rel_phase";
	public static String TABLE_TICKETSET = "urm_rel_ticketset";
	public static String TABLE_TICKETTARGET = "urm_rel_tickettarget";
	public static String TABLE_TICKET = "urm_rel_ticket";
	public static String FIELD_RELEASE_ID = "release_id";
	public static String FIELD_REPOSITORY_ID = "repo_id";
	public static String FIELD_REPOSITORY_META_ID = "meta_fkid";
	public static String FIELD_REPOSITORY_META_NAME = "meta_fkname";
	public static String FIELD_MAIN_ID = "release_id";
	public static String FIELD_MAIN_REPO_ID = "repo_id";
	public static String FIELD_MAIN_DESC = "xdesc";
	public static String FIELD_MAIN_LIFECYCLETYPE = "lifecycle_type";
	public static String FIELD_MAIN_V1 = "v1";
	public static String FIELD_MAIN_V2 = "v2";
	public static String FIELD_MAIN_V3 = "v3";
	public static String FIELD_MAIN_V4 = "v4";
	public static String FIELD_MAIN_BUILDMODE = "buildmode_type";
	public static String FIELD_DIST_ID = "dist_id";
	public static String FIELD_DIST_HASH = "data_hash";
	public static String FIELD_DIST_DATE = "dist_date";
	public static String FIELD_DIST_VARIANT = "dist_variant";
	public static String FIELD_TARGET_ID = "releasetarget_id";
	public static String FIELD_TARGET_SCOPECATEGORY = "scopecategory_type";
	public static String FIELD_TARGET_TARGETTYPE = "releasetarget_type";
	public static String FIELD_TARGET_SCOPEALL = "scope_all";
	public static String FIELD_TARGET_SRCSET_ID = "srcset_fkid";
	public static String FIELD_TARGET_SRCSET_NAME = "srcset_fkname";
	public static String FIELD_TARGET_PROJECT_ID = "project_fkid";
	public static String FIELD_TARGET_PROJECT_NAME = "project_fkname";
	public static String FIELD_TARGET_SRCITEM_ID = "srcitem_fkid";
	public static String FIELD_TARGET_SRCITEM_NAME = "srcitem_fkname";
	public static String FIELD_TARGET_DELIVERY_ID = "delivery_fkid";
	public static String FIELD_TARGET_DELIVERY_NAME = "delivery_fkname";
	public static String FIELD_TARGET_BINARY_ID = "binary_fkid";
	public static String FIELD_TARGET_BINARY_NAME = "binary_fkname";
	public static String FIELD_TARGET_CONF_ID = "confitem_fkid";
	public static String FIELD_TARGET_CONF_NAME = "confitem_fkname";
	public static String FIELD_TARGET_SCHEMA_ID = "schema_fkid";
	public static String FIELD_TARGET_SCHEMA_NAME = "schema_fkname";
	public static String FIELD_TARGET_DOC_ID = "doc_fkid";
	public static String FIELD_TARGET_DOC_NAME = "doc_fkname";
	public static String FIELD_TARGET_BRANCH = "build_branch";
	public static String FIELD_TARGET_TAG = "build_tag";
	public static String FIELD_TARGET_VERSION = "build_version";
	public static String FIELD_TARGET_FILE = "targetfile";
	public static String FIELD_TARGET_FILE_HASH = "targetfile_hash";
	public static String FIELD_TARGET_FILE_SIZE = "targetfile_size";
	public static String FIELD_TARGET_FILE_TIME = "targetfile_time";
	public static String FIELD_SCOPESET_ID = "scopeset_id";
	public static String FIELD_SCOPESET_RELEASETARGET_ID = "releasetarget_id";
	public static String FIELD_SCOPETARGET_ID = "scopetarget_id";
	public static String FIELD_SCOPETARGET_SCOPESET_ID = "scopeset_id";
	public static String FIELD_SCOPETARGET_RELEASETARGET_ID = "releasetarget_id";
	public static String FIELD_SCOPEITEM_ID = "scopeitem_id";
	public static String FIELD_SCOPEITEM_RELEASETARGET_ID = "releasetarget_id";
	public static String FIELD_SCOPEITEM_SCOPETARGET_ID = "scopetarget_id";
	public static String FIELD_SCHEDULE_ID = "release_id";
	public static String FIELD_SCHEDULE_STARTED = "start_date_actual";
	public static String FIELD_SCHEDULE_RELEASEDATE = "release_date_scheduled";
	public static String FIELD_SCHEDULE_RELEASEDATEACTUAL = "release_date_actual";
	public static String FIELD_SCHEDULE_COMPLETEDATEACTUAL = "complete_date_actual";
	public static String FIELD_SCHEDULE_PHASE = "phase_current";
	public static String FIELD_PHASE_ID = "phase_id";
	public static String FIELD_PHASE_LCSTAGETYPE = "lifecyclestage_type";
	public static String FIELD_PHASE_STAGEPOS = "stage_pos";
	public static String FIELD_PHASE_DESC = "xdesc";
	public static String FIELD_PHASE_NORMALDAYS = "normal_days";
	public static String FIELD_PHASE_STARTDATE = "start_date";
	public static String FIELD_PHASE_FINISHDATE = "finish_date";
	public static String FIELD_TICKETSET_ID = "ticketset_id";
	public static String FIELD_TICKETSET_DESC = "xdesc";
	public static String FIELD_TICKETSET_STATUS = "ticketsetstatus_type";
	public static String FIELD_TICKETTARGET_ID = "tickettarget_id";
	public static String FIELD_TICKETTARGET_TICKETSET_ID = "ticketset_id";
	public static String FIELD_TICKETTARGET_RELEASETARGET_ID = "releasetarget_id";
	public static String FIELD_TICKET_ID = "ticket_id";
	public static String FIELD_TICKET_TICKETSET_ID = "ticketset_id";
	public static String FIELD_TICKET_DESC = "xdesc";
	public static String FIELD_TICKET_TYPE = "ticket_type";
	public static String FIELD_TICKET_STATUS = "ticketstatus_type";
	public static String FIELD_TICKET_OWNER_ID = "owner_user_fkid";
	public static String FIELD_TICKET_OWNER_NAME = "owner_user_fkname";
	public static String FIELD_TICKET_DEVUSER_ID = "dev_user_fkid";
	public static String FIELD_TICKET_DEVUSER_NAME = "dev_user_fkname";
	public static String FIELD_TICKET_QAUSER_ID = "qa_user_fkid";
	public static String FIELD_TICKET_QAUSER_NAME = "qa_user_fkname";
	
	public static PropertyEntity upgradeEntityReleaseRepository( EngineLoader loader ) throws Exception {
		DBConnection c = loader.getConnection();
		PropertyEntity entity = PropertyEntity.getAppObjectEntity( DBEnumObjectType.RELEASE_REPOSITORY , DBEnumParamEntityType.RELEASE_REPOSITORY , DBEnumObjectVersionType.APP , TABLE_REPOSITORY , FIELD_REPOSITORY_ID );
		return( DBSettings.savedbObjectEntity( c , entity , new EntityVar[] { 
				EntityVar.metaString( ReleaseRepository.PROPERTY_NAME , "repository name" , true , null ) ,
				EntityVar.metaStringVar( ReleaseRepository.PROPERTY_DESC , FIELD_MAIN_DESC , ReleaseRepository.PROPERTY_DESC , "repository description" , false , null ) ,
				EntityVar.metaObjectDatabaseOnly( FIELD_REPOSITORY_META_ID , "product id" , DBEnumObjectType.APPPRODUCT , false ) ,
				EntityVar.metaStringVar( ReleaseRepository.PROPERTY_PRODUCT , FIELD_REPOSITORY_META_NAME , ReleaseRepository.PROPERTY_PRODUCT , "product name" , false , null ) ,
		} ) );
	}

	public static PropertyEntity loaddbEntityReleaseRepository( DBConnection c ) throws Exception {
		PropertyEntity entity = PropertyEntity.getAppObjectEntity( DBEnumObjectType.RELEASE_REPOSITORY , DBEnumParamEntityType.RELEASE_REPOSITORY , DBEnumObjectVersionType.APP , TABLE_REPOSITORY , FIELD_REPOSITORY_ID );
		DBSettings.loaddbAppEntity( c , entity );
		return( entity );
	}
	
	public static PropertyEntity upgradeEntityReleaseMain( EngineLoader loader ) throws Exception {
		DBConnection c = loader.getConnection();
		PropertyEntity entity = PropertyEntity.getAppObjectEntity( DBEnumObjectType.RELEASE_MAIN , DBEnumParamEntityType.RELEASE_MAIN , DBEnumObjectVersionType.RELEASE , TABLE_MAIN , FIELD_MAIN_ID );
		return( DBSettings.savedbObjectEntity( c , entity , new EntityVar[] { 
				EntityVar.metaObjectDatabaseOnly( FIELD_MAIN_REPO_ID , "repo id" , DBEnumObjectType.RELEASE_REPOSITORY , true ) ,
				EntityVar.metaString( Release.PROPERTY_NAME , "product name" , true , null ) ,
				EntityVar.metaStringVar( Release.PROPERTY_DESC , FIELD_MAIN_DESC , Release.PROPERTY_DESC , "Description" , false , null ) ,
				EntityVar.metaBoolean( Release.PROPERTY_MASTER , "master" , true , false ) ,
				EntityVar.metaEnumVar( Release.PROPERTY_LIFECYCLETYPE , FIELD_MAIN_LIFECYCLETYPE , Release.PROPERTY_LIFECYCLETYPE , "Lifecycle type" , true , DBEnumLifecycleType.UNKNOWN ) ,
				EntityVar.metaIntegerDatabaseOnly( FIELD_MAIN_V1 , "version number 1" , true , null ) ,
				EntityVar.metaIntegerDatabaseOnly( FIELD_MAIN_V2 , "version number 2" , true , null ) ,
				EntityVar.metaIntegerDatabaseOnly( FIELD_MAIN_V3 , "version number 3" , true , null ) ,
				EntityVar.metaIntegerDatabaseOnly( FIELD_MAIN_V4 , "version number 4" , true , null ) ,
				EntityVar.metaString( Release.PROPERTY_VERSION , "release version" , true , null ) ,
				EntityVar.metaEnumVar( Release.PROPERTY_BUILDMODE , FIELD_MAIN_BUILDMODE , Release.PROPERTY_BUILDMODE , "Build mode type" , true , DBEnumBuildModeType.UNKNOWN ) ,
				EntityVar.metaString( Release.PROPERTY_COMPATIBILITY , "release compatibility" , false , null ) ,
				EntityVar.metaBoolean( Release.PROPERTY_CUMULATIVE , "cumulative" , true , false ) ,
				EntityVar.metaBoolean( Release.PROPERTY_ARCHIVEDSTATUS , "archived" , true , false )
		} ) );
	}

	public static PropertyEntity loaddbEntityReleaseMain( DBConnection c ) throws Exception {
		PropertyEntity entity = PropertyEntity.getAppObjectEntity( DBEnumObjectType.RELEASE_MAIN , DBEnumParamEntityType.RELEASE_MAIN , DBEnumObjectVersionType.RELEASE , TABLE_MAIN , FIELD_MAIN_ID );
		DBSettings.loaddbAppEntity( c , entity );
		return( entity );
	}
	
	public static PropertyEntity upgradeEntityReleaseDist( EngineLoader loader ) throws Exception {
		DBConnection c = loader.getConnection();
		PropertyEntity entity = PropertyEntity.getAppObjectEntity( DBEnumObjectType.RELEASE_DIST , DBEnumParamEntityType.RELEASE_DIST , DBEnumObjectVersionType.RELEASE , TABLE_DIST , FIELD_DIST_ID );
		return( DBSettings.savedbObjectEntity( c , entity , new EntityVar[] { 
				EntityVar.metaObjectDatabaseOnly( FIELD_RELEASE_ID , "release id" , DBEnumObjectType.RELEASE_MAIN , true ) ,
				EntityVar.metaStringVar( ReleaseDist.PROPERTY_HASH , FIELD_DIST_HASH , ReleaseDist.PROPERTY_HASH , "data hash" , false , null ) ,
				EntityVar.metaDateVar( ReleaseDist.PROPERTY_DATE , FIELD_DIST_DATE , ReleaseDist.PROPERTY_DATE , "data date" , true ) ,
				EntityVar.metaStringVar( ReleaseDist.PROPERTY_VARIANT , FIELD_DIST_VARIANT , ReleaseDist.PROPERTY_VARIANT , "distributive variant" , false , null ) ,
		} ) );
	}

	public static PropertyEntity loaddbEntityReleaseDist( DBConnection c ) throws Exception {
		PropertyEntity entity = PropertyEntity.getAppObjectEntity( DBEnumObjectType.RELEASE_DIST , DBEnumParamEntityType.RELEASE_DIST , DBEnumObjectVersionType.RELEASE , TABLE_DIST , FIELD_DIST_ID );
		DBSettings.loaddbAppEntity( c , entity );
		return( entity );
	}
	
	public static PropertyEntity upgradeEntityReleaseTarget( EngineLoader loader ) throws Exception {
		DBConnection c = loader.getConnection();
		PropertyEntity entity = PropertyEntity.getAppObjectEntity( DBEnumObjectType.RELEASE_TARGET , DBEnumParamEntityType.RELEASE_TARGET , DBEnumObjectVersionType.RELEASE , TABLE_TARGET , FIELD_TARGET_ID );
		return( DBSettings.savedbObjectEntity( c , entity , new EntityVar[] { 
				EntityVar.metaObjectDatabaseOnly( FIELD_RELEASE_ID , "release id" , DBEnumObjectType.RELEASE_MAIN , true ) ,
				EntityVar.metaBoolean( ReleaseTarget.PROPERTY_SCOPETARGET , "scope target" , true , false ) ,
				EntityVar.metaEnumVar( ReleaseTarget.PROPERTY_SCOPECATEGORY , FIELD_TARGET_SCOPECATEGORY , ReleaseTarget.PROPERTY_SCOPECATEGORY , "scope category type" , true , DBEnumScopeCategoryType.UNKNOWN ) ,
				EntityVar.metaEnumVar( ReleaseTarget.PROPERTY_TARGETTYPE , FIELD_TARGET_TARGETTYPE , ReleaseTarget.PROPERTY_TARGETTYPE , "release target type" , true , DBEnumReleaseTargetType.UNKNOWN ) ,
				EntityVar.metaBooleanVar( ReleaseTarget.PROPERTY_ALL , FIELD_TARGET_SCOPEALL , ReleaseTarget.PROPERTY_ALL , "all scope" , false , false ) ,
				EntityVar.metaObjectDatabaseOnly( FIELD_TARGET_SRCSET_ID , "source project set id" , DBEnumObjectType.META_SOURCESET , false ) ,
				EntityVar.metaStringVar( ReleaseTarget.PROPERTY_SRCSET , FIELD_TARGET_SRCSET_NAME , ReleaseTarget.PROPERTY_SRCSET , "source project set name" , false , null ) ,
				EntityVar.metaObjectDatabaseOnly( FIELD_TARGET_PROJECT_ID , "source project id" , DBEnumObjectType.META_SOURCEPROJECT , false ) ,
				EntityVar.metaStringVar( ReleaseTarget.PROPERTY_PROJECT , FIELD_TARGET_PROJECT_NAME , ReleaseTarget.PROPERTY_PROJECT , "source project name" , false , null ) ,
				EntityVar.metaObjectDatabaseOnly( FIELD_TARGET_SRCITEM_ID , "source project item id" , DBEnumObjectType.META_SOURCEITEM , false ) ,
				EntityVar.metaStringVar( ReleaseTarget.PROPERTY_SRCITEM , FIELD_TARGET_SRCITEM_NAME , ReleaseTarget.PROPERTY_SRCITEM , "source project item name" , false , null ) ,
				EntityVar.metaObjectDatabaseOnly( FIELD_TARGET_DELIVERY_ID , "delivery id" , DBEnumObjectType.META_DIST_DELIVERY , false ) ,
				EntityVar.metaStringVar( ReleaseTarget.PROPERTY_DELIVERY , FIELD_TARGET_DELIVERY_NAME , ReleaseTarget.PROPERTY_DELIVERY , "delivery name" , false , null ) ,
				EntityVar.metaObjectDatabaseOnly( FIELD_TARGET_BINARY_ID , "binary item id" , DBEnumObjectType.META_DIST_BINARYITEM , false ) ,
				EntityVar.metaStringVar( ReleaseTarget.PROPERTY_BINARY , FIELD_TARGET_BINARY_NAME , ReleaseTarget.PROPERTY_BINARY , "binary item name" , false , null ) ,
				EntityVar.metaObjectDatabaseOnly( FIELD_TARGET_CONF_ID , "conf item id" , DBEnumObjectType.META_DIST_CONFITEM , false ) ,
				EntityVar.metaStringVar( ReleaseTarget.PROPERTY_CONF , FIELD_TARGET_CONF_NAME , ReleaseTarget.PROPERTY_CONF , "conf item name" , false , null ) ,
				EntityVar.metaObjectDatabaseOnly( FIELD_TARGET_SCHEMA_ID , "schema id" , DBEnumObjectType.META_SCHEMA , false ) ,
				EntityVar.metaStringVar( ReleaseTarget.PROPERTY_SCHEMA , FIELD_TARGET_SCHEMA_NAME , ReleaseTarget.PROPERTY_SCHEMA , "schema name" , false , null ) ,
				EntityVar.metaObjectDatabaseOnly( FIELD_TARGET_DOC_ID , "doc id" , DBEnumObjectType.META_DOC , false ) ,
				EntityVar.metaStringVar( ReleaseTarget.PROPERTY_DOC , FIELD_TARGET_DOC_NAME , ReleaseTarget.PROPERTY_DOC , "doc name" , false , null ) ,
				EntityVar.metaStringVar( ReleaseTarget.PROPERTY_BUILDBRANCH , FIELD_TARGET_BRANCH , ReleaseTarget.PROPERTY_BUILDBRANCH , "build branch name" , false , null ) ,
				EntityVar.metaStringVar( ReleaseTarget.PROPERTY_BUILDTAG , FIELD_TARGET_TAG , ReleaseTarget.PROPERTY_BUILDTAG , "build tag name" , false , null ) ,
				EntityVar.metaStringVar( ReleaseTarget.PROPERTY_BUILDVERSION , FIELD_TARGET_VERSION , ReleaseTarget.PROPERTY_BUILDVERSION , "build version" , false , null ) ,
				EntityVar.metaStringVar( ReleaseTarget.PROPERTY_FILE , FIELD_TARGET_FILE , ReleaseTarget.PROPERTY_FILE , "file name" , false , null ) ,
				EntityVar.metaStringVar( ReleaseTarget.PROPERTY_FILE_HASH , FIELD_TARGET_FILE_HASH , ReleaseTarget.PROPERTY_FILE_HASH , "file hash" , false , null ) ,
				EntityVar.metaStringVar( ReleaseTarget.PROPERTY_FILE_SIZE , FIELD_TARGET_FILE_SIZE , ReleaseTarget.PROPERTY_FILE_SIZE , "file size" , false , null ) ,
				EntityVar.metaStringVar( ReleaseTarget.PROPERTY_FILE_TIME , FIELD_TARGET_FILE_TIME , ReleaseTarget.PROPERTY_FILE_TIME , "file time" , false , null ) ,
		} ) );
	}

	public static PropertyEntity loaddbEntityReleaseTarget( DBConnection c ) throws Exception {
		PropertyEntity entity = PropertyEntity.getAppObjectEntity( DBEnumObjectType.RELEASE_TARGET , DBEnumParamEntityType.RELEASE_TARGET , DBEnumObjectVersionType.RELEASE , TABLE_TARGET , FIELD_TARGET_ID );
		DBSettings.loaddbAppEntity( c , entity );
		return( entity );
	}
	
	public static PropertyEntity upgradeEntityReleaseScopeSet( EngineLoader loader ) throws Exception {
		DBConnection c = loader.getConnection();
		PropertyEntity entity = PropertyEntity.getAppObjectEntity( DBEnumObjectType.RELEASE_SCOPESET , DBEnumParamEntityType.RELEASE_SCOPESET , DBEnumObjectVersionType.RELEASE , TABLE_SCOPESET , FIELD_SCOPESET_ID );
		return( DBSettings.savedbObjectEntity( c , entity , new EntityVar[] { 
				EntityVar.metaObjectDatabaseOnly( FIELD_RELEASE_ID , "release id" , DBEnumObjectType.RELEASE_MAIN , true ) ,
				EntityVar.metaObjectDatabaseOnly( FIELD_SCOPESET_RELEASETARGET_ID , "release target id" , DBEnumObjectType.RELEASE_TARGET , true )
		} ) );
	}

	public static PropertyEntity loaddbEntityReleaseScopeSet( DBConnection c ) throws Exception {
		PropertyEntity entity = PropertyEntity.getAppObjectEntity( DBEnumObjectType.RELEASE_SCOPESET , DBEnumParamEntityType.RELEASE_SCOPESET , DBEnumObjectVersionType.RELEASE , TABLE_SCOPESET , FIELD_SCOPESET_ID );
		DBSettings.loaddbAppEntity( c , entity );
		return( entity );
	}
	
	public static PropertyEntity upgradeEntityReleaseScopeTarget( EngineLoader loader ) throws Exception {
		DBConnection c = loader.getConnection();
		PropertyEntity entity = PropertyEntity.getAppObjectEntity( DBEnumObjectType.RELEASE_SCOPETARGET , DBEnumParamEntityType.RELEASE_SCOPETARGET , DBEnumObjectVersionType.RELEASE , TABLE_SCOPETARGET , FIELD_SCOPETARGET_ID );
		return( DBSettings.savedbObjectEntity( c , entity , new EntityVar[] { 
				EntityVar.metaObjectDatabaseOnly( FIELD_RELEASE_ID , "release id" , DBEnumObjectType.RELEASE_MAIN , true ) ,
				EntityVar.metaObjectDatabaseOnly( FIELD_SCOPETARGET_RELEASETARGET_ID , "release target id" , DBEnumObjectType.RELEASE_TARGET , true ) ,
				EntityVar.metaObjectDatabaseOnly( FIELD_SCOPETARGET_SCOPESET_ID , "scope set id" , DBEnumObjectType.RELEASE_SCOPESET , true )
		} ) );
	}

	public static PropertyEntity loaddbEntityReleaseScopeTarget( DBConnection c ) throws Exception {
		PropertyEntity entity = PropertyEntity.getAppObjectEntity( DBEnumObjectType.RELEASE_SCOPETARGET , DBEnumParamEntityType.RELEASE_SCOPETARGET , DBEnumObjectVersionType.RELEASE , TABLE_SCOPETARGET , FIELD_SCOPETARGET_ID );
		DBSettings.loaddbAppEntity( c , entity );
		return( entity );
	}
	
	public static PropertyEntity upgradeEntityReleaseScopeItem( EngineLoader loader ) throws Exception {
		DBConnection c = loader.getConnection();
		PropertyEntity entity = PropertyEntity.getAppObjectEntity( DBEnumObjectType.RELEASE_SCOPEITEM , DBEnumParamEntityType.RELEASE_SCOPEITEM , DBEnumObjectVersionType.RELEASE , TABLE_SCOPEITEM , FIELD_SCOPEITEM_ID );
		return( DBSettings.savedbObjectEntity( c , entity , new EntityVar[] { 
				EntityVar.metaObjectDatabaseOnly( FIELD_RELEASE_ID , "release id" , DBEnumObjectType.RELEASE_MAIN , true ) ,
				EntityVar.metaObjectDatabaseOnly( FIELD_SCOPEITEM_SCOPETARGET_ID , "scope set id" , DBEnumObjectType.RELEASE_SCOPESET , true ) ,
				EntityVar.metaObjectDatabaseOnly( FIELD_SCOPEITEM_RELEASETARGET_ID , "release target id" , DBEnumObjectType.RELEASE_TARGET , true )
		} ) );
	}

	public static PropertyEntity loaddbEntityReleaseScopeItem( DBConnection c ) throws Exception {
		PropertyEntity entity = PropertyEntity.getAppObjectEntity( DBEnumObjectType.RELEASE_SCOPEITEM , DBEnumParamEntityType.RELEASE_SCOPEITEM , DBEnumObjectVersionType.RELEASE , TABLE_SCOPEITEM , FIELD_SCOPEITEM_ID );
		DBSettings.loaddbAppEntity( c , entity );
		return( entity );
	}
	
	public static PropertyEntity upgradeEntityReleaseSchedule( EngineLoader loader ) throws Exception {
		DBConnection c = loader.getConnection();
		PropertyEntity entity = PropertyEntity.getAppObjectEntity( DBEnumObjectType.RELEASE_SCHEDULE , DBEnumParamEntityType.RELEASE_SCHEDULE , DBEnumObjectVersionType.RELEASE , TABLE_SCHEDULE , FIELD_SCHEDULE_ID );
		return( DBSettings.savedbObjectEntity( c , entity , new EntityVar[] { 
				EntityVar.metaObjectDatabaseOnly( FIELD_RELEASE_ID , "release id" , DBEnumObjectType.RELEASE_MAIN , true ) ,
				EntityVar.metaDateVar( ReleaseSchedule.PROPERTY_STARTED , FIELD_SCHEDULE_STARTED , ReleaseSchedule.PROPERTY_STARTED , "start date" , true ) ,
				EntityVar.metaDateVar( ReleaseSchedule.PROPERTY_RELEASEDATE , FIELD_SCHEDULE_RELEASEDATE , ReleaseSchedule.PROPERTY_RELEASEDATE , "release date scheduled" , true ) ,
				EntityVar.metaDateVar( ReleaseSchedule.PROPERTY_RELEASEDATEACTUAL , FIELD_SCHEDULE_RELEASEDATEACTUAL , ReleaseSchedule.PROPERTY_RELEASEDATEACTUAL , "release date actual" , false ) ,
 				EntityVar.metaDateVar( ReleaseSchedule.PROPERTY_COMPLETEDATEACTUAL , FIELD_SCHEDULE_COMPLETEDATEACTUAL , ReleaseSchedule.PROPERTY_COMPLETEDATEACTUAL , "complete date actual" , false ) ,
				EntityVar.metaBoolean( ReleaseSchedule.PROPERTY_RELEASEDSTATUS , "released status" , true , false ) ,
				EntityVar.metaBoolean( ReleaseSchedule.PROPERTY_COMPLETEDSTATUS , "completed status" , true , false ) ,
				EntityVar.metaIntegerVar( ReleaseSchedule.PROPERTY_PHASE , FIELD_SCHEDULE_PHASE , ReleaseSchedule.PROPERTY_PHASE , "current phase" , false , null ) ,
		} ) );
	}

	public static PropertyEntity loaddbEntityReleaseSchedule( DBConnection c ) throws Exception {
		PropertyEntity entity = PropertyEntity.getAppObjectEntity( DBEnumObjectType.RELEASE_SCHEDULE , DBEnumParamEntityType.RELEASE_SCHEDULE , DBEnumObjectVersionType.RELEASE , TABLE_SCHEDULE , FIELD_SCHEDULE_ID );
		DBSettings.loaddbAppEntity( c , entity );
		return( entity );
	}
	
	public static PropertyEntity upgradeEntityReleasePhase( EngineLoader loader ) throws Exception {
		DBConnection c = loader.getConnection();
		PropertyEntity entity = PropertyEntity.getAppObjectEntity( DBEnumObjectType.RELEASE_PHASE , DBEnumParamEntityType.RELEASE_PHASE , DBEnumObjectVersionType.RELEASE , TABLE_PHASE , FIELD_PHASE_ID );
		return( DBSettings.savedbObjectEntity( c , entity , new EntityVar[] { 
				EntityVar.metaObjectDatabaseOnly( FIELD_RELEASE_ID , "release id" , DBEnumObjectType.RELEASE_MAIN , true ) ,
				EntityVar.metaEnumVar( ReleaseSchedulePhase.PROPERTY_RELEASESTAGE , FIELD_PHASE_LCSTAGETYPE , ReleaseSchedulePhase.PROPERTY_RELEASESTAGE , "release phase stage type" , true , DBEnumLifecycleStageType.UNKNOWN ) ,
				EntityVar.metaIntegerVar( ReleaseSchedulePhase.PROPERTY_STAGEPOS , FIELD_PHASE_STAGEPOS , ReleaseSchedulePhase.PROPERTY_STAGEPOS , "release phase stage position" , true , null ) ,
				EntityVar.metaString( ReleaseSchedulePhase.PROPERTY_NAME , "product name" , true , null ) ,
				EntityVar.metaStringVar( ReleaseSchedulePhase.PROPERTY_DESC , FIELD_PHASE_DESC , ReleaseSchedulePhase.PROPERTY_DESC , "Description" , false , null ) ,
				EntityVar.metaInteger( ReleaseSchedulePhase.PROPERTY_DAYS , "phase days" , true , null ) ,
				EntityVar.metaIntegerVar( ReleaseSchedulePhase.PROPERTY_NORMALDAYS , FIELD_PHASE_NORMALDAYS , ReleaseSchedulePhase.PROPERTY_NORMALDAYS , "phase days" , true , null ) ,
				EntityVar.metaBoolean( ReleaseSchedulePhase.PROPERTY_FINISHED , "finished status" , true , false ) ,
				EntityVar.metaBoolean( ReleaseSchedulePhase.PROPERTY_UNLIMITED , "unlimited status" , true , false ) ,
				EntityVar.metaDateVar( ReleaseSchedulePhase.PROPERTY_STARTDATE , FIELD_PHASE_STARTDATE , ReleaseSchedulePhase.PROPERTY_STARTDATE , "actual start date" , false ) ,
				EntityVar.metaDateVar( ReleaseSchedulePhase.PROPERTY_FINISHDATE , FIELD_PHASE_FINISHDATE , ReleaseSchedulePhase.PROPERTY_FINISHDATE , "actual finish date" , false )
		} ) );
	}

	public static PropertyEntity loaddbEntityReleasePhase( DBConnection c ) throws Exception {
		PropertyEntity entity = PropertyEntity.getAppObjectEntity( DBEnumObjectType.RELEASE_PHASE , DBEnumParamEntityType.RELEASE_PHASE , DBEnumObjectVersionType.RELEASE , TABLE_PHASE , FIELD_PHASE_ID );
		DBSettings.loaddbAppEntity( c , entity );
		return( entity );
	}
	
	public static PropertyEntity upgradeEntityReleaseTicketSet( EngineLoader loader ) throws Exception {
		DBConnection c = loader.getConnection();
		PropertyEntity entity = PropertyEntity.getAppObjectEntity( DBEnumObjectType.RELEASE_TICKETSET , DBEnumParamEntityType.RELEASE_TICKETSET , DBEnumObjectVersionType.RELEASE , TABLE_TICKETSET , FIELD_TICKETSET_ID );
		return( DBSettings.savedbObjectEntity( c , entity , new EntityVar[] { 
				EntityVar.metaObjectDatabaseOnly( FIELD_RELEASE_ID , "release id" , DBEnumObjectType.RELEASE_MAIN , true ) ,
				EntityVar.metaString( ReleaseTicketSet.PROPERTY_CODE , "ticket set code" , true , null ) ,
				EntityVar.metaString( ReleaseTicketSet.PROPERTY_NAME , "ticket set name" , true , null ) ,
				EntityVar.metaStringVar( ReleaseTicketSet.PROPERTY_DESC , FIELD_TICKETSET_DESC , ReleaseTicketSet.PROPERTY_DESC , "Description" , false , null ) ,
				EntityVar.metaEnumVar( ReleaseTicketSet.PROPERTY_STATUS , FIELD_TICKETSET_STATUS , ReleaseTicketSet.PROPERTY_STATUS , "ticket set status type" , true , DBEnumTicketSetStatusType.UNKNOWN )
		} ) );
	}

	public static PropertyEntity loaddbEntityReleaseTicketSet( DBConnection c ) throws Exception {
		PropertyEntity entity = PropertyEntity.getAppObjectEntity( DBEnumObjectType.RELEASE_TICKETSET , DBEnumParamEntityType.RELEASE_TICKETSET , DBEnumObjectVersionType.RELEASE , TABLE_TICKETSET , FIELD_TICKETSET_ID );
		DBSettings.loaddbAppEntity( c , entity );
		return( entity );
	}
	
	public static PropertyEntity upgradeEntityReleaseTicketTarget( EngineLoader loader ) throws Exception {
		DBConnection c = loader.getConnection();
		PropertyEntity entity = PropertyEntity.getAppObjectEntity( DBEnumObjectType.RELEASE_TICKETTARGET , DBEnumParamEntityType.RELEASE_TICKETTARGET , DBEnumObjectVersionType.RELEASE , TABLE_TICKETTARGET , FIELD_TICKETTARGET_ID );
		return( DBSettings.savedbObjectEntity( c , entity , new EntityVar[] { 
				EntityVar.metaObjectDatabaseOnly( FIELD_RELEASE_ID , "release id" , DBEnumObjectType.RELEASE_MAIN , true ) ,
				EntityVar.metaObjectDatabaseOnly( FIELD_TICKETTARGET_TICKETSET_ID , "ticket set id" , DBEnumObjectType.RELEASE_TICKETSET , true ) ,
				EntityVar.metaInteger( ReleaseTicketSetTarget.PROPERTY_POS , "ticket target position" , true , null ) ,
				EntityVar.metaObjectDatabaseOnly( FIELD_TICKETTARGET_RELEASETARGET_ID , "ticket set target id" , DBEnumObjectType.RELEASE_TARGET , true )
		} ) );
	}

	public static PropertyEntity loaddbEntityReleaseTicketTarget( DBConnection c ) throws Exception {
		PropertyEntity entity = PropertyEntity.getAppObjectEntity( DBEnumObjectType.RELEASE_TICKETTARGET , DBEnumParamEntityType.RELEASE_TICKETTARGET , DBEnumObjectVersionType.RELEASE , TABLE_TICKETTARGET , FIELD_TICKETTARGET_ID );
		DBSettings.loaddbAppEntity( c , entity );
		return( entity );
	}
	
	public static PropertyEntity upgradeEntityReleaseTicket( EngineLoader loader ) throws Exception {
		DBConnection c = loader.getConnection();
		PropertyEntity entity = PropertyEntity.getAppObjectEntity( DBEnumObjectType.RELEASE_TICKET , DBEnumParamEntityType.RELEASE_TICKET , DBEnumObjectVersionType.RELEASE , TABLE_TICKET , FIELD_TICKET_ID );
		return( DBSettings.savedbObjectEntity( c , entity , new EntityVar[] { 
				EntityVar.metaObjectDatabaseOnly( FIELD_RELEASE_ID , "release id" , DBEnumObjectType.RELEASE_MAIN , true ) ,
				EntityVar.metaObjectDatabaseOnly( FIELD_TICKET_TICKETSET_ID , "ticket set id" , DBEnumObjectType.RELEASE_TICKETSET , true ) ,
				EntityVar.metaInteger( ReleaseTicketSetTarget.PROPERTY_POS , "ticket position" , true , null ) ,
				EntityVar.metaString( ReleaseTicket.PROPERTY_CODE , "ticket code" , true , null ) ,
				EntityVar.metaString( ReleaseTicket.PROPERTY_NAME , "ticket name" , true , null ) ,
				EntityVar.metaStringVar( ReleaseTicket.PROPERTY_DESC , FIELD_TICKET_DESC , ReleaseTicket.PROPERTY_DESC , "Description" , false , null ) ,
				EntityVar.metaString( ReleaseTicket.PROPERTY_LINK , "ticket link reference" , false , null ) ,
				EntityVar.metaEnumVar( ReleaseTicket.PROPERTY_TYPE , FIELD_TICKET_TYPE , ReleaseTicket.PROPERTY_TYPE , "ticket type" , true , DBEnumTicketType.UNKNOWN ) ,
				EntityVar.metaEnumVar( ReleaseTicket.PROPERTY_STATUS , FIELD_TICKET_STATUS , ReleaseTicket.PROPERTY_STATUS , "ticket status" , true , DBEnumTicketStatusType.UNKNOWN ) ,
				EntityVar.metaBoolean( ReleaseTicket.PROPERTY_ACTIVE , "active status" , true , false ) ,
				EntityVar.metaBoolean( ReleaseTicket.PROPERTY_ACCEPTED , "accepted status" , true , false ) ,
				EntityVar.metaBoolean( ReleaseTicket.PROPERTY_DESCOPED , "descoped status" , true , false ) ,
				EntityVar.metaObjectDatabaseOnly( FIELD_TICKET_OWNER_ID , "owner user id" , DBEnumObjectType.AUTH_USER , false ) ,
				EntityVar.metaStringVar( ReleaseTicket.PROPERTY_OWNER , FIELD_TICKET_OWNER_NAME , ReleaseTicket.PROPERTY_OWNER , "owner user name" , false , null ) ,
				EntityVar.metaObjectDatabaseOnly( FIELD_TICKET_DEVUSER_ID , "developer user id" , DBEnumObjectType.AUTH_USER , false ) ,
				EntityVar.metaStringVar( ReleaseTicket.PROPERTY_DEV , FIELD_TICKET_DEVUSER_NAME , ReleaseTicket.PROPERTY_DEV , "developer user name" , false , null ) ,
				EntityVar.metaObjectDatabaseOnly( FIELD_TICKET_QAUSER_ID , "qa user id" , DBEnumObjectType.AUTH_USER , false ) ,
				EntityVar.metaStringVar( ReleaseTicket.PROPERTY_QA , FIELD_TICKET_QAUSER_NAME , ReleaseTicket.PROPERTY_QA , "qa user name" , false , null ) ,
		} ) );
	}

	public static PropertyEntity loaddbEntityReleaseTicket( DBConnection c ) throws Exception {
		PropertyEntity entity = PropertyEntity.getAppObjectEntity( DBEnumObjectType.RELEASE_TICKET , DBEnumParamEntityType.RELEASE_TICKET , DBEnumObjectVersionType.RELEASE , TABLE_TICKET , FIELD_TICKET_ID );
		DBSettings.loaddbAppEntity( c , entity );
		return( entity );
	}
	
	public static void dropAllMeta( EngineLoader loader , ProductReleases releases ) throws Exception {
		int metaId = releases.meta.getId();
		dropReleaseTickets( loader , metaId );
		dropReleaseSchedule( loader , metaId );
		dropReleaseCore( loader , metaId );
	}

	private static void dropReleaseCore( EngineLoader loader , int metaId ) throws Exception {
		DBConnection c = loader.getConnection();
		EngineEntities entities = loader.getEntities();
		DBEngineEntities.dropAppObjects( c , entities.entityAppReleaseScopeItem , DBQueries.FILTER_REL_META1 , new String[] { EngineDB.getInteger( metaId ) } );
		DBEngineEntities.dropAppObjects( c , entities.entityAppReleaseScopeTarget , DBQueries.FILTER_REL_META1 , new String[] { EngineDB.getInteger( metaId ) } );
		DBEngineEntities.dropAppObjects( c , entities.entityAppReleaseScopeSet , DBQueries.FILTER_REL_META1 , new String[] { EngineDB.getInteger( metaId ) } );
		DBEngineEntities.dropAppObjects( c , entities.entityAppReleaseTarget , DBQueries.FILTER_REL_META1 , new String[] { EngineDB.getInteger( metaId ) } );
		DBEngineEntities.dropAppObjects( c , entities.entityAppReleaseDist , DBQueries.FILTER_REL_META1 , new String[] { EngineDB.getInteger( metaId ) } );
		DBEngineEntities.dropAppObjects( c , entities.entityAppReleaseMain , DBQueries.FILTER_REL_MAINMETA1 , new String[] { EngineDB.getInteger( metaId ) } );
		DBEngineEntities.dropAppObjects( c , entities.entityAppReleaseRepository , DBQueries.FILTER_REL_REPOMETA1 , new String[] { EngineDB.getInteger( metaId ) } );
	}
	
	private static void dropReleaseSchedule( EngineLoader loader , int metaId ) throws Exception {
		DBConnection c = loader.getConnection();
		EngineEntities entities = loader.getEntities();
		DBEngineEntities.dropAppObjects( c , entities.entityAppReleasePhase , DBQueries.FILTER_REL_META1 , new String[] { EngineDB.getInteger( metaId ) } );
		DBEngineEntities.dropAppObjects( c , entities.entityAppReleaseSchedule , DBQueries.FILTER_REL_META1 , new String[] { EngineDB.getInteger( metaId ) } );
	}
	
	private static void dropReleaseTickets( EngineLoader loader , int metaId ) throws Exception {
		DBConnection c = loader.getConnection();
		EngineEntities entities = loader.getEntities();
		DBEngineEntities.dropAppObjects( c , entities.entityAppReleaseTicketTarget , DBQueries.FILTER_REL_META1 , new String[] { EngineDB.getInteger( metaId ) } );
		DBEngineEntities.dropAppObjects( c , entities.entityAppReleaseTicket , DBQueries.FILTER_REL_META1 , new String[] { EngineDB.getInteger( metaId ) } );
		DBEngineEntities.dropAppObjects( c , entities.entityAppReleaseTicketSet , DBQueries.FILTER_REL_META1 , new String[] { EngineDB.getInteger( metaId ) } );
	}
	
}
