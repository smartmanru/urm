package org.urm.db;

import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;

import org.urm.common.Common;
import org.urm.db.DBEnumTypes.DBEnumObjectType;

public abstract class DBNames {

	private static int FIXED_ID_ENGINE = 0;
	private static int FIXED_ID_ENUMS = 1;
	
	private static Map<String,Integer> map = new HashMap<String,Integer>();
	
	public static int getEngineId() {
		return( FIXED_ID_ENGINE );
	}
	
	public static int getEnumsId() {
		return( FIXED_ID_ENUMS );
	}

	public static synchronized void load( DBConnection connection ) throws Exception {
		ResultSet rs = connection.query( DBQueries.QUERY_NAMES_GETALL0 );
		if( rs == null )
			Common.exitUnexpected();
		
		while( rs.next() ) {
			String key = rs.getInt( 1 ) + "::" + rs.getString( 2 );
			int value = rs.getInt( 3 );
			map.put( key ,  value );
		}
	}
	
	public static int getNextSequenceValue( DBConnection connection ) throws Exception {
		String value = connection.queryValue( DBQueries.QUERY_SEQ_GETNEXTVAL0 );
		if( value == null )
			Common.exitUnexpected();
		return( Integer.parseInt( value ) );
	}
	
	public synchronized static int getNameIndex( DBConnection connection , int parent , String name , DBEnumObjectType type ) throws Exception {
		String key = parent + "::" + name;
		Integer value = map.get( key );
		if( value != null )
			return( value );
			
		int valueSeq = getNextSequenceValue( connection );
		connection.update( DBQueries.QUERY_NAMES_ADDITEM4 , new String[] { "" + parent , name , "" + valueSeq , "" + type.code() } );
		map.put( key , valueSeq );
		return( valueSeq );
	}
	
}
