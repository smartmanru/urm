package org.urm.db;

import org.urm.common.Common;
import org.urm.db.DBEnums.DBEnumObjectVersionType;

public abstract class DBVersions {

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

}
