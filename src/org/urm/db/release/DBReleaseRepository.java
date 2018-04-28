package org.urm.db.release;

import java.sql.ResultSet;
import java.util.Date;

import org.urm.action.ActionBase;
import org.urm.common.Common;
import org.urm.db.DBConnection;
import org.urm.db.DBQueries;
import org.urm.db.EngineDB;
import org.urm.db.engine.DBEngineEntities;
import org.urm.engine.data.EngineEntities;
import org.urm.engine.dist.ReleaseLabelInfo;
import org.urm.engine.dist._Error;
import org.urm.engine.properties.PropertyEntity;
import org.urm.engine.run.EngineMethod;
import org.urm.meta.engine.AppProduct;
import org.urm.meta.engine.AppProductPolicy;
import org.urm.meta.engine.ReleaseLifecycle;
import org.urm.meta.loader.EngineLoader;
import org.urm.meta.product.Meta;
import org.urm.meta.product.ProductMeta;
import org.urm.meta.release.Release;
import org.urm.meta.release.ReleaseChanges;
import org.urm.meta.release.ReleaseDist;
import org.urm.meta.release.ReleaseRepository;
import org.urm.meta.release.ReleaseSchedule;
import org.urm.meta.release.ReleaseScope;
import org.urm.meta.release.ReleaseRepository.ReleaseOperation;
import org.urm.meta.release.ReleaseTicketSet;

public class DBReleaseRepository {

	public static Release createReleaseNormal( EngineMethod method , ActionBase action , ReleaseRepository repo , ReleaseLabelInfo info , Date releaseDate , ReleaseLifecycle lc ) throws Exception {
		Meta meta = repo.meta;

		if( repo.findReleaseByFullVersion( info.RELEASEVER ) != null )
			action.exit1( _Error.ReleaseAlreadyExists1 , "release version=" + info.RELEASEVER + " already exists" , info.RELEASEVER );
		
		AppProduct product = meta.findProduct();
		AppProductPolicy policy = product.getPolicy();
		
		lc = policy.getLifecycle( action , lc , info.getLifecycleType() );
		releaseDate = getReleaseDate( action , product , info.RELEASEVER , releaseDate , lc );
		if( releaseDate == null )
			action.exit1( _Error.MissingReleaseDate1 , "unable to create release version=" + info.RELEASEVER + " due to missing release date" , info.RELEASEVER );

		action.debug( "create normal release: version=" + info.RELEASEVER + ", version=" + info.RELEASEVER + ", date=" + Common.getDateValue( releaseDate ) + " ..." );
		
		// create meta item
		Release release = DBRelease.createNormalRelease( method , action , repo , info.RELEASEVER , releaseDate , lc );
		repo.addRelease( release );
		return( release );
	}

	public static Release createReleaseNormal( EngineMethod method , ActionBase action , ReleaseRepository repo , ReleaseLabelInfo info ) throws Exception {
		if( repo.findReleaseByFullVersion( info.RELEASEVER ) != null )
			action.exit1( _Error.ReleaseAlreadyExists1 , "release version=" + info.RELEASEVER + " already exists" , info.RELEASEVER );
		
		action.debug( "create normal release: version=" + info.RELEASEVER + ", version=" + info.RELEASEVER + " ..." );
		
		// create meta item
		Release release = DBRelease.createNormalRelease( method , action , repo , info.RELEASEVER , new Date() , null );
		repo.addRelease( release );
		return( release );
	}
	
	public static Release createReleaseMaster( EngineMethod method , ActionBase action , ReleaseRepository repo , String RELEASEVER ) throws Exception {
		Release release = repo.findMaster( ReleaseRepository.MASTER_NAME_PRIMARY );
		if( repo.findMaster( ReleaseRepository.MASTER_NAME_PRIMARY ) != null )
			action.exit1( _Error.ReleaseAlreadyExists1 , "master release version=" + release.RELEASEVER + " already exists" , release.RELEASEVER );
		
		action.debug( "create master release: version=" + RELEASEVER + " ..." );
		
		// create meta item
		release = DBRelease.createMasterRelease( method , action , repo , ReleaseRepository.MASTER_NAME_PRIMARY , "" , RELEASEVER );
		repo.addRelease( release );
		return( release );
	}

	private static Date getReleaseDate( ActionBase action , AppProduct product , String RELEASEVER , Date releaseDate , ReleaseLifecycle lc ) throws Exception {
		if( releaseDate != null )
			return( releaseDate );
		
		Date date = ReleaseLifecycle.findReleaseDate( action , RELEASEVER , product , lc );
		if( date == null )
			action.exit0( _Error.MissingReleaseDate0 , "Missing release date" );
		return( null );
	}

	public static void loaddbReleases( EngineLoader loader , ReleaseRepository repo ) throws Exception {
		loaddbReleasesMain( loader , repo );
		loaddbReleasesDist( loader , repo );
		loaddbReleasesSchedule( loader , repo );
		loaddbReleasesSchedulePhase( loader , repo );
		loaddbReleasesTicketSet( loader , repo );
		loaddbReleasesTicket( loader , repo );
		loaddbReleasesBuildTarget( loader , repo );
		loaddbReleasesDistTarget( loader , repo );
		loaddbReleasesTicketTarget( loader , repo );
		loaddbReleasesDistItem( loader , repo );
		
		// reorder and recalculate schedules
		for( Release release : repo.getReleases() ) {
			ReleaseSchedule schedule = release.getSchedule();
			schedule.sortPhases();
			schedule.setDeadlines();
			
			ReleaseChanges changes = release.getChanges();
			for( ReleaseTicketSet set : changes.getSets() ) {
				set.sortTickets();
				set.sortTargets();
			}
		}
	}
	
	private static void loaddbReleasesMain( EngineLoader loader , ReleaseRepository repo ) throws Exception {
		DBConnection c = loader.getConnection();
		EngineEntities entities = loader.getEntities();
		PropertyEntity entity = entities.entityAppReleaseMain;
		
		ResultSet rs = DBEngineEntities.listAppObjectsFiltered( c , entity , DBQueries.FILTER_REL_REPO1 , 
				new String[] { EngineDB.getInteger( repo.ID ) 
				} );
		try {
			while( rs.next() ) {
				Release release = DBRelease.loaddbRelease( loader , repo , rs );
				repo.addRelease( release );
			}
		}
		finally {
			c.closeQuery();
		}
	}

	private static void loaddbReleasesDist( EngineLoader loader , ReleaseRepository repo ) throws Exception {
		DBConnection c = loader.getConnection();
		EngineEntities entities = loader.getEntities();
		PropertyEntity entity = entities.entityAppReleaseDist;
		
		ResultSet rs = DBEngineEntities.listAppObjectsFiltered( c , entity , DBQueries.FILTER_REL_REPORELEASEACTIVE1 , 
				new String[] { EngineDB.getInteger( repo.ID ) 
				} );
		try {
			while( rs.next() ) {
				int releaseId = entity.loaddbObject( rs , DBReleaseData.FIELD_RELEASE_ID );
				Release release = repo.getRelease( releaseId );
				ReleaseDist releaseDist = DBReleaseDist.loaddbReleaseDist( loader , release , rs );
				if( releaseDist.isDefault() )
					release.setDefaultDist( releaseDist );
				else
					release.addDist( releaseDist );
			}
		}
		finally {
			c.closeQuery();
		}
	}

	private static void loaddbReleasesSchedule( EngineLoader loader , ReleaseRepository repo ) throws Exception {
		DBConnection c = loader.getConnection();
		EngineEntities entities = loader.getEntities();
		PropertyEntity entity = entities.entityAppReleaseSchedule;
		
		ResultSet rs = DBEngineEntities.listAppObjectsFiltered( c , entity , DBQueries.FILTER_REL_REPORELEASEACTIVE1 , 
				new String[] { EngineDB.getInteger( repo.ID ) 
				} );
		try {
			while( rs.next() ) {
				int releaseId = entity.loaddbId( rs );
				Release release = repo.getRelease( releaseId );
				ReleaseSchedule releaseSchedule = release.getSchedule();
				DBReleaseSchedule.loaddbReleaseSchedule( loader , release , releaseSchedule , rs );
			}
		}
		finally {
			c.closeQuery();
		}
	}

	private static void loaddbReleasesSchedulePhase( EngineLoader loader , ReleaseRepository repo ) throws Exception {
		DBConnection c = loader.getConnection();
		EngineEntities entities = loader.getEntities();
		PropertyEntity entity = entities.entityAppReleasePhase;
		
		ResultSet rs = DBEngineEntities.listAppObjectsFiltered( c , entity , DBQueries.FILTER_REL_REPORELEASEACTIVE1 , 
				new String[] { EngineDB.getInteger( repo.ID ) 
				} );
		try {
			while( rs.next() ) {
				int releaseId = entity.loaddbObject( rs , DBReleaseData.FIELD_RELEASE_ID );
				Release release = repo.getRelease( releaseId );
				ReleaseSchedule releaseSchedule = release.getSchedule();
				DBReleaseSchedulePhase.loaddbReleaseSchedulePhase( loader , release , releaseSchedule , rs );
			}
		}
		finally {
			c.closeQuery();
		}
	}

	private static void loaddbReleasesTicketSet( EngineLoader loader , ReleaseRepository repo ) throws Exception {
		DBConnection c = loader.getConnection();
		EngineEntities entities = loader.getEntities();
		PropertyEntity entity = entities.entityAppReleaseTicketSet;
		
		ResultSet rs = DBEngineEntities.listAppObjectsFiltered( c , entity , DBQueries.FILTER_REL_REPORELEASEACTIVE1 , 
				new String[] { EngineDB.getInteger( repo.ID ) 
				} );
		try {
			while( rs.next() ) {
				int releaseId = entity.loaddbObject( rs , DBReleaseData.FIELD_RELEASE_ID );
				Release release = repo.getRelease( releaseId );
				ReleaseChanges releaseChanges = release.getChanges();
				DBReleaseChanges.loaddbReleaseTicketSet( loader , release , releaseChanges , rs );
			}
		}
		finally {
			c.closeQuery();
		}
	}

	private static void loaddbReleasesTicket( EngineLoader loader , ReleaseRepository repo ) throws Exception {
		DBConnection c = loader.getConnection();
		EngineEntities entities = loader.getEntities();
		PropertyEntity entity = entities.entityAppReleaseTicket;
		
		ResultSet rs = DBEngineEntities.listAppObjectsFiltered( c , entity , DBQueries.FILTER_REL_REPORELEASEACTIVE1 , 
				new String[] { EngineDB.getInteger( repo.ID ) 
				} );
		try {
			while( rs.next() ) {
				int releaseId = entity.loaddbObject( rs , DBReleaseData.FIELD_RELEASE_ID );
				int setId = entity.loaddbObject( rs , DBReleaseData.FIELD_TICKET_TICKETSET_ID );
				Release release = repo.getRelease( releaseId );
				ReleaseChanges changes = release.getChanges();
				ReleaseTicketSet set = changes.getSet( setId );
				DBReleaseChanges.loaddbReleaseTicket( loader , release , changes , set , rs );
			}
		}
		finally {
			c.closeQuery();
		}
	}

	private static void loaddbReleasesBuildTarget( EngineLoader loader , ReleaseRepository repo ) throws Exception {
		DBConnection c = loader.getConnection();
		EngineEntities entities = loader.getEntities();
		PropertyEntity entity = entities.entityAppReleaseBuildTarget;
		
		ResultSet rs = DBEngineEntities.listAppObjectsFiltered( c , entity , DBQueries.FILTER_REL_REPORELEASEACTIVE1 , 
				new String[] { EngineDB.getInteger( repo.ID ) 
				} );
		try {
			while( rs.next() ) {
				int releaseId = entity.loaddbObject( rs , DBReleaseData.FIELD_RELEASE_ID );
				Release release = repo.getRelease( releaseId );
				ReleaseChanges releaseChanges = release.getChanges();
				ReleaseScope releaseScope = release.getScope();
				DBReleaseBuildTarget.loaddbReleaseBuildTarget( loader , release , releaseChanges , releaseScope , rs );
			}
		}
		finally {
			c.closeQuery();
		}
	}

	private static void loaddbReleasesDistTarget( EngineLoader loader , ReleaseRepository repo ) throws Exception {
		DBConnection c = loader.getConnection();
		EngineEntities entities = loader.getEntities();
		PropertyEntity entity = entities.entityAppReleaseDistTarget;
		
		ResultSet rs = DBEngineEntities.listAppObjectsFiltered( c , entity , DBQueries.FILTER_REL_REPORELEASEACTIVE1 , 
				new String[] { EngineDB.getInteger( repo.ID ) 
				} );
		try {
			while( rs.next() ) {
				int releaseId = entity.loaddbObject( rs , DBReleaseData.FIELD_RELEASE_ID );
				Release release = repo.getRelease( releaseId );
				ReleaseChanges releaseChanges = release.getChanges();
				ReleaseScope releaseScope = release.getScope();
				DBReleaseDistTarget.loaddbReleaseDistTarget( loader , release , releaseChanges , releaseScope , rs );
			}
		}
		finally {
			c.closeQuery();
		}
	}

	private static void loaddbReleasesTicketTarget( EngineLoader loader , ReleaseRepository repo ) throws Exception {
		DBConnection c = loader.getConnection();
		EngineEntities entities = loader.getEntities();
		PropertyEntity entity = entities.entityAppReleaseTicketTarget;
		
		ResultSet rs = DBEngineEntities.listAppObjectsFiltered( c , entity , DBQueries.FILTER_REL_REPORELEASEACTIVE1 , 
				new String[] { EngineDB.getInteger( repo.ID ) 
				} );
		try {
			while( rs.next() ) {
				int releaseId = entity.loaddbObject( rs , DBReleaseData.FIELD_RELEASE_ID );
				Release release = repo.getRelease( releaseId );
				ReleaseChanges releaseChanges = release.getChanges();
				DBReleaseTicketTarget.loaddbReleaseTicketTarget( loader , release , releaseChanges , rs );
			}
		}
		finally {
			c.closeQuery();
		}
	}

	private static void loaddbReleasesDistItem( EngineLoader loader , ReleaseRepository repo ) throws Exception {
		DBConnection c = loader.getConnection();
		EngineEntities entities = loader.getEntities();
		PropertyEntity entity = entities.entityAppReleaseDistItem;
		
		ResultSet rs = DBEngineEntities.listAppObjectsFiltered( c , entity , DBQueries.FILTER_REL_REPORELEASEACTIVE1 , 
				new String[] { EngineDB.getInteger( repo.ID ) 
				} );
		try {
			while( rs.next() ) {
				int releaseId = entity.loaddbObject( rs , DBReleaseData.FIELD_RELEASE_ID );
				int releaseDistId = entity.loaddbObject( rs , DBReleaseData.FIELD_DISTITEM_DIST_ID );
				Release release = repo.getRelease( releaseId );
				ReleaseDist releaseDist = release.getDistVariant( releaseDistId );
				DBReleaseDistTarget.loaddbReleaseDistItem( loader , release , releaseDist , rs );
			}
		}
		finally {
			c.closeQuery();
		}
	}

	public static void dropRelease( EngineMethod method , ActionBase action , ReleaseRepository repo , Release release ) throws Exception {
		DBConnection c = method.getMethodConnection( action );
		EngineEntities entities = c.getEntities();
		
		method.checkUpdateRelease( release );
		DBEngineEntities.dropAppObjects( c , entities.entityAppReleaseTicket , DBQueries.FILTER_REL_RELEASE1 , new String[] { EngineDB.getObject( release.ID ) } );
		DBEngineEntities.dropAppObjects( c , entities.entityAppReleaseTicketTarget , DBQueries.FILTER_REL_RELEASE1 , new String[] { EngineDB.getObject( release.ID ) } );
		DBEngineEntities.dropAppObjects( c , entities.entityAppReleaseTicketSet , DBQueries.FILTER_REL_RELEASE1 , new String[] { EngineDB.getObject( release.ID ) } );
		DBEngineEntities.dropAppObjects( c , entities.entityAppReleasePhase , DBQueries.FILTER_REL_RELEASE1 , new String[] { EngineDB.getObject( release.ID ) } );
		DBEngineEntities.dropAppObjects( c , entities.entityAppReleaseSchedule , DBQueries.FILTER_REL_RELEASE1 , new String[] { EngineDB.getObject( release.ID ) } );
		DBEngineEntities.dropAppObjects( c , entities.entityAppReleaseBuildTarget , DBQueries.FILTER_REL_RELEASE1 , new String[] { EngineDB.getObject( release.ID ) } );
		DBEngineEntities.dropAppObjects( c , entities.entityAppReleaseDistItem , DBQueries.FILTER_REL_RELEASE1 , new String[] { EngineDB.getObject( release.ID ) } );
		DBEngineEntities.dropAppObjects( c , entities.entityAppReleaseDistTarget , DBQueries.FILTER_REL_RELEASE1 , new String[] { EngineDB.getObject( release.ID ) } );
		DBEngineEntities.dropAppObjects( c , entities.entityAppReleaseDist , DBQueries.FILTER_REL_RELEASE1 , new String[] { EngineDB.getObject( release.ID ) } );
		DBEngineEntities.dropAppObjects( c , entities.entityAppReleaseMain , DBQueries.FILTER_REL_RELEASE1 , new String[] { EngineDB.getObject( release.ID ) } );
		repo.removeRelease( release );
	}

	public static void dropRevisionRepository( DBConnection c , ProductMeta storage ) throws Exception {
		EngineEntities entities = c.getEntities();
		
		ReleaseRepository repo = storage.getReleaseRepository();
		int version = c.getNextProductVersion( storage );
		DBEngineEntities.deleteAppObject( c , entities.entityAppReleaseRepository , repo.ID , version );
	}
	
	public static void changeRevision( DBConnection c , int repoId , ProductMeta storage ) throws Exception {
		if( !c.modify( DBQueries.MODIFY_REL_CHANGEREPOREVISION2 , new String[] {
				EngineDB.getObject( repoId ) ,
				EngineDB.getObject( storage.ID )
				}))
			Common.exitUnexpected();
	}
	
	public static void archiveRelease( EngineMethod method , ActionBase action , ReleaseRepository repo , Release release ) throws Exception {
		DBConnection c = method.getMethodConnection( action );
		method.checkUpdateRelease( release );
		
		release.setArchived();
		DBRelease.modifyRelease( c , repo , release , false );
		
		repo.archiveRelease( release );
	}

	public static void addDistAction( EngineMethod method , Release release , ReleaseDist releaseDist , boolean success , ReleaseOperation op , String msg ) throws Exception {
	}
	
}
