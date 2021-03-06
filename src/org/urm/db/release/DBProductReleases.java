package org.urm.db.release;

import java.sql.ResultSet;

import org.urm.common.Common;
import org.urm.db.DBConnection;
import org.urm.db.DBQueries;
import org.urm.db.EngineDB;
import org.urm.db.core.DBNames;
import org.urm.db.core.DBEnums.*;
import org.urm.db.engine.DBEngineEntities;
import org.urm.engine.data.EngineEntities;
import org.urm.engine.properties.PropertyEntity;
import org.urm.meta.loader.EngineLoader;
import org.urm.meta.product.ProductMeta;
import org.urm.meta.release.ReleaseRepository;

public class DBProductReleases {

	public static ReleaseRepository createdb( EngineLoader loader , ProductMeta storage , boolean forceClearMeta ) throws Exception {
		DBConnection c = loader.getConnection();
		
		ReleaseRepository repo = null;
		if( forceClearMeta )
			DBReleaseData.dropAllMeta( c , storage );
		else
			repo = loaddbRepository( loader , storage );
		
		if( repo == null )
			repo = createdbRepository( loader , storage );
		return( repo );
	}
	
	public static ReleaseRepository loaddb( EngineLoader loader , ProductMeta storage ) throws Exception {
		ReleaseRepository repo = loaddbRepository( loader , storage );
		if( repo == null )
			Common.exitUnexpected();
		return( repo );
	} 

	public static ReleaseRepository loaddbByImport( EngineLoader loader , ProductMeta storage , ProductMeta storageOld ) throws Exception {
		ReleaseRepository repo = null;
		if( storageOld == null )
			repo = createdbRepository( loader , storage );
		else {
			repo = storageOld.getReleaseRepository();
			DBReleaseData.rematchReleases( loader , repo , storage , storageOld );
			
			repo = loaddbRepository( loader , storage );
		}
		return( repo );
	} 

	private static ReleaseRepository createdbRepository( EngineLoader loader , ProductMeta storage ) throws Exception {
		DBConnection c = loader.getConnection();
		
		ReleaseRepository repo = new ReleaseRepository( storage.meta );
		repo.createRepository( "main" , "default" );
		modifyRepository( c , repo , true );
		return( repo );
	}
	
	private static ReleaseRepository loaddbRepository( EngineLoader loader , ProductMeta storage ) throws Exception {
		DBConnection c = loader.getConnection();
		EngineEntities entities = loader.getEntities();
		PropertyEntity entity = entities.entityAppReleaseRepository;
		
		ReleaseRepository repo = null;
		ResultSet rs = DBEngineEntities.listAppObjectsFiltered( c , entity , DBQueries.FILTER_REL_REPOMETA1 , 
				new String[] { EngineDB.getInteger( storage.ID ) 
				} );
		try {
			while( rs.next() ) {
				repo = new ReleaseRepository( storage.meta );
				repo.ID = entity.loaddbId( rs );
				repo.createRepository(
						entity.loaddbString( rs , DBReleaseData.FIELD_REPOSITORY_NAME ) ,
						entity.loaddbString( rs , DBReleaseData.FIELD_REPOSITORY_DESC )
						);
				break;
			}
		}
		finally {
			c.closeQuery();
		}

		// load repository releases
		if( repo != null )
			DBReleaseRepository.loaddbReleases( loader , repo );
		return( repo );
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
				EngineDB.getObject( repo.meta.getId() )
		} , insert );
	}

}
