package org.urm.db.core;

import org.urm.common.Common;
import org.urm.db.DBConnection;
import org.urm.db.DBQueries;
import org.urm.engine.EngineDB;
import org.urm.meta.EngineLoader;

public abstract class DBCoreData {

	public static void upgradeData( EngineLoader loader ) throws Exception {
		DBConnection c = loader.getConnection();
		c.setAppVersion( EngineDB.APP_VERSION );
		DBEnums.updateDatabase( c );
	}

	public static void useData( EngineLoader loader ) throws Exception {
		DBEnums.verifyDatabase( loader );
	}
	
	public static void dropCoreData( EngineLoader loader ) throws Exception {
		dropCoreReleasesData( loader );
		dropCoreAuthData( loader );
		dropCoreInfraData( loader );
		dropCoreBaseData( loader );
		dropCoreEngineData( loader );
	}

	public static void dropCoreReleasesData( EngineLoader loader ) throws Exception {
		DBConnection c = loader.getConnection();
		boolean res = true;
		res = ( res )? c.update( DBQueries.MODIFY_RELEASES_DROP_BUILDERS0 ) : false;
		if( !res )
			Common.exitUnexpected();
	}

	public static void dropCoreAuthData( EngineLoader loader ) throws Exception {
		DBConnection c = loader.getConnection();
		boolean res = true;
		res = ( res )? c.update( DBQueries.MODIFY_AUTH_DROP_ACCESSPRODUCT0 ) : false;
		res = ( res )? c.update( DBQueries.MODIFY_AUTH_DROP_ACCESSRESOURCE0 ) : false;
		res = ( res )? c.update( DBQueries.MODIFY_AUTH_DROP_ACCESSNETWORK0 ) : false;
		res = ( res )? c.update( DBQueries.MODIFY_AUTH_DROP_USER0 ) : false;
		res = ( res )? c.update( DBQueries.MODIFY_AUTH_DROP_GROUP0 ) : false;
		if( !res )
			Common.exitUnexpected();
	}

	public static void dropCoreInfraData( EngineLoader loader ) throws Exception {
		DBConnection c = loader.getConnection();
		boolean res = true;
		res = ( res )? c.update( DBQueries.MODIFY_INFRA_DROP_ACCOUNT0 ) : false;
		res = ( res )? c.update( DBQueries.MODIFY_INFRA_DROP_HOST0 ) : false;
		res = ( res )? c.update( DBQueries.MODIFY_INFRA_DROP_NETWORK0 ) : false;
		res = ( res )? c.update( DBQueries.MODIFY_INFRA_DROP_DATACENTER0 ) : false;
		if( !res )
			Common.exitUnexpected();
	}
	
	public static void dropCoreBaseData( EngineLoader loader ) throws Exception {
		DBConnection c = loader.getConnection();
		boolean res = true;
		res = ( res )? c.update( DBQueries.MODIFY_BASE_DROP_ITEMDEPS0 ) : false;
		res = ( res )? c.update( DBQueries.MODIFY_BASE_DROP_ITEM0 ) : false;
		res = ( res )? c.update( DBQueries.MODIFY_BASE_DROP_GROUP0 ) : false;
		if( !res )
			Common.exitUnexpected();
	}

	public static void dropCoreEngineData( EngineLoader loader ) throws Exception {
		DBConnection c = loader.getConnection();
		boolean res = true;
		res = ( res )? c.update( DBQueries.MODIFY_CORE_DROP_PARAMVALUE1 , new String[] { "" + DBVersions.CORE_ID } ) : false;
		res = ( res )? c.update( DBQueries.MODIFY_CORE_DROP_PARAM1 , new String[] { "" + DBVersions.CORE_ID } ) : false;
		res = ( res )? c.update( DBQueries.MODIFY_CORE_DROP_RESOURCE0 ) : false;
		res = ( res )? c.update( DBQueries.MODIFY_CORE_DROP_MIRROR0 ) : false;
		if( !res )
			Common.exitUnexpected();
	}
	
	
}
