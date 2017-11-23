package org.urm.db.core;

import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;

import org.urm.common.Common;
import org.urm.db.DBConnection;
import org.urm.db.DBQueries;
import org.urm.db.core.DBEnums.*;
import org.urm.engine.EngineDB;
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
		if( rs == null )
			Common.exitUnexpected();
		
		while( rs.next() ) {
			String key = rs.getInt( 1 ) + "::" + rs.getString( 2 ) + "::" + rs.getString( 3 );
			int value = rs.getInt( 4 );
			map.put( key ,  value );
		}
	}
	
	public static int getNextSequenceValue( DBConnection c ) throws Exception {
		String value = c.queryValue( DBQueries.QUERY_SEQ_GETNEXTVAL0 );
		if( value == null )
			Common.exitUnexpected();
		return( Integer.parseInt( value ) );
	}
	
	public synchronized static int getNameIndex( DBConnection c , int parent , String name , DBEnumObjectType type ) throws Exception {
		String key = parent + "::" + type.code() + "::" + name;
		Integer value = map.get( key );
		if( value != null )
			return( value );
			
		int valueSeq = getNextSequenceValue( c );
		if( !c.update( DBQueries.MODIFY_NAMES_MERGEITEM4 , new String[] { "" + parent , "" + type.code() , EngineDB.getString( name ) , "" + valueSeq } ) )
			Common.exitUnexpected();
				
		map.put( key , valueSeq );
		return( valueSeq );
	}
	
	public synchronized static void updateName( DBConnection c , int parent , String name , int id , DBEnumObjectType type ) throws Exception {
		String key = parent + "::" + type.code() + "::" + name;
		Integer value = map.get( key );
		if( value != null && value == id )
			return;
		
		if( !c.update( DBQueries.MODIFY_NAMES_MERGEITEM4 , new String[] { "" + parent , "" + type.code() , EngineDB.getString( name ) , "" + id } ) )
			Common.exitUnexpected();
		
		map.put( key , id );
	}
	
}
