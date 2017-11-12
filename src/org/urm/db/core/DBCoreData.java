package org.urm.db.core;

import org.urm.common.Common;
import org.urm.db.DBConnection;
import org.urm.db.DBQueries;
import org.urm.db.DBEnumTypes.*;

public abstract class DBCoreData {

	public static int CORE_ID = 0;
	
	public static int getCurrentCoreVersion( DBConnection c ) throws Exception {
		return( getCurrentVersion( c , CORE_ID ) );
	}
	
	public static int getCurrentVersion( DBConnection c , int id ) throws Exception {
		String value = c.queryValue( DBQueries.QUERY_VERSIONS_GETVERSION1 , new String[] { "" + id } );
		if( value == null || value.isEmpty() )
			return( 0 );
		return( Integer.parseInt( value ) );
	}

	public static void setNextCoreVersion( DBConnection c , int version ) throws Exception {
		setNextVersion( c , CORE_ID , version , DBEnumObjectVersionType.CORE );
	}
	
	public static void setNextVersion( DBConnection c , int id , int version , DBEnumObjectVersionType type ) throws Exception {
		if( !c.update( DBQueries.MODIFY_VERSIONS_MERGEVERSION3 , new String[] { "" + id , "" + version , "" + type.code() } ) )
			Common.exitUnexpected();
	}
	
	public static void dropCoreData( DBConnection c ) throws Exception {
		dropCoreReleasesData( c );
		dropCoreAuthData( c );
		dropCoreInfraData( c );
		dropCoreBaseData( c );
		dropCoreAppData( c );
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

	public static void dropCoreAppData( DBConnection c ) throws Exception {
		boolean res = true;
		res = ( res )? c.update( DBQueries.MODIFY_APP_DROP_PRODUCT0 ) : false;
		res = ( res )? c.update( DBQueries.MODIFY_APP_DROP_SYSTEM0 ) : false;
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
