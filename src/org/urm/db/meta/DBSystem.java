package org.urm.db.meta;

import java.sql.ResultSet;
import java.util.LinkedList;
import java.util.List;

import org.urm.common.Common;
import org.urm.db.DBConnection;
import org.urm.db.DBData;
import org.urm.db.DBEnumTypes.DBEnumObjectType;
import org.urm.db.DBNames;
import org.urm.db.DBQueries;
import org.urm.engine.EngineDB;
import org.urm.meta.engine.EngineDirectory;
import org.urm.meta.engine.System;

public class DBSystem {

	public static void insert( DBConnection c , int CV , System system ) throws Exception {
		system.ID = DBNames.getNameIndex( c , DBData.CORE_ID , system.NAME , DBEnumObjectType.SYSTEM );
		system.CV = CV;
		if( !c.update( DBQueries.UPDATE_SYSTEM_ADD5 , new String[] {
				"" + system.ID , 
				EngineDB.getString( system.NAME ) , 
				EngineDB.getString( system.DESC ) ,
				EngineDB.getBoolean( system.OFFLINE ) ,
				"" + system.CV 
				} ) )
			Common.exitUnexpected();
	}

	public static System[] load( DBConnection c , EngineDirectory directory ) throws Exception {
		List<System> systems = new LinkedList<System>();
		
		ResultSet rs = c.query( DBQueries.QUERY_SYSTEM_GETALL0 );
		if( rs == null )
			Common.exitUnexpected();
		
		while( rs.next() ) {
			System system = new System( directory );
			system.ID = rs.getInt( 1 );
			system.NAME = rs.getString( 2 );
			system.DESC = rs.getString( 3 );
			system.OFFLINE = rs.getBoolean( 4 );
			system.CV = rs.getInt( 5 );
			systems.add( system );
		}
		
		return( systems.toArray( new System[0] ) );
	}
	
}