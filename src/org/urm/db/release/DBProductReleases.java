package org.urm.db.release;

import org.urm.common.Common;
import org.urm.db.DBConnection;
import org.urm.db.DBQueries;
import org.urm.db.EngineDB;
import org.urm.db.engine.DBEngineEntities;
import org.urm.engine.data.EngineEntities;
import org.urm.meta.EngineLoader;
import org.urm.meta.product.ProductMeta;
import org.urm.meta.release.ProductReleases;

public class DBProductReleases {

	public static void createdb( EngineLoader loader , ProductReleases releases , boolean forceClearMeta , boolean forceClearDist ) throws Exception {
		matchRepositories( loader , releases );
		
		if( forceClearMeta )
			dropAllMeta( loader , releases );
	}
	
	public static void importxml( EngineLoader loader , ProductMeta storage ) throws Exception {
	}

	private static void matchRepositories( EngineLoader loader , ProductReleases releases ) throws Exception {
		DBConnection c = loader.getConnection();
		if( !c.modify( DBQueries.MODIFY_REL_REPO_MATCHMETA2 , new String[] { EngineDB.getInteger( releases.meta.getId() ) , EngineDB.getString( releases.meta.name ) } ) )
			Common.exitUnexpected();
	}

	private static void dropAllMeta( EngineLoader loader , ProductReleases releases ) throws Exception {
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
