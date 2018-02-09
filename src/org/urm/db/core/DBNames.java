package org.urm.db.core;

import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;

import org.urm.common.Common;
import org.urm.db.DBConnection;
import org.urm.db.DBQueries;
import org.urm.db.EngineDB;
import org.urm.db.core.DBEnums.*;
import org.urm.meta.EngineLoader;

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

	public static synchronized void loaddb( EngineLoader loader ) throws Exception {
		DBConnection c = loader.getConnection();
		ResultSet rs = c.query( DBQueries.QUERY_NAMES_GETALL0 );
		try {
			while( rs.next() ) {
				String key = rs.getInt( 1 ) + "::" + rs.getString( 2 ) + "::" + rs.getString( 3 );
				int value = rs.getInt( 4 );
				map.put( key ,  value );
			}
		}
		finally {
			c.closeQuery();
		}
	}
	
	public synchronized static int getNameIndex( DBConnection c , int parent , String name , DBEnumObjectType type ) throws Exception {
		if( name == null || name.isEmpty() )
			Common.exit1( _Error.UnexpectedNameNull1 , "Unexpected empty name, object type=" + type.name() , type.name() );
			
		String key = parent + "::" + type.code() + "::" + name;
		Integer value = map.get( key );
		if( value != null && value > 0 )
			return( value );
			
		int valueSeq = c.getNextSequenceValue();
		if( !c.modify( DBQueries.MODIFY_NAMES_MERGEITEM4 , new String[] { "" + parent , "" + type.code() , EngineDB.getString( name ) , "" + valueSeq } ) )
			Common.exitUnexpected();
				
		map.put( key , valueSeq );
		return( valueSeq );
	}
	
	public synchronized static void updateName( DBConnection c , int parent , String name , int id , DBEnumObjectType type ) throws Exception {
		// update name
		String key = parent + "::" + type.code() + "::" + name;
		Integer value = map.get( key );
		if( value != null && value == id )
			return;
		
		if( !c.modify( DBQueries.MODIFY_NAMES_MERGEITEM4 , new String[] { "" + parent , "" + type.code() , EngineDB.getString( name ) , "" + id } ) )
			Common.exitUnexpected();
		
		map.put( key , id );
	}
	
}
