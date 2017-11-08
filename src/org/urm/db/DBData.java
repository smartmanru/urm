package org.urm.db;

public abstract class DBData {

	public static int getCurrentServerVersion( DBConnection c ) throws Exception {
		return( getCurrentVersion( c , 0 ) );
	}
	
	public static int getCurrentVersion( DBConnection c , int id ) throws Exception {
		String value = c.queryValue( DBQueries.QUERY_VERSIONS_GETVERSION1 , new String[] { "" + id } );
		if( value == null || value.isEmpty() )
			return( 0 );
		return( Integer.parseInt( value ) );
	}

	public static void setNextServerVersion( DBConnection c , int version ) throws Exception {
		setNextVersion( c , 0 , version );
	}
	
	public static void setNextVersion( DBConnection c , int id , int version ) throws Exception {
		if( version == 1 )
			c.update( DBQueries.UPDATE_VERSIONS_INSERTVERSION2 , new String[] { "" + id , "" + version } );
		else
			c.update( DBQueries.UPDATE_VERSIONS_UPDATEVERSION2 , new String[] { "" + id , "" + version } );
	}
	
	public static void dropServerData( DBConnection c ) throws Exception {
		dropServerAuthData( c );
		dropServerInfraData( c );
		dropServerBaseData( c );
		dropServerAppData( c );
		dropServerCoreData( c );
	}

	public static void dropServerAuthData( DBConnection c ) throws Exception {
		c.update( DBQueries.UPDATE_AUTH_DROP_ACCESSPRODUCT0 );
		c.update( DBQueries.UPDATE_AUTH_DROP_ACCESSRESOURCE0 );
		c.update( DBQueries.UPDATE_AUTH_DROP_ACCESSNETWORK0 );
		c.update( DBQueries.UPDATE_AUTH_DROP_USER0 );
		c.update( DBQueries.UPDATE_AUTH_DROP_GROUP0 );
	}

	public static void dropServerInfraData( DBConnection c ) throws Exception {
		c.update( DBQueries.UPDATE_INFRA_DROP_ACCOUNT0 );
		c.update( DBQueries.UPDATE_INFRA_DROP_HOST0 );
		c.update( DBQueries.UPDATE_INFRA_DROP_NETWORK0 );
		c.update( DBQueries.UPDATE_INFRA_DROP_DATACENTER0 );
	}
	
	public static void dropServerBaseData( DBConnection c ) throws Exception {
		c.update( DBQueries.UPDATE_BASE_DROP_ITEMPARAM0 );
		c.update( DBQueries.UPDATE_BASE_DROP_ITEMDEPS0 );
		c.update( DBQueries.UPDATE_BASE_DROP_ITEM0 );
		c.update( DBQueries.UPDATE_BASE_DROP_GROUP0 );
		c.update( DBQueries.UPDATE_BASE_DROP_CATEGORY0 );
	}

	public static void dropServerAppData( DBConnection c ) throws Exception {
		c.update( DBQueries.UPDATE_APP_DROP_SYSTEMPARAM0 );
		c.update( DBQueries.UPDATE_APP_DROP_PRODUCT0 );
		c.update( DBQueries.UPDATE_APP_DROP_SYSTEM0 );
	}

	public static void dropServerCoreData( DBConnection c ) throws Exception {
		c.update( DBQueries.UPDATE_CORE_DROP_RESOURCE0 );
		c.update( DBQueries.UPDATE_CORE_DROP_PARAM0 );
	}
	
}
