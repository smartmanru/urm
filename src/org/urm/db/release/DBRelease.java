package org.urm.db.release;

import java.sql.ResultSet;
import java.util.Date;

import org.urm.action.ActionBase;
import org.urm.db.DBConnection;
import org.urm.db.EngineDB;
import org.urm.db.core.DBEnums.DBEnumBuildModeType;
import org.urm.db.core.DBEnums.DBEnumLifecycleType;
import org.urm.db.engine.DBEngineEntities;
import org.urm.engine.data.EngineEntities;
import org.urm.engine.dist.VersionInfo;
import org.urm.engine.properties.PropertyEntity;
import org.urm.engine.run.EngineMethod;
import org.urm.meta.EngineLoader;
import org.urm.meta.engine.ReleaseLifecycle;
import org.urm.meta.release.Release;
import org.urm.meta.release.ReleaseRepository;

public class DBRelease {

	public static Release createRelease( EngineMethod method , ActionBase action , ReleaseRepository repo , String RELEASEVER , Date releaseDate , ReleaseLifecycle lc ) throws Exception {
		DBConnection c = method.getMethodConnection( action );
		
		Release release = new Release( repo );
		release.createNormal( action , RELEASEVER , releaseDate , lc );

		modifyRelease( c , repo , release , true );
		DBReleaseSchedule.createReleaseSchedule( method , action , release );
		
		return( release );
	}
	
	private static void modifyRelease( DBConnection c , ReleaseRepository repo , Release release , boolean insert ) throws Exception {
		if( insert )
			release.ID = c.getNextSequenceValue();
		
		release.RV = c.getNextReleaseVersion( release );
		EngineEntities entities = c.getEntities();
		VersionInfo info = VersionInfo.getReleaseDirInfo( release.RELEASEVER );
		DBEngineEntities.modifyAppObject( c , entities.entityAppReleaseMain , release.ID , release.RV , new String[] {
				EngineDB.getObject( repo.ID ) ,
				EngineDB.getString( release.NAME ) ,
				EngineDB.getString( release.DESC ) ,
				EngineDB.getBoolean( release.MASTER ) ,
				EngineDB.getEnum( release.TYPE ) ,
				EngineDB.getInteger( info.v1 ) ,
				EngineDB.getInteger( info.v2 ) ,
				EngineDB.getInteger( info.v3 ) ,
				EngineDB.getInteger( info.v4 ) ,
				EngineDB.getString( info.getFullVersion() ) ,
				EngineDB.getEnum( release.BUILDMODE ) ,
				EngineDB.getString( release.COMPATIBILITY ) ,
				EngineDB.getBoolean( release.CUMULATIVE ) ,
				EngineDB.getBoolean( release.ARCHIVED ) ,
				EngineDB.getBoolean( release.CANCELLED )
				} , insert );
	}

	public static Release loaddbRelease( EngineLoader loader , ReleaseRepository repo , ResultSet rs ) throws Exception {
		EngineEntities entities = loader.getEntities();
		PropertyEntity entity = entities.entityAppReleaseMain;
		
		Release release = new Release( repo );
		release.ID = entity.loaddbId( rs );
		release.RV = entity.loaddbVersion( rs );
		release.create(
				entity.loaddbString( rs , Release.PROPERTY_NAME ) ,
				entity.loaddbString( rs , Release.PROPERTY_DESC ) ,
				entity.loaddbBoolean( rs , Release.PROPERTY_MASTER ) ,
				DBEnumLifecycleType.getValue( entity.loaddbEnum( rs , Release.PROPERTY_LIFECYCLETYPE ) , true ) ,
				entity.loaddbString( rs , Release.PROPERTY_VERSION ) ,
				DBEnumBuildModeType.getValue( entity.loaddbEnum( rs , Release.PROPERTY_BUILDMODE ) , false ) ,
				entity.loaddbString( rs , Release.PROPERTY_COMPATIBILITY ) ,
				entity.loaddbBoolean( rs , Release.PROPERTY_CUMULATIVE ) ,
				entity.loaddbBoolean( rs , Release.PROPERTY_ARCHIVED ) ,
				entity.loaddbBoolean( rs , Release.PROPERTY_CANCELLED )
				);
		return( release );
	}
}
