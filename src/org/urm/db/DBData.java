package org.urm.db;

public abstract class DBData {

	public static int getCurrentServerVersion( DBConnection c ) throws Exception {
		String value = c.queryValue( DBQueries.QUERY_VERSIONS_GETSV0 );
		if( value == null || value.isEmpty() )
			return( 0 );
		return( Integer.parseInt( value ) );
	}
	
}
