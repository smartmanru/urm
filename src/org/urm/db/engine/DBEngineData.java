package org.urm.db.engine;

import org.urm.common.Common;
import org.urm.db.DBConnection;
import org.urm.db.DBQueries;
import org.urm.db.EngineDB;
import org.urm.db.core.DBEnums;
import org.urm.db.core.DBVersions;
import org.urm.engine.properties.EngineEntities;
import org.urm.meta.EngineLoader;

public abstract class DBEngineData {

	public static void upgradeMeta( EngineLoader loader ) throws Exception {
		DBConnection c = loader.getConnection();
		c.setAppVersion( EngineDB.APP_VERSION );
		DBEnums.updateDatabase( c );
	}

	public static void useMeta( EngineLoader loader ) throws Exception {
		DBEnums.verifyDatabase( loader );
	}
	
	public static void dropCoreData( EngineLoader loader ) throws Exception {
		dropCoreReleasesData( loader );
		dropCoreInfraData( loader );
		dropCoreBaseData( loader );
		dropCoreEngineData( loader );
	}

	public static void dropAuthData( EngineLoader loader ) throws Exception {
		DBConnection c = loader.getConnection();
		EngineEntities entities = c.getEntities();
		boolean res = true;
		res = ( res )? c.update( DBQueries.MODIFY_AUTH_DROP_ACCESSPRODUCT0 ) : false;
		res = ( res )? c.update( DBQueries.MODIFY_AUTH_DROP_ACCESSRESOURCE0 ) : false;
		res = ( res )? c.update( DBQueries.MODIFY_AUTH_DROP_ACCESSNETWORK0 ) : false;
		res = ( res )? c.update( DBQueries.MODIFY_AUTH_DROP_GROUPUSERS0 ) : false;
		DBEngineEntities.dropAppObjects( c , entities.entityAppAuthUser );
		DBEngineEntities.dropAppObjects( c , entities.entityAppAuthGroup );
		if( !res )
			Common.exitUnexpected();
	}

	private static void dropCoreReleasesData( EngineLoader loader ) throws Exception {
		DBConnection c = loader.getConnection();
		EngineEntities entities = c.getEntities();
		DBEngineEntities.dropAppObjects( c , entities.entityAppProjectBuilder );
		DBEngineEntities.dropAppObjects( c , entities.entityAppLifecyclePhase );
		DBEngineEntities.dropAppObjects( c , entities.entityAppReleaseLifecycle );
	}

	private static void dropCoreInfraData( EngineLoader loader ) throws Exception {
		DBConnection c = loader.getConnection();
		EngineEntities entities = c.getEntities();
		DBEngineEntities.dropAppObjects( c , entities.entityAppHostAccount );
		DBEngineEntities.dropAppObjects( c , entities.entityAppNetworkHost );
		DBEngineEntities.dropAppObjects( c , entities.entityAppNetwork );
		DBEngineEntities.dropAppObjects( c , entities.entityAppDatacenter );
	}
	
	private static void dropCoreBaseData( EngineLoader loader ) throws Exception {
		DBConnection c = loader.getConnection();
		EngineEntities entities = c.getEntities();
		boolean res = true;
		res = ( res )? c.update( DBQueries.MODIFY_BASE_DROP_ITEMDEPS0 ) : false;
		DBEngineEntities.dropAppObjects( c , entities.entityAppBaseItem );
		DBEngineEntities.dropAppObjects( c , entities.entityAppBaseGroup );
		if( !res )
			Common.exitUnexpected();
	}

	private static void dropCoreEngineData( EngineLoader loader ) throws Exception {
		DBConnection c = loader.getConnection();
		EngineEntities entities = c.getEntities();
		boolean res = true;
		res = ( res )? c.update( DBQueries.MODIFY_CORE_DROP_PARAMVALUE1 , new String[] { "" + DBVersions.CORE_ID } ) : false;
		res = ( res )? c.update( DBQueries.MODIFY_CORE_DROP_PARAM1 , new String[] { "" + DBVersions.CORE_ID } ) : false;
		DBEngineEntities.dropAppObjects( c , entities.entityAppMirror );
		DBEngineEntities.dropAppObjects( c , entities.entityAppResource );
		if( !res )
			Common.exitUnexpected();
	}
	
}
