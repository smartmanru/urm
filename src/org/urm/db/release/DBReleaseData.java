package org.urm.db.release;

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
import org.urm.meta.loader.EngineLoader;
import org.urm.meta.product.ProductMeta;
import org.urm.meta.release.Release;
import org.urm.meta.release.ReleaseBuildTarget;
import org.urm.meta.release.ReleaseDist;
import org.urm.meta.release.ReleaseDistItem;
import org.urm.meta.release.ReleaseSchedule;
import org.urm.meta.release.ReleaseSchedulePhase;
import org.urm.meta.release.ReleaseDistTarget;
import org.urm.meta.release.ReleaseRepository;
import org.urm.meta.release.ReleaseTicket;
import org.urm.meta.release.ReleaseTicketSet;
import org.urm.meta.release.ReleaseTicketTarget;

public class DBReleaseData {

	public static String TABLE_REPOSITORY = "urm_rel_repository";
	public static String TABLE_MAIN = "urm_rel_main";
	public static String TABLE_DIST = "urm_rel_dist";
	public static String TABLE_BUILDTARGET = "urm_rel_buildtarget";
	public static String TABLE_DISTTARGET = "urm_rel_disttarget";
	public static String TABLE_SCOPESET = "urm_rel_scopeset";
	public static String TABLE_SCOPETARGET = "urm_rel_scopetarget";
	public static String TABLE_SCOPEITEM = "urm_rel_scopeitem";
	public static String TABLE_SCHEDULE = "urm_rel_schedule";
	public static String TABLE_PHASE = "urm_rel_phase";
	public static String TABLE_TICKETSET = "urm_rel_ticketset";
	public static String TABLE_TICKETTARGET = "urm_rel_tickettarget";
	public static String TABLE_TICKET = "urm_rel_ticket";
	public static String TABLE_DISTITEM = "urm_rel_distitem";
	public static String FIELD_RELEASE_ID = "release_id";
	public static String FIELD_REPOSITORY_ID = "repo_id";
	public static String FIELD_REPOSITORY_NAME = "name";
	public static String FIELD_REPOSITORY_DESC = "xdesc";
	public static String FIELD_REPOSITORY_META_ID = "meta_id";
	public static String FIELD_MAIN_ID = "release_id";
	public static String FIELD_MAIN_REPO_ID = "repo_id";
	public static String FIELD_MAIN_TRANSITION_REPO_ID = "transition_repo_id";
	public static String FIELD_MAIN_DESC = "xdesc";
	public static String FIELD_MAIN_LIFECYCLETYPE = "lifecycle_type";
	public static String FIELD_MAIN_V1 = "v1";
	public static String FIELD_MAIN_V2 = "v2";
	public static String FIELD_MAIN_V3 = "v3";
	public static String FIELD_MAIN_V4 = "v4";
	public static String FIELD_MAIN_VERSION = "releasever";
	public static String FIELD_MAIN_BUILDMODE = "buildmode_type";
	public static String FIELD_MAIN_COMPATIBILITY = "compatibility";
	public static String FIELD_DIST_ID = "dist_id";
	public static String FIELD_DIST_DATAHASH = "data_hash";
	public static String FIELD_DIST_METAHASH = "meta_hash";
	public static String FIELD_DIST_DATE = "dist_date";
	public static String FIELD_DIST_VARIANT = "dist_variant";
	public static String FIELD_BUILDTARGET_ID = "buildtarget_id";
	public static String FIELD_BUILDTARGET_TARGETTYPE = "buildtarget_type";
	public static String FIELD_BUILDTARGET_SCOPEALL = "scope_all";
	public static String FIELD_BUILDTARGET_SRCSET_ID = "srcset_fkid";
	public static String FIELD_BUILDTARGET_SRCSET_NAME = "srcset_fkname";
	public static String FIELD_BUILDTARGET_PROJECT_ID = "project_fkid";
	public static String FIELD_BUILDTARGET_PROJECT_NAME = "project_fkname";
	public static String FIELD_BUILDTARGET_BRANCH = "build_branch";
	public static String FIELD_BUILDTARGET_TAG = "build_tag";
	public static String FIELD_BUILDTARGET_VERSION = "build_version";
	public static String FIELD_DISTTARGET_ID = "disttarget_id";
	public static String FIELD_DISTTARGET_TARGETTYPE = "disttarget_type";
	public static String FIELD_DISTTARGET_SCOPEALL = "scope_all";
	public static String FIELD_DISTTARGET_DELIVERY_ID = "delivery_fkid";
	public static String FIELD_DISTTARGET_DELIVERY_NAME = "delivery_fkname";
	public static String FIELD_DISTTARGET_BINARY_ID = "binary_fkid";
	public static String FIELD_DISTTARGET_BINARY_NAME = "binary_fkname";
	public static String FIELD_DISTTARGET_CONF_ID = "confitem_fkid";
	public static String FIELD_DISTTARGET_CONF_NAME = "confitem_fkname";
	public static String FIELD_DISTTARGET_SCHEMA_ID = "schema_fkid";
	public static String FIELD_DISTTARGET_SCHEMA_NAME = "schema_fkname";
	public static String FIELD_DISTTARGET_DOC_ID = "doc_fkid";
	public static String FIELD_DISTTARGET_DOC_NAME = "doc_fkname";
	public static String FIELD_SCHEDULE_ID = "release_id";
	public static String FIELD_SCHEDULE_STARTED = "start_date_actual";
	public static String FIELD_SCHEDULE_RELEASEDATE = "release_date_scheduled";
	public static String FIELD_SCHEDULE_RELEASEDATEACTUAL = "release_date_actual";
	public static String FIELD_SCHEDULE_COMPLETEDATEACTUAL = "complete_date_actual";
	public static String FIELD_SCHEDULE_PHASE = "phase_current";
	public static String FIELD_SCHEDULE_NAME = "schedule_name";
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
	public static String FIELD_TICKETTARGET_BUILDTARGET_ID = "buildtarget_id";
	public static String FIELD_TICKETTARGET_DISTTARGET_ID = "disttarget_id";
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
	public static String FIELD_DISTITEM_ID = "distitem_id";
	public static String FIELD_DISTITEM_DISTTARGET_ID = "disttarget_id";
	public static String FIELD_DISTITEM_DIST_ID = "dist_id";
	public static String FIELD_DISTITEM_FILE = "targetfile";
	public static String FIELD_DISTITEM_FILE_FOLDER = "targetfile_folder";
	public static String FIELD_DISTITEM_FILE_HASH = "targetfile_hash";
	public static String FIELD_DISTITEM_FILE_SIZE = "targetfile_size";
	public static String FIELD_DISTITEM_FILE_TIME = "targetfile_time";
	public static String FIELD_DISTITEM_SOURCE_RELEASEDIR = "source_releasedir";
	public static String FIELD_DISTITEM_SOURCE_RELEASETIME = "source_releasetime";
	
	public static PropertyEntity makeEntityReleaseRepository( DBConnection c , boolean upgrade ) throws Exception {
		PropertyEntity entity = PropertyEntity.getAppObjectEntity( DBEnumObjectType.RELEASE_REPOSITORY , DBEnumParamEntityType.RELEASE_REPOSITORY , DBEnumObjectVersionType.APP , TABLE_REPOSITORY , FIELD_REPOSITORY_ID , false );
		if( !upgrade ) {
			DBSettings.loaddbAppEntity( c , entity );
			return( entity );
		}
		
		return( DBSettings.savedbObjectEntity( c , entity , new EntityVar[] { 
				EntityVar.metaString( FIELD_REPOSITORY_NAME , "repository name" , true , null ) ,
				EntityVar.metaString( FIELD_REPOSITORY_DESC , "repository description" , false , null ) ,
				EntityVar.metaObjectDatabaseOnly( FIELD_REPOSITORY_META_ID , "meta id" , DBEnumObjectType.META , false )
		} ) );
	}

	public static PropertyEntity makeEntityReleaseMain( DBConnection c , boolean upgrade ) throws Exception {
		PropertyEntity entity = PropertyEntity.getAppObjectEntity( DBEnumObjectType.RELEASE_MAIN , DBEnumParamEntityType.RELEASE_MAIN , DBEnumObjectVersionType.RELEASE , TABLE_MAIN , FIELD_MAIN_ID , false );
		if( !upgrade ) {
			DBSettings.loaddbAppEntity( c , entity );
			return( entity );
		}
		
		return( DBSettings.savedbObjectEntity( c , entity , new EntityVar[] { 
				EntityVar.metaObjectDatabaseOnly( FIELD_MAIN_REPO_ID , "repo id" , DBEnumObjectType.RELEASE_REPOSITORY , true ) ,
				EntityVar.metaObjectDatabaseOnly( FIELD_MAIN_TRANSITION_REPO_ID , "transition repo id" , DBEnumObjectType.RELEASE_REPOSITORY , false ) ,
				EntityVar.metaString( Release.PROPERTY_NAME , "release name" , true , null ) ,
				EntityVar.metaStringVar( Release.PROPERTY_DESC , FIELD_MAIN_DESC , "Description" , false , null ) ,
				EntityVar.metaBoolean( Release.PROPERTY_MASTER , "master" , true , false ) ,
				EntityVar.metaEnumVar( Release.PROPERTY_LIFECYCLETYPE , FIELD_MAIN_LIFECYCLETYPE , "Lifecycle type" , true , DBEnumLifecycleType.UNKNOWN ) ,
				EntityVar.metaIntegerDatabaseOnly( FIELD_MAIN_V1 , "version number 1" , true , null ) ,
				EntityVar.metaIntegerDatabaseOnly( FIELD_MAIN_V2 , "version number 2" , true , null ) ,
				EntityVar.metaIntegerDatabaseOnly( FIELD_MAIN_V3 , "version number 3" , true , null ) ,
				EntityVar.metaIntegerDatabaseOnly( FIELD_MAIN_V4 , "version number 4" , true , null ) ,
				EntityVar.metaStringVar( Release.PROPERTY_VERSION , FIELD_MAIN_VERSION , "release version" , true , null ) ,
				EntityVar.metaEnumVar( Release.PROPERTY_BUILDMODE , FIELD_MAIN_BUILDMODE , "Build mode type" , true , DBEnumBuildModeType.UNKNOWN ) ,
				EntityVar.metaStringVar( Release.PROPERTY_COMPATIBILITY , FIELD_MAIN_COMPATIBILITY , "release compatibility" , false , null ) ,
				EntityVar.metaBoolean( Release.PROPERTY_CUMULATIVE , "cumulative" , true , false ) ,
				EntityVar.metaBoolean( Release.PROPERTY_ARCHIVED , "archived" , true , false ) ,
				EntityVar.metaBoolean( Release.PROPERTY_CANCELLED , "cancelled" , true , false )
		} ) );
	}

	public static PropertyEntity makeEntityReleaseDist( DBConnection c , boolean upgrade ) throws Exception {
		PropertyEntity entity = PropertyEntity.getAppObjectEntity( DBEnumObjectType.RELEASE_DIST , DBEnumParamEntityType.RELEASE_DIST , DBEnumObjectVersionType.RELEASE , TABLE_DIST , FIELD_DIST_ID , false );
		if( !upgrade ) {
			DBSettings.loaddbAppEntity( c , entity );
			return( entity );
		}
		
		return( DBSettings.savedbObjectEntity( c , entity , new EntityVar[] { 
				EntityVar.metaObjectDatabaseOnly( FIELD_RELEASE_ID , "release id" , DBEnumObjectType.RELEASE_MAIN , true ) ,
				EntityVar.metaStringVar( ReleaseDist.PROPERTY_VARIANT , FIELD_DIST_VARIANT , "distributive variant" , false , null ) ,
				EntityVar.metaDateDatabaseOnly( FIELD_DIST_DATE , "data date" , false ) ,
				EntityVar.metaStringDatabaseOnly( FIELD_DIST_METAHASH , "meta hash" , false , null ) ,
				EntityVar.metaStringDatabaseOnly( FIELD_DIST_DATAHASH , "data hash" , false , null )
		} ) );
	}

	public static PropertyEntity makeEntityReleaseBuildTarget( DBConnection c , boolean upgrade ) throws Exception {
		PropertyEntity entity = PropertyEntity.getAppObjectEntity( DBEnumObjectType.RELEASE_BUILDTARGET , DBEnumParamEntityType.RELEASE_BUILDTARGET , DBEnumObjectVersionType.RELEASE , TABLE_BUILDTARGET , FIELD_BUILDTARGET_ID , false );
		if( !upgrade ) {
			DBSettings.loaddbAppEntity( c , entity );
			return( entity );
		}
		
		return( DBSettings.savedbObjectEntity( c , entity , new EntityVar[] { 
				EntityVar.metaObjectDatabaseOnly( FIELD_RELEASE_ID , "release id" , DBEnumObjectType.RELEASE_MAIN , true ) ,
				EntityVar.metaBooleanDatabaseOnly( ReleaseBuildTarget.PROPERTY_SCOPETARGET , "scope target" , true , false ) ,
				EntityVar.metaEnumVar( ReleaseBuildTarget.PROPERTY_TARGETTYPE , FIELD_BUILDTARGET_TARGETTYPE , "release target type" , true , DBEnumBuildTargetType.UNKNOWN ) ,
				EntityVar.metaBooleanVar( ReleaseBuildTarget.PROPERTY_ALL , FIELD_BUILDTARGET_SCOPEALL , "all scope" , false , false ) ,
				EntityVar.metaObjectDatabaseOnly( FIELD_BUILDTARGET_SRCSET_ID , "source project set id" , DBEnumObjectType.META_SOURCESET , false ) ,
				EntityVar.metaStringVar( ReleaseBuildTarget.PROPERTY_SRCSET , FIELD_BUILDTARGET_SRCSET_NAME , "source project set name" , false , null ) ,
				EntityVar.metaObjectDatabaseOnly( FIELD_BUILDTARGET_PROJECT_ID , "source project id" , DBEnumObjectType.META_SOURCEPROJECT , false ) ,
				EntityVar.metaStringVar( ReleaseBuildTarget.PROPERTY_PROJECT , FIELD_BUILDTARGET_PROJECT_NAME , "source project name" , false , null ) ,
				EntityVar.metaStringVar( ReleaseBuildTarget.PROPERTY_BUILDBRANCH , FIELD_BUILDTARGET_BRANCH , "build branch name" , false , null ) ,
				EntityVar.metaStringVar( ReleaseBuildTarget.PROPERTY_BUILDTAG , FIELD_BUILDTARGET_TAG , "build tag name" , false , null ) ,
				EntityVar.metaStringVar( ReleaseBuildTarget.PROPERTY_BUILDVERSION , FIELD_BUILDTARGET_VERSION , "build version" , false , null )
		} ) );
	}

	public static PropertyEntity makeEntityReleaseDistTarget( DBConnection c , boolean upgrade ) throws Exception {
		PropertyEntity entity = PropertyEntity.getAppObjectEntity( DBEnumObjectType.RELEASE_DISTTARGET , DBEnumParamEntityType.RELEASE_DISTTARGET , DBEnumObjectVersionType.RELEASE , TABLE_DISTTARGET , FIELD_DISTTARGET_ID , false );
		if( !upgrade ) {
			DBSettings.loaddbAppEntity( c , entity );
			return( entity );
		}
		
		return( DBSettings.savedbObjectEntity( c , entity , new EntityVar[] { 
				EntityVar.metaObjectDatabaseOnly( FIELD_RELEASE_ID , "release id" , DBEnumObjectType.RELEASE_MAIN , true ) ,
				EntityVar.metaBooleanDatabaseOnly( ReleaseDistTarget.PROPERTY_SCOPETARGET , "scope target" , true , false ) ,
				EntityVar.metaEnumVar( ReleaseDistTarget.PROPERTY_TARGETTYPE , FIELD_DISTTARGET_TARGETTYPE , "release target type" , true , DBEnumDistTargetType.UNKNOWN ) ,
				EntityVar.metaBooleanVar( ReleaseDistTarget.PROPERTY_ALL , FIELD_DISTTARGET_SCOPEALL , "all scope" , false , false ) ,
				EntityVar.metaObjectDatabaseOnly( FIELD_DISTTARGET_DELIVERY_ID , "delivery id" , DBEnumObjectType.META_DIST_DELIVERY , false ) ,
				EntityVar.metaStringVar( ReleaseDistTarget.PROPERTY_DELIVERY , FIELD_DISTTARGET_DELIVERY_NAME , "delivery name" , false , null ) ,
				EntityVar.metaObjectDatabaseOnly( FIELD_DISTTARGET_BINARY_ID , "binary item id" , DBEnumObjectType.META_DIST_BINARYITEM , false ) ,
				EntityVar.metaStringVar( ReleaseDistTarget.PROPERTY_BINARY , FIELD_DISTTARGET_BINARY_NAME , "binary item name" , false , null ) ,
				EntityVar.metaObjectDatabaseOnly( FIELD_DISTTARGET_CONF_ID , "conf item id" , DBEnumObjectType.META_DIST_CONFITEM , false ) ,
				EntityVar.metaStringVar( ReleaseDistTarget.PROPERTY_CONF , FIELD_DISTTARGET_CONF_NAME , "conf item name" , false , null ) ,
				EntityVar.metaObjectDatabaseOnly( FIELD_DISTTARGET_SCHEMA_ID , "schema id" , DBEnumObjectType.META_SCHEMA , false ) ,
				EntityVar.metaStringVar( ReleaseDistTarget.PROPERTY_SCHEMA , FIELD_DISTTARGET_SCHEMA_NAME , "schema name" , false , null ) ,
				EntityVar.metaObjectDatabaseOnly( FIELD_DISTTARGET_DOC_ID , "doc id" , DBEnumObjectType.META_DOC , false ) ,
				EntityVar.metaStringVar( ReleaseDistTarget.PROPERTY_DOC , FIELD_DISTTARGET_DOC_NAME , "doc name" , false , null ) ,
		} ) );
	}

	public static PropertyEntity makeEntityReleaseDistItem( DBConnection c , boolean upgrade ) throws Exception {
		PropertyEntity entity = PropertyEntity.getAppObjectEntity( DBEnumObjectType.RELEASE_DISTITEM , DBEnumParamEntityType.RELEASE_DISTITEM , DBEnumObjectVersionType.RELEASE , TABLE_DISTITEM , FIELD_DISTITEM_ID , false );
		if( !upgrade ) {
			DBSettings.loaddbAppEntity( c , entity );
			return( entity );
		}
		
		return( DBSettings.savedbObjectEntity( c , entity , new EntityVar[] { 
				EntityVar.metaObjectDatabaseOnly( FIELD_RELEASE_ID , "release id" , DBEnumObjectType.RELEASE_MAIN , true ) ,
				EntityVar.metaObjectDatabaseOnly( FIELD_DISTITEM_DIST_ID , "release distributive id" , DBEnumObjectType.RELEASE_DIST , true ) ,
				EntityVar.metaObjectDatabaseOnly( FIELD_DISTITEM_DISTTARGET_ID , "release target id" , DBEnumObjectType.RELEASE_DISTTARGET , true ) ,
				EntityVar.metaStringVar( ReleaseDistItem.PROPERTY_FILE , FIELD_DISTITEM_FILE , "file name" , false , null ) ,
				EntityVar.metaStringVar( ReleaseDistItem.PROPERTY_FILE_FOLDER , FIELD_DISTITEM_FILE_FOLDER , "file path" , false , null ) ,
				EntityVar.metaStringVar( ReleaseDistItem.PROPERTY_FILE_HASH , FIELD_DISTITEM_FILE_HASH , "file hash" , false , null ) ,
				EntityVar.metaStringVar( ReleaseDistItem.PROPERTY_FILE_SIZE , FIELD_DISTITEM_FILE_SIZE , "file size" , false , null ) ,
				EntityVar.metaStringVar( ReleaseDistItem.PROPERTY_FILE_TIME , FIELD_DISTITEM_FILE_TIME , "file time" , false , null ) ,
				EntityVar.metaString( FIELD_DISTITEM_SOURCE_RELEASEDIR , "source release distributive version" , false , null ) ,
				EntityVar.metaDate( FIELD_DISTITEM_SOURCE_RELEASETIME , "source release distributive time" , false )
		} ) );
	}

	public static PropertyEntity makeEntityReleaseSchedule( DBConnection c , boolean upgrade ) throws Exception {
		PropertyEntity entity = PropertyEntity.getAppObjectEntity( DBEnumObjectType.RELEASE_SCHEDULE , DBEnumParamEntityType.RELEASE_SCHEDULE , DBEnumObjectVersionType.RELEASE , TABLE_SCHEDULE , FIELD_SCHEDULE_ID , false );
		if( !upgrade ) {
			DBSettings.loaddbAppEntity( c , entity );
			return( entity );
		}
		
		return( DBSettings.savedbObjectEntity( c , entity , new EntityVar[] { 
				EntityVar.metaDateVar( ReleaseSchedule.PROPERTY_STARTED , FIELD_SCHEDULE_STARTED , "start date" , true ) ,
				EntityVar.metaDateVar( ReleaseSchedule.PROPERTY_RELEASEDATE , FIELD_SCHEDULE_RELEASEDATE , "release date scheduled" , true ) ,
				EntityVar.metaDateVar( ReleaseSchedule.PROPERTY_RELEASEDATEACTUAL , FIELD_SCHEDULE_RELEASEDATEACTUAL , "release date actual" , false ) ,
 				EntityVar.metaDateVar( ReleaseSchedule.PROPERTY_COMPLETEDATEACTUAL , FIELD_SCHEDULE_COMPLETEDATEACTUAL , "complete date actual" , false ) ,
				EntityVar.metaBoolean( ReleaseSchedule.PROPERTY_RELEASEDSTATUS , "released status" , true , false ) ,
				EntityVar.metaBoolean( ReleaseSchedule.PROPERTY_COMPLETEDSTATUS , "completed status" , true , false ) ,
				EntityVar.metaStringVar( ReleaseSchedule.PROPERTY_SCHEDULE , FIELD_SCHEDULE_NAME , "current phase" , false , null ) ,
				EntityVar.metaIntegerVar( ReleaseSchedule.PROPERTY_PHASE , FIELD_SCHEDULE_PHASE , "current phase" , false , null )
		} ) );
	}

	public static PropertyEntity makeEntityReleasePhase( DBConnection c , boolean upgrade ) throws Exception {
		PropertyEntity entity = PropertyEntity.getAppObjectEntity( DBEnumObjectType.RELEASE_PHASE , DBEnumParamEntityType.RELEASE_PHASE , DBEnumObjectVersionType.RELEASE , TABLE_PHASE , FIELD_PHASE_ID , false );
		if( !upgrade ) {
			DBSettings.loaddbAppEntity( c , entity );
			return( entity );
		}
		
		return( DBSettings.savedbObjectEntity( c , entity , new EntityVar[] { 
				EntityVar.metaObjectDatabaseOnly( FIELD_RELEASE_ID , "release id" , DBEnumObjectType.RELEASE_MAIN , true ) ,
				EntityVar.metaEnumVar( ReleaseSchedulePhase.PROPERTY_RELEASESTAGE , FIELD_PHASE_LCSTAGETYPE , "release phase stage type" , true , DBEnumLifecycleStageType.UNKNOWN ) ,
				EntityVar.metaIntegerVar( ReleaseSchedulePhase.PROPERTY_STAGEPOS , FIELD_PHASE_STAGEPOS , "release phase stage position" , true , null ) ,
				EntityVar.metaString( ReleaseSchedulePhase.PROPERTY_NAME , "product name" , true , null ) ,
				EntityVar.metaStringVar( ReleaseSchedulePhase.PROPERTY_DESC , FIELD_PHASE_DESC , "Description" , false , null ) ,
				EntityVar.metaInteger( ReleaseSchedulePhase.PROPERTY_DAYS , "phase days" , true , null ) ,
				EntityVar.metaIntegerVar( ReleaseSchedulePhase.PROPERTY_NORMALDAYS , FIELD_PHASE_NORMALDAYS , "phase days" , true , null ) ,
				EntityVar.metaBoolean( ReleaseSchedulePhase.PROPERTY_FINISHED , "finished status" , true , false ) ,
				EntityVar.metaBoolean( ReleaseSchedulePhase.PROPERTY_UNLIMITED , "unlimited status" , true , false ) ,
				EntityVar.metaDateVar( ReleaseSchedulePhase.PROPERTY_STARTDATE , FIELD_PHASE_STARTDATE , "actual start date" , false ) ,
				EntityVar.metaDateVar( ReleaseSchedulePhase.PROPERTY_FINISHDATE , FIELD_PHASE_FINISHDATE , "actual finish date" , false )
		} ) );
	}

	public static PropertyEntity makeEntityReleaseTicketSet( DBConnection c , boolean upgrade ) throws Exception {
		PropertyEntity entity = PropertyEntity.getAppObjectEntity( DBEnumObjectType.RELEASE_TICKETSET , DBEnumParamEntityType.RELEASE_TICKETSET , DBEnumObjectVersionType.RELEASE , TABLE_TICKETSET , FIELD_TICKETSET_ID , false );
		if( !upgrade ) {
			DBSettings.loaddbAppEntity( c , entity );
			return( entity );
		}
		
		return( DBSettings.savedbObjectEntity( c , entity , new EntityVar[] { 
				EntityVar.metaObjectDatabaseOnly( FIELD_RELEASE_ID , "release id" , DBEnumObjectType.RELEASE_MAIN , true ) ,
				EntityVar.metaString( ReleaseTicketSet.PROPERTY_CODE , "ticket set code" , true , null ) ,
				EntityVar.metaString( ReleaseTicketSet.PROPERTY_NAME , "ticket set name" , true , null ) ,
				EntityVar.metaStringVar( ReleaseTicketSet.PROPERTY_DESC , FIELD_TICKETSET_DESC , "Description" , false , null ) ,
				EntityVar.metaEnumVar( ReleaseTicketSet.PROPERTY_STATUS , FIELD_TICKETSET_STATUS , "ticket set status type" , true , DBEnumTicketSetStatusType.UNKNOWN )
		} ) );
	}

	public static PropertyEntity makeEntityReleaseTicketTarget( DBConnection c , boolean upgrade ) throws Exception {
		PropertyEntity entity = PropertyEntity.getAppObjectEntity( DBEnumObjectType.RELEASE_TICKETTARGET , DBEnumParamEntityType.RELEASE_TICKETTARGET , DBEnumObjectVersionType.RELEASE , TABLE_TICKETTARGET , FIELD_TICKETTARGET_ID , false );
		if( !upgrade ) {
			DBSettings.loaddbAppEntity( c , entity );
			return( entity );
		}
		
		return( DBSettings.savedbObjectEntity( c , entity , new EntityVar[] { 
				EntityVar.metaObjectDatabaseOnly( FIELD_RELEASE_ID , "release id" , DBEnumObjectType.RELEASE_MAIN , true ) ,
				EntityVar.metaObjectDatabaseOnly( FIELD_TICKETTARGET_TICKETSET_ID , "ticket set id" , DBEnumObjectType.RELEASE_TICKETSET , true ) ,
				EntityVar.metaInteger( ReleaseTicketTarget.PROPERTY_POS , "ticket target position" , true , null ) ,
				EntityVar.metaObjectDatabaseOnly( FIELD_TICKETTARGET_BUILDTARGET_ID , "ticket build target id" , DBEnumObjectType.RELEASE_BUILDTARGET , true ) ,
				EntityVar.metaObjectDatabaseOnly( FIELD_TICKETTARGET_DISTTARGET_ID , "ticket distributive target id" , DBEnumObjectType.RELEASE_DISTTARGET , true ) ,
				EntityVar.metaBoolean( ReleaseTicket.PROPERTY_DESCOPED , "descoped status" , true , false ) ,
				EntityVar.metaBoolean( ReleaseTicket.PROPERTY_ACCEPTED , "accepted status" , true , false )
		} ) );
	}

	public static PropertyEntity makeEntityReleaseTicket( DBConnection c , boolean upgrade ) throws Exception {
		PropertyEntity entity = PropertyEntity.getAppObjectEntity( DBEnumObjectType.RELEASE_TICKET , DBEnumParamEntityType.RELEASE_TICKET , DBEnumObjectVersionType.RELEASE , TABLE_TICKET , FIELD_TICKET_ID , false );
		if( !upgrade ) {
			DBSettings.loaddbAppEntity( c , entity );
			return( entity );
		}
		
		return( DBSettings.savedbObjectEntity( c , entity , new EntityVar[] { 
				EntityVar.metaObjectDatabaseOnly( FIELD_RELEASE_ID , "release id" , DBEnumObjectType.RELEASE_MAIN , true ) ,
				EntityVar.metaObjectDatabaseOnly( FIELD_TICKET_TICKETSET_ID , "ticket set id" , DBEnumObjectType.RELEASE_TICKETSET , true ) ,
				EntityVar.metaInteger( ReleaseTicket.PROPERTY_POS , "ticket position" , true , null ) ,
				EntityVar.metaString( ReleaseTicket.PROPERTY_CODE , "ticket code" , true , null ) ,
				EntityVar.metaString( ReleaseTicket.PROPERTY_NAME , "ticket name" , true , null ) ,
				EntityVar.metaStringVar( ReleaseTicket.PROPERTY_DESC , FIELD_TICKET_DESC , "Description" , false , null ) ,
				EntityVar.metaString( ReleaseTicket.PROPERTY_LINK , "ticket link reference" , false , null ) ,
				EntityVar.metaEnumVar( ReleaseTicket.PROPERTY_TYPE , FIELD_TICKET_TYPE , "ticket type" , true , DBEnumTicketType.UNKNOWN ) ,
				EntityVar.metaEnumVar( ReleaseTicket.PROPERTY_STATUS , FIELD_TICKET_STATUS , "ticket status" , true , DBEnumTicketStatusType.UNKNOWN ) ,
				EntityVar.metaBoolean( ReleaseTicket.PROPERTY_ACTIVE , "active status" , true , false ) ,
				EntityVar.metaBoolean( ReleaseTicket.PROPERTY_ACCEPTED , "accepted status" , true , false ) ,
				EntityVar.metaBoolean( ReleaseTicket.PROPERTY_DESCOPED , "descoped status" , true , false ) ,
				EntityVar.metaObjectDatabaseOnly( FIELD_TICKET_OWNER_ID , "owner user id" , DBEnumObjectType.AUTH_USER , false ) ,
				EntityVar.metaStringVar( ReleaseTicket.PROPERTY_OWNER , FIELD_TICKET_OWNER_NAME , "owner user name" , false , null ) ,
				EntityVar.metaObjectDatabaseOnly( FIELD_TICKET_DEVUSER_ID , "developer user id" , DBEnumObjectType.AUTH_USER , false ) ,
				EntityVar.metaStringVar( ReleaseTicket.PROPERTY_DEV , FIELD_TICKET_DEVUSER_NAME , "developer user name" , false , null ) ,
				EntityVar.metaObjectDatabaseOnly( FIELD_TICKET_QAUSER_ID , "qa user id" , DBEnumObjectType.AUTH_USER , false ) ,
				EntityVar.metaStringVar( ReleaseTicket.PROPERTY_QA , FIELD_TICKET_QAUSER_NAME , "qa user name" , false , null ) ,
		} ) );
	}

	public static void dropAllMeta( DBConnection c , ProductMeta storage ) throws Exception {
		int metaId = storage.ID;
		dropReleaseTickets( c , metaId );
		dropReleaseSchedule( c , metaId );
		dropReleaseCore( c , metaId );
	}

	private static void dropReleaseCore( DBConnection c , int metaId ) throws Exception {
		EngineEntities entities = c.getEntities();
		DBEngineEntities.dropAppObjects( c , entities.entityAppReleaseDistItem , DBQueries.FILTER_REL_META1 , new String[] { EngineDB.getInteger( metaId ) } );
		DBEngineEntities.dropAppObjects( c , entities.entityAppReleaseBuildTarget , DBQueries.FILTER_REL_META1 , new String[] { EngineDB.getInteger( metaId ) } );
		DBEngineEntities.dropAppObjects( c , entities.entityAppReleaseDistTarget , DBQueries.FILTER_REL_META1 , new String[] { EngineDB.getInteger( metaId ) } );
		DBEngineEntities.dropAppObjects( c , entities.entityAppReleaseDist , DBQueries.FILTER_REL_META1 , new String[] { EngineDB.getInteger( metaId ) } );
		DBEngineEntities.dropAppObjects( c , entities.entityAppReleaseMain , DBQueries.FILTER_REL_MAINMETA1 , new String[] { EngineDB.getInteger( metaId ) } );
		DBEngineEntities.dropAppObjects( c , entities.entityAppReleaseRepository , DBQueries.FILTER_REL_REPOMETA1 , new String[] { EngineDB.getInteger( metaId ) } );
	}
	
	private static void dropReleaseSchedule( DBConnection c , int metaId ) throws Exception {
		EngineEntities entities = c.getEntities();
		DBEngineEntities.dropAppObjects( c , entities.entityAppReleasePhase , DBQueries.FILTER_REL_META1 , new String[] { EngineDB.getInteger( metaId ) } );
		DBEngineEntities.dropAppObjects( c , entities.entityAppReleaseSchedule , DBQueries.FILTER_REL_META1 , new String[] { EngineDB.getInteger( metaId ) } );
	}
	
	private static void dropReleaseTickets( DBConnection c , int metaId ) throws Exception {
		EngineEntities entities = c.getEntities();
		DBEngineEntities.dropAppObjects( c , entities.entityAppReleaseTicketTarget , DBQueries.FILTER_REL_META1 , new String[] { EngineDB.getInteger( metaId ) } );
		DBEngineEntities.dropAppObjects( c , entities.entityAppReleaseTicket , DBQueries.FILTER_REL_META1 , new String[] { EngineDB.getInteger( metaId ) } );
		DBEngineEntities.dropAppObjects( c , entities.entityAppReleaseTicketSet , DBQueries.FILTER_REL_META1 , new String[] { EngineDB.getInteger( metaId ) } );
	}

	public static void rematchReleases( EngineLoader loader , ReleaseRepository repo , ProductMeta storage , ProductMeta storageOld ) throws Exception {
		DBConnection c = loader.getConnection();
		if( !c.modify( DBQueries.MODIFY_REL_REMATCHBUILDTARGETSRCSET2 , new String[] { 
				EngineDB.getInteger( storageOld.ID ) ,
				EngineDB.getInteger( storage.ID )
				}) )
			Common.exitUnexpected();
		if( !c.modify( DBQueries.MODIFY_REL_REMATCHBUILDTARGETPROJECT2 , new String[] { 
				EngineDB.getInteger( storageOld.ID ) ,
				EngineDB.getInteger( storage.ID )
				}) )
			Common.exitUnexpected();
		if( !c.modify( DBQueries.MODIFY_REL_REMATCHDISTTARGETDELIVERY2 , new String[] { 
				EngineDB.getInteger( storageOld.ID ) ,
				EngineDB.getInteger( storage.ID )
				}) )
			Common.exitUnexpected();
		if( !c.modify( DBQueries.MODIFY_REL_REMATCHDISTTARGETBINARY2 , new String[] { 
				EngineDB.getInteger( storageOld.ID ) ,
				EngineDB.getInteger( storage.ID )
				}) )
			Common.exitUnexpected();
		if( !c.modify( DBQueries.MODIFY_REL_REMATCHDISTTARGETCONF2 , new String[] { 
				EngineDB.getInteger( storageOld.ID ) ,
				EngineDB.getInteger( storage.ID )
				}) )
			Common.exitUnexpected();
		if( !c.modify( DBQueries.MODIFY_REL_REMATCHDISTTARGETSCHEMA2 , new String[] { 
				EngineDB.getInteger( storageOld.ID ) ,
				EngineDB.getInteger( storage.ID )
				}) )
			Common.exitUnexpected();
		if( !c.modify( DBQueries.MODIFY_REL_REMATCHDISTTARGETDOC2 , new String[] { 
				EngineDB.getInteger( storageOld.ID ) ,
				EngineDB.getInteger( storage.ID )
				}) )
			Common.exitUnexpected();
		
		DBReleaseRepository.changeRevision( c , repo.ID , storage );
	}
	
}
