package org.urm.db.release;

import java.sql.ResultSet;

import org.urm.common.Common;
import org.urm.db.DBConnection;
import org.urm.db.DBQueries;
import org.urm.db.EngineDB;
import org.urm.db.core.DBNames;
import org.urm.db.core.DBEnums.DBEnumParamEntityType;
import org.urm.db.engine.DBEngineEntities;
import org.urm.engine.data.EngineEntities;
import org.urm.engine.properties.PropertyEntity;
import org.urm.meta.EngineLoader;
import org.urm.meta.release.ProductReleases;
import org.urm.meta.release.ReleaseRepository;

public class DBProductReleases {

	public static void createdb( EngineLoader loader , ProductReleases releases , boolean forceClearMeta , boolean forceClearDist ) throws Exception {
		matchRepositories( loader , releases );
		
		ReleaseRepository repo = null;
		if( forceClearMeta )
			DBReleaseData.dropAllMeta( loader , releases );
		else
			repo = loaddbRepository( loader , releases );
		
		if( repo == null )
			repo = createdbRepository( loader , releases );
		releases.setReleaseRepository( repo );
	}
	
	public static void loaddb( EngineLoader loader , ProductReleases releases ) throws Exception {
		ReleaseRepository repo = loaddbRepository( loader , releases );
		if( repo == null )
			Common.exitUnexpected();
		
		releases.setReleaseRepository( repo );
	} 

	private static ReleaseRepository createdbRepository( EngineLoader loader , ProductReleases releases ) throws Exception {
		DBConnection c = loader.getConnection();
		
		ReleaseRepository repo = new ReleaseRepository( releases.meta , releases );
		repo.createRepository( "main" , "default" );
		modifyRepository( c , repo , true );
		return( repo );
	}
	
	private static ReleaseRepository loaddbRepository( EngineLoader loader , ProductReleases releases ) throws Exception {
		DBConnection c = loader.getConnection();
		EngineEntities entities = loader.getEntities();
		PropertyEntity entity = entities.entityAppReleaseRepository;
		
		ReleaseRepository repo = null;
		ResultSet rs = DBEngineEntities.listAppObjectsFiltered( c , entity , DBQueries.FILTER_REL_REPOMETA1 , 
				new String[] { EngineDB.getInteger( releases.meta.getId() ) 
				} );
		try {
			while( rs.next() ) {
				repo = new ReleaseRepository( releases.meta , releases );
				repo.createRepository(
						entity.loaddbString( rs , ReleaseRepository.PROPERTY_NAME ) ,
						entity.loaddbString( rs , ReleaseRepository.PROPERTY_DESC )
						);
				break;
			}
		}
		finally {
			c.closeQuery();
		}

		// load repository releases
		return( repo );
	}
	
	private static void matchRepositories( EngineLoader loader , ProductReleases releases ) throws Exception {
		DBConnection c = loader.getConnection();
		if( !c.modify( DBQueries.MODIFY_REL_REPO_MATCHMETA2 , new String[] { EngineDB.getInteger( releases.meta.getId() ) , EngineDB.getString( releases.meta.name ) } ) )
			Common.exitUnexpected();
	}

	private static void modifyRepository( DBConnection c , ReleaseRepository repo , boolean insert ) throws Exception {
		if( insert )
			repo.ID = DBNames.getNameIndex( c , repo.meta.getId() , repo.NAME , DBEnumParamEntityType.RELEASE_REPOSITORY );
		else
			DBNames.updateName( c , repo.meta.getId() , repo.NAME , repo.ID , DBEnumParamEntityType.RELEASE_REPOSITORY );
		
		EngineEntities entities = c.getEntities();
		DBEngineEntities.modifyAppObject( c , entities.entityAppReleaseRepository , repo.ID , EngineDB.APP_VERSION , new String[] {
				EngineDB.getString( repo.NAME ) ,
				EngineDB.getString( repo.DESC ) ,
				EngineDB.getObject( repo.meta.getId() ) ,
				EngineDB.getString( null )
		} , insert );
	}

}
