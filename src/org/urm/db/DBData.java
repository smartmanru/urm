package org.urm.db;

import org.urm.common.Common;

public abstract class DBData {

	public static int CORE_ID = 0;
	
	public static int getCurrentEngineVersion( DBConnection c ) throws Exception {
		return( getCurrentVersion( c , CORE_ID ) );
	}
	
	public static int getCurrentVersion( DBConnection c , int id ) throws Exception {
		String value = c.queryValue( DBQueries.QUERY_VERSIONS_GETVERSION1 , new String[] { "" + id } );
		if( value == null || value.isEmpty() )
			return( 0 );
		return( Integer.parseInt( value ) );
	}

	public static void setNextEngineVersion( DBConnection c , int version ) throws Exception {
		setNextVersion( c , CORE_ID , version );
	}
	
	public static void setNextVersion( DBConnection c , int id , int version ) throws Exception {
		if( !c.update( DBQueries.MODIFY_VERSIONS_MERGEVERSION2 , new String[] { "" + id , "" + version } ) )
			Common.exitUnexpected();
	}
	
	public static void dropEngineData( DBConnection c ) throws Exception {
		dropEngineReleasesData( c );
		dropEngineAuthData( c );
		dropEngineInfraData( c );
		dropEngineBaseData( c );
		dropEngineAppData( c );
		dropEngineCoreData( c );
	}

	public static void dropEngineReleasesData( DBConnection c ) throws Exception {
		boolean res = true;
		res = ( res )? c.update( DBQueries.MODIFY_RELEASES_DROP_BUILDERS0 ) : false;
		if( !res )
			Common.exitUnexpected();
	}

	public static void dropEngineAuthData( DBConnection c ) throws Exception {
		boolean res = true;
		res = ( res )? c.update( DBQueries.MODIFY_AUTH_DROP_ACCESSPRODUCT0 ) : false;
		res = ( res )? c.update( DBQueries.MODIFY_AUTH_DROP_ACCESSRESOURCE0 ) : false;
		res = ( res )? c.update( DBQueries.MODIFY_AUTH_DROP_ACCESSNETWORK0 ) : false;
		res = ( res )? c.update( DBQueries.MODIFY_AUTH_DROP_USER0 ) : false;
		res = ( res )? c.update( DBQueries.MODIFY_AUTH_DROP_GROUP0 ) : false;
		if( !res )
			Common.exitUnexpected();
	}

	public static void dropEngineInfraData( DBConnection c ) throws Exception {
		boolean res = true;
		res = ( res )? c.update( DBQueries.MODIFY_INFRA_DROP_ACCOUNT0 ) : false;
		res = ( res )? c.update( DBQueries.MODIFY_INFRA_DROP_HOST0 ) : false;
		res = ( res )? c.update( DBQueries.MODIFY_INFRA_DROP_NETWORK0 ) : false;
		res = ( res )? c.update( DBQueries.MODIFY_INFRA_DROP_DATACENTER0 ) : false;
		if( !res )
			Common.exitUnexpected();
	}
	
	public static void dropEngineBaseData( DBConnection c ) throws Exception {
		boolean res = true;
		res = ( res )? c.update( DBQueries.MODIFY_BASE_DROP_ITEMDEPS0 ) : false;
		res = ( res )? c.update( DBQueries.MODIFY_BASE_DROP_ITEM0 ) : false;
		res = ( res )? c.update( DBQueries.MODIFY_BASE_DROP_GROUP0 ) : false;
		res = ( res )? c.update( DBQueries.MODIFY_BASE_DROP_CATEGORY0 ) : false;
		if( !res )
			Common.exitUnexpected();
	}

	public static void dropEngineAppData( DBConnection c ) throws Exception {
		boolean res = true;
		res = ( res )? c.update( DBQueries.MODIFY_APP_DROP_SYSTEMPARAM0 ) : false;
		res = ( res )? c.update( DBQueries.MODIFY_APP_DROP_PRODUCT0 ) : false;
		res = ( res )? c.update( DBQueries.MODIFY_APP_DROP_SYSTEMMETA0 ) : false;
		res = ( res )? c.update( DBQueries.MODIFY_APP_DROP_SYSTEM0 ) : false;
		if( !res )
			Common.exitUnexpected();
	}

	public static void dropEngineCoreData( DBConnection c ) throws Exception {
		boolean res = true;
		res = ( res )? c.update( DBQueries.MODIFY_CORE_DROP_RESOURCE0 ) : false;
		res = ( res )? c.update( DBQueries.MODIFY_CORE_DROP_COREPARAM0 ) : false;
		res = ( res )? c.update( DBQueries.MODIFY_CORE_DROP_MIRROR0 ) : false;
		if( !res )
			Common.exitUnexpected();
	}
	
}
