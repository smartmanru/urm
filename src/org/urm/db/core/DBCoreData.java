package org.urm.db.core;

import org.urm.common.Common;
import org.urm.db.DBConnection;
import org.urm.db.DBQueries;

public abstract class DBCoreData {

	public static void dropCoreData( DBConnection c ) throws Exception {
		dropCoreReleasesData( c );
		dropCoreAuthData( c );
		dropCoreInfraData( c );
		dropCoreBaseData( c );
		dropCoreEngineData( c );
	}

	public static void dropCoreReleasesData( DBConnection c ) throws Exception {
		boolean res = true;
		res = ( res )? c.update( DBQueries.MODIFY_RELEASES_DROP_BUILDERS0 ) : false;
		if( !res )
			Common.exitUnexpected();
	}

	public static void dropCoreAuthData( DBConnection c ) throws Exception {
		boolean res = true;
		res = ( res )? c.update( DBQueries.MODIFY_AUTH_DROP_ACCESSPRODUCT0 ) : false;
		res = ( res )? c.update( DBQueries.MODIFY_AUTH_DROP_ACCESSRESOURCE0 ) : false;
		res = ( res )? c.update( DBQueries.MODIFY_AUTH_DROP_ACCESSNETWORK0 ) : false;
		res = ( res )? c.update( DBQueries.MODIFY_AUTH_DROP_USER0 ) : false;
		res = ( res )? c.update( DBQueries.MODIFY_AUTH_DROP_GROUP0 ) : false;
		if( !res )
			Common.exitUnexpected();
	}

	public static void dropCoreInfraData( DBConnection c ) throws Exception {
		boolean res = true;
		res = ( res )? c.update( DBQueries.MODIFY_INFRA_DROP_ACCOUNT0 ) : false;
		res = ( res )? c.update( DBQueries.MODIFY_INFRA_DROP_HOST0 ) : false;
		res = ( res )? c.update( DBQueries.MODIFY_INFRA_DROP_NETWORK0 ) : false;
		res = ( res )? c.update( DBQueries.MODIFY_INFRA_DROP_DATACENTER0 ) : false;
		if( !res )
			Common.exitUnexpected();
	}
	
	public static void dropCoreBaseData( DBConnection c ) throws Exception {
		boolean res = true;
		res = ( res )? c.update( DBQueries.MODIFY_BASE_DROP_ITEMDEPS0 ) : false;
		res = ( res )? c.update( DBQueries.MODIFY_BASE_DROP_ITEM0 ) : false;
		res = ( res )? c.update( DBQueries.MODIFY_BASE_DROP_GROUP0 ) : false;
		res = ( res )? c.update( DBQueries.MODIFY_BASE_DROP_CATEGORY0 ) : false;
		if( !res )
			Common.exitUnexpected();
	}

	public static void dropCoreEngineData( DBConnection c ) throws Exception {
		boolean res = true;
		res = ( res )? c.update( DBQueries.MODIFY_CORE_DROP_RESOURCE0 ) : false;
		res = ( res )? c.update( DBQueries.MODIFY_CORE_DROP_COREPARAM0 ) : false;
		res = ( res )? c.update( DBQueries.MODIFY_CORE_DROP_MIRROR0 ) : false;
		if( !res )
			Common.exitUnexpected();
	}
	
}
